package com.minderall.captainslogapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OuraDataResponse {
    private Long id;
    private Long userId;
    private LocalDate dataDate;
    private Integer readinessScore;
    private Integer sleepScore;
    private Integer activityScore;
    private LocalDateTime createdAt;
}