package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Models.WaterIntake;
import com.minderall.captainslogapp.dto.WaterIntakeRequest;
import com.minderall.captainslogapp.dto.WaterIntakeResponse;
import com.minderall.captainslogapp.exception.ResourceNotFoundException;
import com.minderall.captainslogapp.Repositories.UserRepository;
import com.minderall.captainslogapp.Repositories.WaterIntakeRepository;
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
public class WaterIntakeServiceImpl implements WaterIntakeService {

    @Autowired
    private WaterIntakeRepository waterIntakeRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to convert Entity to DTO
    private WaterIntakeResponse mapToDto(WaterIntake waterIntake) {
        WaterIntakeResponse dto = new WaterIntakeResponse();
        dto.setId(waterIntake.getId());
        dto.setUserId(waterIntake.getUser().getId());
        dto.setLogDate(waterIntake.getLogDate());
        dto.setAmountMl(waterIntake.getAmountMl());
        dto.setCreatedAt(waterIntake.getCreatedAt());
        dto.setUpdatedAt(waterIntake.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public WaterIntakeResponse createWaterIntake(Long userId, WaterIntakeRequest waterIntakeRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        WaterIntake waterIntake = WaterIntake.builder()
                .user(user)
                .logDate(waterIntakeRequest.getLogDate())
                .amountMl(waterIntakeRequest.getAmountMl())
                .build();

        WaterIntake savedWaterIntake = waterIntakeRepository.save(waterIntake);
        return mapToDto(savedWaterIntake);
    }

    @Override
    @Transactional(readOnly = true)
    public WaterIntakeResponse getWaterIntakeById(Long userId, Long waterIntakeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        WaterIntake waterIntake = waterIntakeRepository.findByIdAndUser(waterIntakeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("WaterIntake", "id", waterIntakeId + " for user " + userId));
        return mapToDto(waterIntake);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterIntakeResponse> getWaterIntakesByUserAndDate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        List<WaterIntake> waterIntakes = waterIntakeRepository.findByUserAndLogDate(user, date);
        return waterIntakes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalWaterIntakeByUserAndDate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return waterIntakeRepository.sumAmountMlByUserAndLogDate(user, date).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WaterIntakeResponse> getAllWaterIntakesByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Page<WaterIntake> waterIntakesPage = waterIntakeRepository.findByUserOrderByLogDateDescCreatedAtDesc(user, pageable);
        return waterIntakesPage.map(this::mapToDto);
    }

    @Override
    @Transactional
    public WaterIntakeResponse updateWaterIntake(Long userId, Long waterIntakeId, WaterIntakeRequest waterIntakeRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        WaterIntake waterIntake = waterIntakeRepository.findByIdAndUser(waterIntakeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("WaterIntake", "id", waterIntakeId + " for user " + userId));

        waterIntake.setLogDate(waterIntakeRequest.getLogDate());
        waterIntake.setAmountMl(waterIntakeRequest.getAmountMl());

        WaterIntake updatedWaterIntake = waterIntakeRepository.save(waterIntake);
        return mapToDto(updatedWaterIntake);
    }

    @Override
    @Transactional
    public void deleteWaterIntake(Long userId, Long waterIntakeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        WaterIntake waterIntake = waterIntakeRepository.findByIdAndUser(waterIntakeId, user)
                .orElseThrow(() -> new ResourceNotFoundException("WaterIntake", "id", waterIntakeId + " for user " + userId));
        waterIntakeRepository.delete(waterIntake);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterIntakeResponse> getWaterIntakesByUserAndDateRange(Long userId, String startDateStr, String endDateStr) {
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

        List<WaterIntake> waterIntakes = waterIntakeRepository.findByUserAndLogDateBetweenOrderByLogDateAsc(user, startDate, endDate);
        return waterIntakes.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}