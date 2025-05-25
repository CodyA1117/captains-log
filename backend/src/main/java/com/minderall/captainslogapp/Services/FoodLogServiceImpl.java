package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.FoodLog;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.FoodLogRequest;
import com.minderall.captainslogapp.dto.FoodLogResponse;
import com.minderall.captainslogapp.exception.ResourceNotFoundException;
import com.minderall.captainslogapp.Repositories.FoodLogRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodLogServiceImpl implements FoodLogService {

    @Autowired
    private FoodLogRepository foodLogRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to convert Entity to DTO
    private FoodLogResponse mapToDto(FoodLog foodLog) {
        FoodLogResponse dto = new FoodLogResponse();
        dto.setId(foodLog.getId());
        dto.setUserId(foodLog.getUser().getId());
        dto.setLogDate(foodLog.getLogDate());
        dto.setDescription(foodLog.getDescription());
        dto.setCalories(foodLog.getCalories());
        dto.setNotes(foodLog.getNotes());
        dto.setCreatedAt(foodLog.getCreatedAt());
        dto.setUpdatedAt(foodLog.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public FoodLogResponse createFoodLog(Long userId, FoodLogRequest foodLogRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        FoodLog foodLog = FoodLog.builder()
                .user(user)
                .logDate(foodLogRequest.getLogDate()) // Assumes logDate is always provided in request
                .description(foodLogRequest.getDescription())
                .calories(foodLogRequest.getCalories())
                .notes(foodLogRequest.getNotes())
                .build();

        FoodLog savedFoodLog = foodLogRepository.save(foodLog);
        return mapToDto(savedFoodLog);
    }

    @Override
    @Transactional(readOnly = true)
    public FoodLogResponse getFoodLogById(Long userId, Long foodLogId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        FoodLog foodLog = foodLogRepository.findByIdAndUser(foodLogId, user)
                .orElseThrow(() -> new ResourceNotFoundException("FoodLog", "id", foodLogId + " for user " + userId));
        return mapToDto(foodLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FoodLogResponse> getAllFoodLogsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Page<FoodLog> foodLogsPage = foodLogRepository.findByUserOrderByLogDateDescCreatedAtDesc(user, pageable);
        return foodLogsPage.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodLogResponse> getFoodLogsByUserAndDate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        List<FoodLog> foodLogs = foodLogRepository.findByUserAndLogDate(user, date);
        return foodLogs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FoodLogResponse updateFoodLog(Long userId, Long foodLogId, FoodLogRequest foodLogRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        FoodLog foodLog = foodLogRepository.findByIdAndUser(foodLogId, user)
                .orElseThrow(() -> new ResourceNotFoundException("FoodLog", "id", foodLogId + " for user " + userId));

        foodLog.setLogDate(foodLogRequest.getLogDate());
        foodLog.setDescription(foodLogRequest.getDescription());
        foodLog.setCalories(foodLogRequest.getCalories());
        foodLog.setNotes(foodLogRequest.getNotes());

        FoodLog updatedFoodLog = foodLogRepository.save(foodLog);
        return mapToDto(updatedFoodLog);
    }

    @Override
    @Transactional
    public void deleteFoodLog(Long userId, Long foodLogId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        FoodLog foodLog = foodLogRepository.findByIdAndUser(foodLogId, user)
                .orElseThrow(() -> new ResourceNotFoundException("FoodLog", "id", foodLogId + " for user " + userId));
        foodLogRepository.delete(foodLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodLogResponse> getFoodLogsByUserAndDateRange(Long userId, String startDateStr, String endDateStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD.", e);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<FoodLog> foodLogs = foodLogRepository.findByUserAndLogDateBetweenOrderByLogDateAsc(user, startDate, endDate);
        return foodLogs.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}