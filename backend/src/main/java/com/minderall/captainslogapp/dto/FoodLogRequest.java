package com.minderall.captainslogapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FoodLogRequest {
    @NotNull(message = "Log date cannot be null")
    private LocalDate logDate;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @PositiveOrZero(message = "Calories must be zero or positive")
    private Integer calories; // Optional

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes; // Optional
}