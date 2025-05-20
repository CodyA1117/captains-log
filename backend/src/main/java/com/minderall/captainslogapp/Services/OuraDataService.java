package com.minderall.captainslogapp.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.OuraToken;
import com.minderall.captainslogapp.Repositories.OuraDataRepository;
import com.minderall.captainslogapp.Repositories.OuraTokenRepository;
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

    private final String readinessEndpoint = "https://api.ouraring.com/v2/usercollection/readiness";

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
}
