package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.dto.MessageResponse;
import com.minderall.captainslogapp.dto.WaterIntakeRequest;
import com.minderall.captainslogapp.dto.WaterIntakeResponse;
import com.minderall.captainslogapp.Security.UserDetailsImpl;
import com.minderall.captainslogapp.Services.WaterIntakeService;
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
@RequestMapping("/api/logs/water")
public class WaterIntakeController {

    @Autowired
    private WaterIntakeService waterIntakeService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WaterIntakeResponse> createWaterIntake(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                 @Valid @RequestBody WaterIntakeRequest waterIntakeRequest) {
        WaterIntakeResponse createdWaterIntake = waterIntakeService.createWaterIntake(userDetails.getId(), waterIntakeRequest);
        return new ResponseEntity<>(createdWaterIntake, HttpStatus.CREATED);
    }

    @GetMapping("/{waterIntakeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WaterIntakeResponse> getWaterIntakeById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                  @PathVariable Long waterIntakeId) {
        WaterIntakeResponse waterIntake = waterIntakeService.getWaterIntakeById(userDetails.getId(), waterIntakeId);
        return ResponseEntity.ok(waterIntake);
    }

    // Get all water intake entries for the user for a specific date
    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WaterIntakeResponse>> getWaterIntakesByUserAndDate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<WaterIntakeResponse> waterIntakes = waterIntakeService.getWaterIntakesByUserAndDate(userDetails.getId(), date);
        return ResponseEntity.ok(waterIntakes);
    }

    // Get total water intake for the user for a specific date
    @GetMapping("/date/{date}/total")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Integer> getTotalWaterIntakeByUserAndDate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Integer totalIntake = waterIntakeService.getTotalWaterIntakeByUserAndDate(userDetails.getId(), date);
        return ResponseEntity.ok(totalIntake);
    }

    // Get all water intake logs for the user (paginated history)
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<WaterIntakeResponse>> getAllWaterIntakesByUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "logDate,desc") String[] sort) {

        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<WaterIntakeResponse> waterIntakes = waterIntakeService.getAllWaterIntakesByUser(userDetails.getId(), pageable);
        return ResponseEntity.ok(waterIntakes);
    }

    @PutMapping("/{waterIntakeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WaterIntakeResponse> updateWaterIntake(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                 @PathVariable Long waterIntakeId,
                                                                 @Valid @RequestBody WaterIntakeRequest waterIntakeRequest) {
        WaterIntakeResponse updatedWaterIntake = waterIntakeService.updateWaterIntake(userDetails.getId(), waterIntakeId, waterIntakeRequest);
        return ResponseEntity.ok(updatedWaterIntake);
    }

    @DeleteMapping("/{waterIntakeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> deleteWaterIntake(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @PathVariable Long waterIntakeId) {
        waterIntakeService.deleteWaterIntake(userDetails.getId(), waterIntakeId);
        return ResponseEntity.ok(new MessageResponse("Water intake log deleted successfully"));
    }

    @GetMapping("/range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WaterIntakeResponse>> getWaterIntakesByDateRange(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String startDate, // Expects YYYY-MM-DD
            @RequestParam String endDate) {  // Expects YYYY-MM-DD
        List<WaterIntakeResponse> waterIntakes = waterIntakeService.getWaterIntakesByUserAndDateRange(userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(waterIntakes);
    }
}