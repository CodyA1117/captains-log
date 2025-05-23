package com.minderall.captainslogapp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.minderall.captainslogapp.Models.NutritionLog;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.NutritionLogRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import com.minderall.captainslogapp.Services.NutritionixService;
import com.minderall.captainslogapp.dto.NutritionLogDTO; // Create this DTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionLogController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionLogController.class);

    @Autowired
    private NutritionixService nutritionixService;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private UserRepository userRepository;

    // DTO for sending nutrition log data to frontend
    private NutritionLogDTO convertToDTO(NutritionLog log) {
        if (log == null) return null;
        NutritionLogDTO dto = new NutritionLogDTO();
        dto.setDate(log.getDate());
        dto.setProtein(log.getProtein());
        dto.setCarbs(log.getCarbs());
        dto.setFat(log.getFat());
        dto.setCalories(log.getCalories());
        dto.setBodyWeight(log.getBodyWeight());
        dto.setWaterIntakeOz(log.getWaterIntakeOz());
        // dto.setMealsJson(log.getMealsJson()); // Optional: if frontend needs raw meals
        return dto;
    }

    @PostMapping("/sync-today") // Renamed for clarity, matches Oura pattern
    public ResponseEntity<String> syncTodayNutritionData(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        String appUserEmail = userDetails.getUsername();
        User user = userRepository.findByEmail(appUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + appUserEmail));

        if (!user.isNutritionixConnected() || user.getNutritionixUserId() == null) {
            return ResponseEntity.badRequest().body("Nutritionix not connected for this user.");
        }

        LocalDate today = LocalDate.now();
        JsonNode nutritionDataNode = nutritionixService.fetchNutritionDataForDate(appUserEmail, today);

        if (nutritionDataNode == null || !nutritionDataNode.has("dates") || nutritionDataNode.get("dates").isEmpty()) {
            logger.warn("No nutrition data found or failed to fetch from Nutritionix API for user {} on {}", appUserEmail, today);
            return ResponseEntity.ok("No new nutrition data found from Nutritionix for today or sync failed.");
        }

        // The /reports/totals endpoint returns a "dates" array. We expect one entry for today.
        JsonNode todayTotalsNode = nutritionDataNode.get("dates").get(0);
        if (todayTotalsNode == null) {
            logger.warn("Today's totals node is missing in Nutritionix response for user {} on {}", appUserEmail, today);
            return ResponseEntity.ok("Nutritionix response format unexpected for today's totals.");
        }

        NutritionLog log = nutritionLogRepository.findByUserAndDate(user, today)
                .orElse(new NutritionLog()); // Get existing or create new

        log.setUser(user);
        log.setDate(today);
        log.setProtein(todayTotalsNode.path("protein_grams").asDouble(log.getProtein() != null ? log.getProtein() : 0));
        log.setCarbs(todayTotalsNode.path("carbs_grams").asDouble(log.getCarbs() != null ? log.getCarbs() : 0));
        log.setFat(todayTotalsNode.path("fat_grams").asDouble(log.getFat() != null ? log.getFat() : 0));
        log.setCalories(todayTotalsNode.path("calories_kcal").asDouble(log.getCalories() != null ? log.getCalories() : 0));

        // Body weight and water are not typically part of daily food log totals from Nutritionix /reports/totals
        // These might need to be logged separately or come from a different source/endpoint.
        // For now, let's assume they are not updated by this specific sync.
        // log.setWaterIntakeOz(...);
        // log.setBodyWeight(...);

        // mealsJson might not be relevant if using /reports/totals
        // log.setMealsJson(nutritionDataNode.path("foods").toString()); // If fetching individual foods

        nutritionLogRepository.save(log);
        logger.info("Nutrition data synced successfully for user {} on {}", appUserEmail, today);
        return ResponseEntity.ok("Nutrition data synced successfully for today!");
    }

    // Endpoint to get today's nutrition log from *your* database
    @GetMapping("/today")
    public ResponseEntity<NutritionLogDTO> getTodayNutritionLog(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return nutritionLogRepository.findByUserAndDate(user, LocalDate.now())
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // Optional: Endpoint to get historical nutrition logs
    @GetMapping("/history")
    public ResponseEntity<List<NutritionLogDTO>> getNutritionLogHistory(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<NutritionLogDTO> history = nutritionLogRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }
}