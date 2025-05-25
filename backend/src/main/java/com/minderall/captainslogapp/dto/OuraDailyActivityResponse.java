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
public class OuraDailyActivityResponse {
    private List<ActivityData> data;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityData {
        private String id; // Oura's internal ID for the data point
        private Integer score;
        @JsonProperty("active_calories")
        private Integer activeCalories;
        @JsonProperty("day") // Oura API often uses 'day' for the date
        private LocalDate date;
        // Add other relevant fields from Oura's daily activity response
        // e.g., steps, total_calories, equivalent_calories_walking, etc.
    }
}