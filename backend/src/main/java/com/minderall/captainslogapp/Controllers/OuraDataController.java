package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Models.OuraData; // Keep this
import com.minderall.captainslogapp.Services.OuraDataService;
import com.minderall.captainslogapp.dto.OuraDataDTO; // Import the DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oura")
public class OuraDataController {

    @Autowired
    private OuraDataService ouraDataService;

    // ... (other sync methods remain the same) ...
    @GetMapping("/sync-today")
    public ResponseEntity<String> syncTodayData(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayReadiness(email);
        // Small improvement: check if data was actually saved, but for now, this is fine
        return ResponseEntity.ok("Readiness score sync attempted.");
    }

    @GetMapping("/sync-sleep")
    public ResponseEntity<String> syncTodaySleep(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodaySleepScore(email);
        return ResponseEntity.ok("Sleep score sync attempted.");
    }

    @GetMapping("/sync-activity")
    public ResponseEntity<String> syncTodayActivity(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayActivityScore(email);
        return ResponseEntity.ok("Activity score sync attempted.");
    }

    @GetMapping("/sync-heart-rate")
    public ResponseEntity<String> syncTodayHeartRate(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayHeartRate(email);
        return ResponseEntity.ok("Heart rate sync attempted.");
    }

    @GetMapping("/sync-stress")
    public ResponseEntity<String> syncTodayStress(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayStressScore(email); // Assuming you want to try syncing stress
        return ResponseEntity.ok("Stress score sync attempted.");
    }


    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping("/today")
    public ResponseEntity<?> getTodayOuraData(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        System.out.println("📡 Requesting Oura data for user: " + email);

        return ouraDataService.getTodayOuraData(email)
                .map(dataEntity -> { // dataEntity is OuraData
                    System.out.println("✅ Found data entity: " + dataEntity);
                    // Map entity to DTO
                    OuraDataDTO dataDTO = new OuraDataDTO(
                            dataEntity.getDate(),
                            dataEntity.getReadinessScore(),
                            dataEntity.getSleepScore(),
                            dataEntity.getActivityScore(),
                            dataEntity.getHeartRate(),
                            dataEntity.getStressScore()
                    );
                    System.out.println("✅ Returning DTO: " + dataDTO);
                    return ResponseEntity.ok(dataDTO); // Return DTO
                })
                .orElseGet(() -> {
                    System.out.println("❌ No data found for today in DB to return.");
                    return ResponseEntity.noContent().build();
                });
    }
}