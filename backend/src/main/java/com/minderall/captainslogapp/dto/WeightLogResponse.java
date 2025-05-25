package com.minderall.captainslogapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WeightLogResponse {
    private Long id;
    private Long userId;
    private LocalDate logDate;
    private Double weightKg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}