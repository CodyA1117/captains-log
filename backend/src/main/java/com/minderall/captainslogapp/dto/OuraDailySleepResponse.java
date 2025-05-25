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
public class OuraDailySleepResponse {
    private List<SleepData> data;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SleepData {
        private String id;
        private Integer score;
        @JsonProperty("total_sleep_duration") // in seconds
        private Integer totalSleepDuration;
        @JsonProperty("day")
        private LocalDate date;
        // Add other relevant fields: deep_sleep_duration, rem_sleep_duration, efficiency, etc.
    }
}