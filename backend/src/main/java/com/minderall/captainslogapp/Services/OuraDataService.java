package com.minderall.captainslogapp.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.OuraToken;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.OuraDataRepository;
import com.minderall.captainslogapp.Repositories.OuraTokenRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function; // Ensure this is java.util.function.Function

@Service
public class OuraDataService {

    private static final Logger logger = LoggerFactory.getLogger(OuraDataService.class);

    @Autowired
    private OuraTokenRepository tokenRepository;

    @Autowired
    private OuraDataRepository dataRepository;

    @Autowired
    private UserRepository userRepository;

    private final String OURA_API_BASE_URL = "https://api.ouraring.com/v2/usercollection/";
    private final String READINESS_ENDPOINT = OURA_API_BASE_URL + "daily_readiness";
    private final String SLEEP_ENDPOINT = OURA_API_BASE_URL + "daily_sleep";
    private final String ACTIVITY_ENDPOINT = OURA_API_BASE_URL + "daily_activity";
    private final String STRESS_ENDPOINT = OURA_API_BASE_URL + "daily_stress";
    // private final String HEART_RATE_TIMESERIES_ENDPOINT = OURA_API_BASE_URL + "heart_rate"; // Not used for daily summary

    private OuraData getOrCreateOuraDataRecord(User user, LocalDate date) {
        List<OuraData> existingDataList = dataRepository.findByUserAndDate(user, date);
        if (!existingDataList.isEmpty()) {
            if (existingDataList.size() > 1) {
                logger.warn("Found {} OuraData records for user ID {} on {}. Using the first one found (ID: {}). Consider cleaning up duplicates.",
                        existingDataList.size(), user.getId(), date, existingDataList.get(0).getId());
            }
            return existingDataList.get(0);
        } else {
            OuraData newData = new OuraData();
            newData.setUser(user);
            newData.setDate(date);
            return newData;
        }
    }

    private void fetchAndSaveGenericOuraMetric(String userEmail, String endpoint, String metricName,
                                               Function<JsonNode, Integer> jsonFieldExtractor, // Corrected import
                                               BiConsumer<OuraData, Integer> dataSetter) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) {
            logger.warn("No Oura token found for user {} while fetching {}.", userEmail, metricName);
            return;
        }
        User user = optionalToken.get().getUser();
        String accessToken = optionalToken.get().getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createRequestHeaders(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        String today = LocalDate.now().toString();
        String url = endpoint + "?start_date=" + today + "&end_date=" + today;

        try {
            logger.debug("Fetching {} for user {} from URL: {}", metricName, userEmail, url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // VVVV TEMPORARY RAW JSON LOGGING - REMOVE OR COMMENT OUT AFTER DEBUGGING VVVV
            if (response.getBody() != null) {
                logger.info("<<<<< RAW OURA RESPONSE for [{}] for user {} >>>>>: {}", metricName, userEmail, response.getBody());
            }
            // ^^^^ TEMPORARY RAW JSON LOGGING - REMOVE OR COMMENT OUT AFTER DEBUGGING ^^^^

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode dataArray = root.path("data");

                if (dataArray.isArray() && !dataArray.isEmpty()) {
                    JsonNode todayDataNode = dataArray.get(0); // Assumes data for 'today' is the first item
                    Integer metricValue = jsonFieldExtractor.apply(todayDataNode);

                    if (metricValue != null && metricValue != -1) {
                        OuraData dataToSave = getOrCreateOuraDataRecord(user, LocalDate.now());
                        dataSetter.accept(dataToSave, metricValue);
                        dataRepository.save(dataToSave);
                        logger.info("Successfully saved {} ({}) for user {}", metricName, metricValue, userEmail);
                    } else {
                        logger.warn("{} value not found or invalid (-1) in Oura response for user {}. JSON node for the day: {}", metricName, userEmail, todayDataNode.toString());
                    }
                } else {
                    logger.info("No data array or empty data array in Oura response for [{}] for user {}. Full response body: {}", metricName, userEmail, response.getBody());
                }
            } else {
                logger.error("Failed to fetch {} from Oura for user {}. Status: {}, Body: {}", metricName, userEmail, response.getStatusCode(), response.getBody());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Oura API HTTP Error while fetching [{}] for user {}: Status Code: {}, Response Body: {}", metricName, userEmail, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("General error processing {} data for user {}: {}", metricName, userEmail, e.getMessage(), e);
        }
    }

    public void fetchAndSaveTodayReadiness(String userEmail) {
        fetchAndSaveGenericOuraMetric(userEmail, READINESS_ENDPOINT, "Readiness",
                jsonNode -> jsonNode.path("score").asInt(-1),
                OuraData::setReadinessScore
        );
    }

    public void fetchAndSaveTodaySleepScore(String userEmail) {
        fetchAndSaveGenericOuraMetric(userEmail, SLEEP_ENDPOINT, "Sleep Score", // Changed metric name for clarity
                jsonNode -> jsonNode.path("score").asInt(-1),
                OuraData::setSleepScore
        );
    }

    public void fetchAndSaveTodayActivityScore(String userEmail) {
        fetchAndSaveGenericOuraMetric(userEmail, ACTIVITY_ENDPOINT, "Activity",
                jsonNode -> jsonNode.path("score").asInt(-1),
                OuraData::setActivityScore
        );
    }

    public void fetchAndSaveTodayStressScore(String userEmail) {
        // Oura V2 /daily_stress provides "day_stress_balance" or "contributors.recovery_time_rate" etc.
        // We'll attempt to use "day_stress_balance".
        fetchAndSaveGenericOuraMetric(userEmail, STRESS_ENDPOINT, "Stress (Day Stress Balance)",
                // This value might be a float or not always present, asInt(-1) will handle.
                jsonNode -> jsonNode.path("day_stress_balance").asInt(-1),
                OuraData::setStressScore
        );
    }

    public void fetchAndSaveTodayHeartRate(String userEmail) {
        // Resting heart rate IS available directly in the /daily_sleep object according to Oura V2 docs.
        // However, your logs showed it missing from the sleep data but present in readiness contributors.
        // Let's prioritize the documented field from Daily Sleep first. If it's often missing,
        // an alternative could be readiness contributors, but that might reflect differently.
        // The WARNING "value not found or invalid (-1)" for HR from sleep means the field "resting_heart_rate"
        // was not present in the sleep JSON object that day.
        // Let's stick to the documented source (Daily Sleep) and accept it might be N/A some days.
        fetchAndSaveGenericOuraMetric(userEmail, SLEEP_ENDPOINT, "Resting Heart Rate (from Sleep)",
                jsonNode -> jsonNode.path("resting_heart_rate").asInt(-1), // From Oura V2 Daily Sleep object
                OuraData::setHeartRate
        );
    }

    private HttpHeaders createRequestHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public Optional<OuraData> getTodayOuraData(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("User not found for email {} when calling getTodayOuraData.", email);
            return Optional.empty();
        }

        User user = userOpt.get();
        LocalDate today = LocalDate.now();
        List<OuraData> dataList = dataRepository.findByUserAndDate(user, today);

        if (!dataList.isEmpty()) {
            if (dataList.size() > 1) {
                logger.warn("Found {} OuraData records for user ID {} on {} for getTodayOuraData. Returning the first one.",
                        dataList.size(), user.getId(), today);
            }
            // Make sure the returned OuraData object actually has values set
            OuraData data = dataList.get(0);
            if (data.getReadinessScore() == null && data.getSleepScore() == null && data.getActivityScore() == null && data.getHeartRate() == null) {
                logger.warn("OuraData record found for user {} on {}, but all metric fields are null. Data might not have synced properly.", email, today);
            }
            return Optional.of(data);
        }
        logger.info("No OuraData database record found for user ID {} on {} for getTodayOuraData.", user.getId(), today);
        return Optional.empty();
    }
}