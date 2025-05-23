package com.minderall.captainslogapp.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.Models.User; // Import User
import com.minderall.captainslogapp.Repositories.UserRepository; // Import UserRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Autowire UserRepository
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder; // For query params

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class NutritionixService {

    private static final Logger logger = LoggerFactory.getLogger(NutritionixService.class);

    @Value("${nutritionix.app.id}")
    private String appId;

    @Value("${nutritionix.app.key}")
    private String appKey;

    @Autowired // Added for fetching User to get nutritionix_user_id
    private UserRepository userRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Fetches logged foods for a specific date (e.g., today) for a given app user (by their email)
    public JsonNode fetchNutritionDataForDate(String appUserEmail, LocalDate date) {
        User user = userRepository.findByEmail(appUserEmail)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", appUserEmail);
                    return new UsernameNotFoundException("User not found: " + appUserEmail);
                });

        if (!user.isNutritionixConnected() || user.getNutritionixUserId() == null) {
            logger.warn("Nutritionix not connected for user: {} or Nutritionix User ID is null.", appUserEmail);
            return null;
        }

        String remoteUserId = user.getNutritionixUserId();
        logger.info("Fetching Nutritionix data for appUserEmail: {}, remoteUserId: {}, date: {}", appUserEmail, remoteUserId, date);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-app-id", appId);
        headers.set("x-app-key", appKey);
        headers.set("x-remote-user-id", remoteUserId); // Use the stored remote user ID
        // headers.setContentType(MediaType.APPLICATION_JSON); // Not strictly needed for GET with no body

        // Nutritionix /v2/log endpoint fetches logged food.
        // It doesn't seem to have a direct "daily summary" endpoint like Oura.
        // You'd fetch foods for a day and aggregate yourself, or use /v2/reports/totals for date ranges.
        // For simplicity, let's assume we are fetching the log for a specific day.
        // The /v2/log endpoint is typically a GET request if you're fetching existing logs.
        // Or a POST to /v2/natural/nutrients if you are sending a query like "1 cup of rice".

        // Let's use the /v2/reports/totals endpoint to get a daily summary.
        // Example from Nutritionix docs: GET /v2/reports/totals?from=2023-08-01&to=2023-08-01
        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String url = UriComponentsBuilder.fromHttpUrl("https://trackapi.nutritionix.com/v2/reports/totals")
                .queryParam("begin", formattedDate + "T00:00:00") // Requires full timestamp for begin/end
                .queryParam("end", formattedDate + "T23:59:59")
                // .queryParam("timezone_aware", "true") // Optional: if you want results based on user's logged timezone
                .toUriString();

        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        try {
            logger.debug("Requesting Nutritionix daily total: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            logger.info("Nutritionix API response for daily total for user {}: {} - {}", appUserEmail, response.getStatusCode(), response.getBody());
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            } else {
                logger.error("Failed to fetch nutrition totals from Nutritionix for user {}. Status: {}, Body: {}",
                        appUserEmail, response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            logger.error("Nutritionix API HTTP Error for user {}: {} - {}", appUserEmail, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching or parsing nutrition totals from Nutritionix for user {}: {}", appUserEmail, e.getMessage(), e);
            return null;
        }
    }
}