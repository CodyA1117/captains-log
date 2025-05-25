package com.minderall.captainslogapp.dto;

import java.time.LocalDateTime;

public class WaterLogRequestDTO {
    private Double amountOz;
    private LocalDateTime loggedAt;

    // Getters and Setters
    public Double getAmountOz() { return amountOz; }
    public void setAmountOz(Double amountOz) { this.amountOz = amountOz; }
    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
}