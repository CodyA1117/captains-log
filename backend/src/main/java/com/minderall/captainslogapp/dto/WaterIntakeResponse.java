package com.minderall.captainslogapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WaterIntakeResponse {
    private Long id;
    private Long userId;
    private LocalDate logDate;
    private Integer amountMl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}