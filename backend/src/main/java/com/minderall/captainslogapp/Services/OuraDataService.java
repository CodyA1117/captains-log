package com.minderall.captainslogapp.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.OuraDataRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class OuraDataService {

    private static final Logger logger = LoggerFactory.getLogger(OuraDataService.class);

    @Autowired
    private OuraTokenRepository tokenRepository;

    @Autowired
    private OuraDataRepository dataRepository;

    @Autowired
    private UserRepository userRepository; // Used in getTodayOuraData

    private final String OURA_API_BASE_URL = "https://api.ouraring.com/v2/usercollection/";
    private final String READINESS_ENDPOINT = OURA_API_BASE_URL + "daily_readiness";
    private final String SLEEP_ENDPOINT = OURA_API_BASE_URL + "daily_sleep";
    private final String ACTIVITY_ENDPOINT = OURA_API_BASE_URL + "daily_activity";
    private final String STRESS_ENDPOINT = OURA_API_BASE_URL + "daily_stress";

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
            // Initialize all Double fields to 0.0 to avoid DB null constraint issues on new records
            newData.setReadinessScore(null); // Scores can be null initially
            newData.setSleepScore(null);
            newData.setActivityScore(null);
            newData.setHeartRate(null);
            newData.setStressScore(null);
            return newData;
        }
    }

    private void fetchAndSaveGenericOuraMetric(String userEmail, String endpoint, String metricName,
                                               Function<JsonNode, Integer> jsonFieldExtractor,
                                               BiConsumer<OuraData, Integer> dataSetter) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) {
            logger.warn("No Oura token found for user {} while fetching {}. User needs to connect Oura.", userEmail, metricName);
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

            if (response.getBody() != null) {
                logger.info("<<<<< RAW OURA RESPONSE for [{}] for user {} >>>>>: {}", metricName, userEmail, response.getBody());
            }

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode dataArray = root.path("data");

                if (dataArray.isArray() && !dataArray.isEmpty()) {
                    JsonNode todayDataNode = dataArray.get(0);
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
            // Log 401 specifically, user will need to re-authenticate with Oura via UI
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.error("Oura API returned 401 UNAUTHORIZED for user {} fetching [{}]. Access token likely expired or revoked. User needs to re-connect Oura. Detail: {}", userEmail, metricName, e.getResponseBodyAsString());
            } else {
                logger.error("Oura API HTTP Error while fetching [{}] for user {}: Status Code: {}, Response Body: {}", metricName, userEmail, e.getStatusCode(), e.getResponseBodyAsString(), e);
            }
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
        fetchAndSaveGenericOuraMetric(userEmail, SLEEP_ENDPOINT, "Sleep Score",
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
        // **YOU MUST UPDATE THE PATH BELOW BASED ON YOUR ACTUAL RAW STRESS JSON LOGS**
        // This is just a placeholder. If Oura returns no data or this path is wrong, it will be N/A.
        fetchAndSaveGenericOuraMetric(userEmail, STRESS_ENDPOINT, "Stress (Example: day_stress_balance)",
                jsonNode -> jsonNode.path("day_stress_balance").asInt(-1), // <<< ADJUST THIS PATH!!!
                OuraData::setStressScore
        );
    }

    public void fetchAndSaveTodayHeartRate(String userEmail) {
        // Attempting to get resting_heart_rate from Daily Readiness contributors, as it seemed more reliable in your logs
        logger.info("Fetching Resting Heart Rate from Daily Readiness contributors for user {}.", userEmail);
        fetchAndSaveGenericOuraMetric(userEmail, READINESS_ENDPOINT, "Resting Heart Rate (from Readiness)",
                jsonNode -> jsonNode.path("contributors").path("resting_heart_rate").asInt(-1),
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<OuraData> dataList = dataRepository.findByUserAndDate(user, LocalDate.now());
        if (!dataList.isEmpty()) {
            OuraData data = dataList.get(0);
            // Check if there's any actual score data, not just an empty record
            if (data.getReadinessScore() != null || data.getSleepScore() != null ||
                    data.getActivityScore() != null || data.getHeartRate() != null ||
                    data.getStressScore() != null) {
                logger.info("Returning OuraData for user {} on {}: {}", email, LocalDate.now(), data);
                return Optional.of(data);
            } else {
                logger.info("OuraData record exists for user {} on {} but all score fields are null.", email, LocalDate.now());
                return Optional.empty(); // Treat as no meaningful data
            }
        }
        logger.info("No OuraData database record found for user ID {} on {} for getTodayOuraData.", user.getId(), LocalDate.now());
        return Optional.empty();
    }
}