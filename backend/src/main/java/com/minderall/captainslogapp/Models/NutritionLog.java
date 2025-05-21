package com.minderall.captainslogapp.Models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private LocalDate date;
    private double protein;
    private double carbs;
    private double fat;
    private double bodyWeight;
    private double waterIntakeOz;

    @Lob
    private String mealsJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getBodyWeight() {
        return bodyWeight;
    }

    public void setBodyWeight(double bodyWeight) {
        this.bodyWeight = bodyWeight;
    }

    public double getWaterIntakeOz() {
        return waterIntakeOz;
    }

    public void setWaterIntakeOz(double waterIntakeOz) {
        this.waterIntakeOz = waterIntakeOz;
    }

    public String getMealsJson() {
        return mealsJson;
    }

    public void setMealsJson(String mealsJson) {
        this.mealsJson = mealsJson;
    }
}
