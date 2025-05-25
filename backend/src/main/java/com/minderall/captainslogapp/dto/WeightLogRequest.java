package com.minderall.captainslogapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WeightLogRequest {
    @NotNull(message = "Log date cannot be null")
    private LocalDate logDate;

    @NotNull(message = "Weight in kg cannot be null")
    @Positive(message = "Weight in kg must be positive")
    private Double weightKg;
}