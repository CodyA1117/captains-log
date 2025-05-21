package com.minderall.captainslogapp.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minderall.captainslogapp.dto.NutritionixSignupDTO;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/nutritionix")
public class NutritionixConnectController {

    @Value("${nutritionix.app.id}")
    private String appId;

    @Value("${nutritionix.app.key}")
    private String appKey;

    private final UserRepository appUserRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public NutritionixConnectController(UserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connectNutritionixAccount(@RequestParam Long appUserId,
                                                       @RequestBody NutritionixSignupDTO dto) {
        Optional<User> userOpt = appUserRepository.findById(appUserId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        if (user.isNutritionixConnected()) {
            return ResponseEntity.ok("Already connected.");
        }

        // Generate a Nutritionix user ID (e.g. user-cody-<uuid>)
        String nutritionixUserId = "user-" + UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-app-id", appId);
        headers.set("x-app-key", appKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build JSON body
        ObjectMapper mapper = new ObjectMapper();
        try {
            String body = mapper.writeValueAsString(new Object() {
                public String user_id = nutritionixUserId;
                public double weight_kg = dto.weightKg;
                public double height_cm = dto.heightCm;
                public int age = dto.age;
                public String gender = dto.gender;
            });

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            String url = "https://trackapi.nutritionix.com/v2/user/signup";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                user.setNutritionixUserId(nutritionixUserId);
                user.setNutritionixConnected(true);
                appUserRepository.save(user);
                return ResponseEntity.ok("Nutritionix connected successfully.");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Error from Nutritionix API.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
