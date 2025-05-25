package com.minderall.captainslogapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WaterIntakeRequest {
    @NotNull(message = "Log date cannot be null")
    private LocalDate logDate;

    @NotNull(message = "Amount in mL cannot be null")
    @Positive(message = "Amount in mL must be positive")
    private Integer amountMl;
}