package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Repositories.UserRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionLogController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionLogController.class);

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @Autowired
    private UserRepository userRepository;

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
        return dto;
    }

    @GetMapping("/today")
    public ResponseEntity<NutritionLogDTO> getTodayNutritionLog(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return nutritionLogRepository.findByUserAndDate(user, LocalDate.now())
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

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

    // Endpoint to log/update a full day's nutrition summary MANUALLY
    @PostMapping("/log-daily-summary")
    public ResponseEntity<NutritionLogDTO> logDailyNutritionSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ManualNutritionLogRequestDTO requestDTO) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + userDetails.getUsername()));

        LocalDate logDate = (requestDTO.getLoggedAt() != null) ? requestDTO.getLoggedAt().toLocalDate() : LocalDate.now();

        NutritionLog dailyLog = nutritionLogRepository.findByUserAndDate(user, logDate)
                .orElseGet(() -> {
                    NutritionLog newLog = new NutritionLog();
                    newLog.setUser(user);
                    newLog.setDate(logDate);
                    // Initialize all to 0.0 if it's a new log for the day
                    newLog.setCalories(0.0);
                    newLog.setProtein(0.0);
                    newLog.setCarbs(0.0);
                    newLog.setFat(0.0);
                    newLog.setWaterIntakeOz(0.0);
                    // bodyWeight might be set separately or default to null/previous
                    return newLog;
                });

        // Update with provided values (if any)
        if (requestDTO.getCalories() != null) dailyLog.setCalories(requestDTO.getCalories());
        if (requestDTO.getProtein() != null) dailyLog.setProtein(requestDTO.getProtein());
        if (requestDTO.getCarbs() != null) dailyLog.setCarbs(requestDTO.getCarbs());
        if (requestDTO.getFat() != null) dailyLog.setFat(requestDTO.getFat());
        if (requestDTO.getWaterIntakeOz() != null) dailyLog.setWaterIntakeOz(requestDTO.getWaterIntakeOz());
        if (requestDTO.getBodyWeight() != null) dailyLog.setBodyWeight(requestDTO.getBodyWeight());
        // mealsJson can be a simple description or empty if not used
        dailyLog.setMealsJson(requestDTO.getDescription() != null ? "{\"description\": \"" + requestDTO.getDescription() + "\"}" : null);


        NutritionLog savedLog = nutritionLogRepository.save(dailyLog);
        logger.info("Manually logged/updated nutrition summary for user {} on {}.", user.getEmail(), logDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedLog));
    }
}