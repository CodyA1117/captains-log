package com.minderall.captainslogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayOuraResponse {
    private LocalDate date;
    private Integer readinessScore;
    private Integer sleepScore;
    private Integer activityScore;
}