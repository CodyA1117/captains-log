// src/main/java/com/minderall/captainslogapp/dto/NutritionLogDTO.java
package com.minderall.captainslogapp.dto;

import java.time.LocalDate;

public class NutritionLogDTO {
    private LocalDate date;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double calories;
    private Double bodyWeight;
    private Double waterIntakeOz;

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }
    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }
    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat; }
    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }
    public Double getBodyWeight() { return bodyWeight; }
    public void setBodyWeight(Double bodyWeight) { this.bodyWeight = bodyWeight; }
    public Double getWaterIntakeOz() { return waterIntakeOz; }
    public void setWaterIntakeOz(Double waterIntakeOz) { this.waterIntakeOz = waterIntakeOz; }
}