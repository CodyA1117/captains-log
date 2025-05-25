package com.minderall.captainslogapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FoodLogResponse {
    private Long id;
    private Long userId;
    private LocalDate logDate;
    private String description;
    private Integer calories;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}