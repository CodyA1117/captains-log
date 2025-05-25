package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.dto.MessageResponse;
import com.minderall.captainslogapp.dto.FoodLogRequest;
import com.minderall.captainslogapp.dto.FoodLogResponse;
import com.minderall.captainslogapp.Security.UserDetailsImpl;
import com.minderall.captainslogapp.Services.FoodLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs/food")
public class FoodLogController {

    @Autowired
    private FoodLogService foodLogService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FoodLogResponse> createFoodLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @Valid @RequestBody FoodLogRequest foodLogRequest) {
        FoodLogResponse createdFoodLog = foodLogService.createFoodLog(userDetails.getId(), foodLogRequest);
        return new ResponseEntity<>(createdFoodLog, HttpStatus.CREATED);
    }

    @GetMapping("/{foodLogId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FoodLogResponse> getFoodLogById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @PathVariable Long foodLogId) {
        FoodLogResponse foodLog = foodLogService.getFoodLogById(userDetails.getId(), foodLogId);
        return ResponseEntity.ok(foodLog);
    }

    // Get all food logs for the user (paginated)
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<FoodLogResponse>> getAllFoodLogsByUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "logDate,desc") String[] sort) {

        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<FoodLogResponse> foodLogs = foodLogService.getAllFoodLogsByUser(userDetails.getId(), pageable);
        return ResponseEntity.ok(foodLogs);
    }

    // Get all food logs for the user for a specific date
    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FoodLogResponse>> getFoodLogsByUserAndDate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<FoodLogResponse> foodLogs = foodLogService.getFoodLogsByUserAndDate(userDetails.getId(), date);
        return ResponseEntity.ok(foodLogs);
    }

    @GetMapping("/range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FoodLogResponse>> getFoodLogsByDateRange(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String startDate, // Expects YYYY-MM-DD
            @RequestParam String endDate) {  // Expects YYYY-MM-DD
        List<FoodLogResponse> foodLogs = foodLogService.getFoodLogsByUserAndDateRange(userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(foodLogs);
    }


    @PutMapping("/{foodLogId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<FoodLogResponse> updateFoodLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable Long foodLogId,
                                                         @Valid @RequestBody FoodLogRequest foodLogRequest) {
        FoodLogResponse updatedFoodLog = foodLogService.updateFoodLog(userDetails.getId(), foodLogId, foodLogRequest);
        return ResponseEntity.ok(updatedFoodLog);
    }

    @DeleteMapping("/{foodLogId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> deleteFoodLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable Long foodLogId) {
        foodLogService.deleteFoodLog(userDetails.getId(), foodLogId);
        return ResponseEntity.ok(new MessageResponse("Food log deleted successfully"));
    }
}