package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.dto.FoodLogRequest;
import com.minderall.captainslogapp.dto.FoodLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface FoodLogService {

    FoodLogResponse createFoodLog(Long userId, FoodLogRequest foodLogRequest);

    FoodLogResponse getFoodLogById(Long userId, Long foodLogId);

    Page<FoodLogResponse> getAllFoodLogsByUser(Long userId, Pageable pageable);

    List<FoodLogResponse> getFoodLogsByUserAndDate(Long userId, LocalDate date);

    FoodLogResponse updateFoodLog(Long userId, Long foodLogId, FoodLogRequest foodLogRequest);

    void deleteFoodLog(Long userId, Long foodLogId);

    List<FoodLogResponse> getFoodLogsByUserAndDateRange(Long userId, String startDateStr, String endDateStr);
}