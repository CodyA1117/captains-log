package com.minderall.captainslogapp.dto;

import java.time.LocalDateTime;

public class FoodLogRequestDTO {
    private String foodQuery;
    private String description; // <<< ADDED THIS FIELD (for manual entries)
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private LocalDateTime loggedAt;

    // Getters and Setters
    public String getFoodQuery() { return foodQuery; }
    public void setFoodQuery(String foodQuery) { this.foodQuery = foodQuery; }

    public String getDescription() { return description; } // <<< ADDED GETTER
    public void setDescription(String description) { this.description = description; } // <<< ADDED SETTER

    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }
    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }
    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }
    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
}