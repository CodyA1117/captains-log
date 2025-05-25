package com.minderall.captainslogapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OuraDailyReadinessResponse {
    private List<ReadinessData> data;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReadinessData {
        private String id;
        private Integer score;
        @JsonProperty("day")
        private LocalDate date;
        // Add other relevant fields: temperature_deviation, resting_heart_rate, etc.
    }
}