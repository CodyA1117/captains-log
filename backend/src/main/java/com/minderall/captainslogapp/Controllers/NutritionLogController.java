package com.minderall.captainslogapp.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.minderall.captainslogapp.Models.NutritionLog;
import com.minderall.captainslogapp.Repositories.NutritionLogRepository;
import com.minderall.captainslogapp.Services.NutritionixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionLogController {

    @Autowired
    private NutritionixService nutritionixService;

    @Autowired
    private NutritionLogRepository nutritionLogRepository;

    @PostMapping("/sync")
    public String syncNutritionData(@RequestParam String userId) {
        JsonNode root = nutritionixService.fetchTodayNutritionData(userId);

        if (root == null || !root.has("foods")) {
            return "No data found or failed to fetch.";
        }

        double protein = 0, carbs = 0, fat = 0, weight = 0, water = 0;
        String meals = root.get("foods").toString();

        for (JsonNode food : root.get("foods")) {
            protein += food.path("nf_protein").asDouble(0);
            carbs += food.path("nf_total_carbohydrate").asDouble(0);
            fat += food.path("nf_total_fat").asDouble(0);
            water += food.path("nf_water_grams").asDouble(0);
        }

        if (root.has("weight")) {
            weight = root.path("weight").asDouble(0);
        }

        NutritionLog log = new NutritionLog();
        log.setUserId(userId);
        log.setDate(LocalDate.now());
        log.setProtein(protein);
        log.setCarbs(carbs);
        log.setFat(fat);
        log.setWaterIntakeOz(water);
        log.setBodyWeight(weight);
        log.setMealsJson(meals);

        nutritionLogRepository.save(log);

        return "Nutrition data synced successfully!";
    }
}
