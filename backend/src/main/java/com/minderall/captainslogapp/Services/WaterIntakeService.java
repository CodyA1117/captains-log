package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.dto.WaterIntakeRequest;
import com.minderall.captainslogapp.dto.WaterIntakeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WaterIntakeService {

    WaterIntakeResponse createWaterIntake(Long userId, WaterIntakeRequest waterIntakeRequest);

    WaterIntakeResponse getWaterIntakeById(Long userId, Long waterIntakeId);

    // Get all water intakes for a user on a specific date (as a user might log multiple times)
    List<WaterIntakeResponse> getWaterIntakesByUserAndDate(Long userId, LocalDate date);

    // Get total water intake for a user on a specific date
    Integer getTotalWaterIntakeByUserAndDate(Long userId, LocalDate date);

    Page<WaterIntakeResponse> getAllWaterIntakesByUser(Long userId, Pageable pageable); // For viewing history

    WaterIntakeResponse updateWaterIntake(Long userId, Long waterIntakeId, WaterIntakeRequest waterIntakeRequest);

    void deleteWaterIntake(Long userId, Long waterIntakeId);

    List<WaterIntakeResponse> getWaterIntakesByUserAndDateRange(Long userId, String startDateStr, String endDateStr);
}