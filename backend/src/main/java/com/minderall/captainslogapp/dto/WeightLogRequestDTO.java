package com.minderall.captainslogapp.dto;

import java.time.LocalDateTime;

public class WeightLogRequestDTO {
    private Double weight; // Assuming a consistent unit like kg or lbs
    private String unit; // e.g., "kg", "lbs"
    private LocalDateTime loggedAt;

    // Getters and Setters
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
}