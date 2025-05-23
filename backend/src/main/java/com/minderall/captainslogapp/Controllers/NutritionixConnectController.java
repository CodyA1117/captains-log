package com.minderall.captainslogapp.Controllers;

import com.fasterxml.jackson.databind.JsonNode; // For parsing response
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.dto.NutritionixSignupDTO;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

// import java.util.Optional; // No longer needed as findByEmail throws
import java.util.UUID;

@RestController
@RequestMapping("/api/nutritionix")
public class NutritionixConnectController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionixConnectController.class);

    @Value("${nutritionix.app.id}")
    private String appId;

    @Value("${nutritionix.app.key}")
    private String appKey;

    private final UserRepository appUserRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public NutritionixConnectController(UserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // This DTO should be in your dto package
    // public static class NutritionixSignupDTO { ... } // Or import if separate file

    @PostMapping("/connect")
    public ResponseEntity<?> connectNutritionixAccount(
            @AuthenticationPrincipal UserDetails userDetails, // Use authenticated user
            @RequestBody NutritionixSignupDTO dto) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        User user = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        if (user.isNutritionixConnected() && user.getNutritionixUserId() != null) {
            logger.info("User {} is already connected to Nutritionix with ID: {}", user.getEmail(), user.getNutritionixUserId());
            return ResponseEntity.ok("User already connected to Nutritionix with ID: " + user.getNutritionixUserId());
        }

        // Generate a Nutritionix user ID if one doesn't exist or you want a new one per connection attempt
        // For simplicity, let's generate a new one each time connect is called IF not already set.
        // A more robust approach might check if a previous attempt failed and reuse.
        String remoteUserId = "captainslog-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        logger.info("Generated remote_user_id for Nutritionix: {}", remoteUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-app-id", appId);
        headers.set("x-app-key", appKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construct the request body for Nutritionix user signup
        // Nutritionix v2 user signup doesn't actually take these biometrics.
        // It's more about just creating a user ID on their end if needed for tracking.
        // The actual /v2/natural/nutrients or /v2/log endpoint uses x-remote-user-id.
        // The /v2/user/signup is more of a conceptual step. The key is that *you* generate
        // and manage the remote_user_id.
        // For now, let's assume you just need to store this remote_user_id.
        // If their signup endpoint *did* take these, this is how you'd send them:
        /*
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("user_id", remoteUserId); // This is the x-remote-user-id you will use later
        // requestBodyMap.put("weight_kg", dto.getWeightKg());
        // requestBodyMap.put("height_cm", dto.getHeightCm());
        // requestBodyMap.put("age", dto.getAge());
        // requestBodyMap.put("gender", dto.getGender());
        */

        // Since Nutritionix /v2/user/signup isn't a real endpoint for creating users
        // in the way some APIs have, the "connection" step for your app is primarily:
        // 1. Generate a unique ID that *you* will use as the `x-remote-user-id` for this app user.
        // 2. Store this ID in your `app_user` table.
        // 3. Mark the user as "Nutritionix connected."

        user.setNutritionixUserId(remoteUserId); // Store the ID you generated
        user.setNutritionixConnected(true);
        appUserRepository.save(user);

        logger.info("Nutritionix 'connection' established for app user {} with remote_user_id: {}", user.getEmail(), remoteUserId);
        return ResponseEntity.ok("Nutritionix connected successfully. Your Nutritionix User ID: " + remoteUserId);
    }
}