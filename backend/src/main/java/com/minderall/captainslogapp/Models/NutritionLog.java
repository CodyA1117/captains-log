package com.minderall.captainslogapp.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Removed String userId, will use the User object relationship
    // private String userId;

    @ManyToOne(fetch = FetchType.LAZY) // Good practice for performance
    @JoinColumn(name = "app_user_id", nullable = false) // Name of the foreign key column
    @JsonBackReference("user-nutritionlogs") // To prevent serialization loops
    private User user; // Link to your app's User entity

    private LocalDate date;
    private Double protein; // Use Double for precision
    private Double carbs;
    private Double fat;
    private Double calories; // Often useful to store total calories
    private Double bodyWeight; // Assuming this is in user's preferred unit (e.g., kg or lbs)
    private Double waterIntakeOz;

    @Lob // For potentially large JSON string
    @Column(columnDefinition = "TEXT")
    private String mealsJson; // Store raw food items if needed

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public String getMealsJson() { return mealsJson; }
    public void setMealsJson(String mealsJson) { this.mealsJson = mealsJson; }
}