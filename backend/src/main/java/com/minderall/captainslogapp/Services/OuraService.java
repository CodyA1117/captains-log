package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.OuraDailyActivityResponse;
import com.minderall.captainslogapp.dto.OuraDailyReadinessResponse;
import com.minderall.captainslogapp.dto.OuraDailySleepResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface OuraService {

    // Methods to fetch data from Oura API for a given user and date
    Mono<OuraDailyActivityResponse.ActivityData> getDailyActivityForDate(User user, LocalDate date);
    Mono<OuraDailySleepResponse.SleepData> getDailySleepForDate(User user, LocalDate date);
    Mono<OuraDailyReadinessResponse.ReadinessData> getDailyReadinessForDate(User user, LocalDate date);

    // Method to fetch and store today's Oura data for a user
    // This would internally call the above methods for today's date
    // and then save the relevant scores to our OuraData entity.
    void fetchAndStoreTodayOuraData(Long userId);

    // Potentially methods for date ranges
    // Mono<OuraDailyActivityResponse> getDailyActivityForDateRange(User user, LocalDate startDate, LocalDate endDate);
}