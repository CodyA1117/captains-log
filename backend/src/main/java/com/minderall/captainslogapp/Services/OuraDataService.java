package com.minderall.captainslogapp.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.OuraToken;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.OuraDataRepository;
import com.minderall.captainslogapp.Repositories.OuraTokenRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class OuraDataService {

    @Autowired
    private OuraTokenRepository tokenRepository;

    @Autowired
    private OuraDataRepository dataRepository;

    @Autowired
    private UserRepository userRepository;



    private final String readinessEndpoint = "https://api.ouraring.com/v2/usercollection/readiness";
    private final String sleepEndpoint = "https://api.ouraring.com/v2/usercollection/sleep";
    private final String activityEndpoint = "https://api.ouraring.com/v2/usercollection/activity";
    private final String stressEndpoint = "https://api.ouraring.com/v2/usercollection/daily_stress";
    private final String heartRateEndpoint = "https://api.ouraring.com/v2/usercollection/heart_rate";

    public void fetchAndSaveTodayReadiness(String userEmail) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) return;

        String accessToken = optionalToken.get().getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        String today = LocalDate.now().toString();

        String url = readinessEndpoint + "?start_date=" + today + "&end_date=" + today;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode readinessArray = root.get("data");

                if (readinessArray != null && readinessArray.isArray() && readinessArray.size() > 0) {
                    JsonNode todayData = readinessArray.get(0);
                    int readinessScore = todayData.get("score").asInt();

                    OuraData data = new OuraData();
                    data.setUser(optionalToken.get().getUser());
                    data.setReadinessScore(readinessScore);
                    data.setDate(LocalDate.now());

                    dataRepository.save(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // 1. Fetch and save today's sleep score
    public void fetchAndSaveTodaySleepScore(String userEmail) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) return;

        String accessToken = optionalToken.get().getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        String today = LocalDate.now().toString();
        String url = sleepEndpoint + "?start_date=" + today + "&end_date=" + today;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode sleepArray = root.get("data");

                if (sleepArray != null && sleepArray.isArray() && sleepArray.size() > 0) {
                    JsonNode todayData = sleepArray.get(0);
                    int sleepScore = todayData.get("score").asInt();

                    OuraData data = dataRepository.findByUserAndDate(optionalToken.get().getUser(), LocalDate.now())
                            .orElse(new OuraData());
                    data.setUser(optionalToken.get().getUser());
                    data.setDate(LocalDate.now());
                    data.setSleepScore(sleepScore);

                    dataRepository.save(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // 2. Fetch and save today's activity score
    public void fetchAndSaveTodayActivityScore(String userEmail) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) return;

        String accessToken = optionalToken.get().getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        String today = LocalDate.now().toString();
        String url = activityEndpoint + "?start_date=" + today + "&end_date=" + today;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode todayData = dataArray.get(0);
                    int activityScore = todayData.get("score").asInt();

                    OuraData data = dataRepository.findByUserAndDate(optionalToken.get().getUser(), LocalDate.now())
                            .orElse(new OuraData());
                    data.setUser(optionalToken.get().getUser());
                    data.setDate(LocalDate.now());
                    data.setActivityScore(activityScore);

                    dataRepository.save(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 3. Fetch and save today's stress score
    public void fetchAndSaveTodayStressScore(String userEmail) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) return;

        String accessToken = optionalToken.get().getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        String today = LocalDate.now().toString();
        String url = stressEndpoint + "?start_date=" + today + "&end_date=" + today;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode todayData = dataArray.get(0);
                    int stressScore = todayData.get("average_stress").asInt();

                    OuraData data = dataRepository.findByUserAndDate(optionalToken.get().getUser(), LocalDate.now())
                            .orElse(new OuraData());
                    data.setUser(optionalToken.get().getUser());
                    data.setDate(LocalDate.now());
                    data.setStressScore(stressScore);

                    dataRepository.save(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 4. Fetch and save today's heart rate
    public void fetchAndSaveTodayHeartRate(String userEmail) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) return;

        String accessToken = optionalToken.get().getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        String today = LocalDate.now().toString();
        String url = heartRateEndpoint + "?start_date=" + today + "&end_date=" + today;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode todayData = dataArray.get(0);
                    int heartRate = todayData.get("resting_heart_rate").asInt();

                    OuraData data = dataRepository.findByUserAndDate(optionalToken.get().getUser(), LocalDate.now())
                            .orElse(new OuraData());
                    data.setUser(optionalToken.get().getUser());
                    data.setDate(LocalDate.now());
                    data.setHeartRate(heartRate);

                    dataRepository.save(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void fetchAndSaveOuraMetric(String userEmail, String endpoint, String metricType) {
        Optional<OuraToken> optionalToken = tokenRepository.findByUserEmail(userEmail);
        if (optionalToken.isEmpty()) return;

        String accessToken = optionalToken.get().getAccessToken();
        String today = LocalDate.now().toString();
        String url = endpoint + "?start_date=" + today + "&end_date=" + today;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, createRequest(accessToken), String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode dataArray = root.get("data");

                if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode todayData = dataArray.get(0);

                    OuraData data = dataRepository.findByUserAndDate(optionalToken.get().getUser(), LocalDate.now())
                            .orElse(new OuraData());

                    data.setUser(optionalToken.get().getUser());
                    data.setDate(LocalDate.now());

                    switch (metricType) {
                        case "readiness":
                            data.setReadinessScore(todayData.get("score").asInt());
                            break;
                        case "sleep":
                            data.setSleepScore(todayData.get("score").asInt());
                            break;
                        case "activity":
                            data.setActivityScore(todayData.get("score").asInt());
                            break;
                        case "heartrate":
                            data.setHeartRate(todayData.get("resting_heart_rate").asInt());
                            break;
                    }

                    dataRepository.save(data);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private HttpEntity<String> createRequest(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(headers);
    }

    public Optional<OuraData> getTodayOuraData(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        LocalDate today = LocalDate.now();
        return dataRepository.findByUserAndDate(user, today);
    }



}
