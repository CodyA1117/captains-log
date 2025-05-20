package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Services.OuraDataService;
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

    // GET endpoint to sync today's readiness score
    @GetMapping("/sync-today")
    public ResponseEntity<String> syncTodayData(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayReadiness(email);
        return ResponseEntity.ok("Readiness score synced successfully.");
    }

    // GET endpoint to sync today's sleep score
    @GetMapping("/sync-sleep")
    public ResponseEntity<String> syncTodaySleep(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodaySleepScore(email);
        return ResponseEntity.ok("Sleep score synced successfully.");
    }

    // GET endpoint to sync today's activity score
    @GetMapping("/sync-activity")
    public ResponseEntity<String> syncTodayActivity(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayActivityScore(email);
        return ResponseEntity.ok("Activity score synced successfully.");
    }

    // GET endpoint to sync today's heart rate
    @GetMapping("/sync-heart-rate")
    public ResponseEntity<String> syncTodayHeartRate(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ouraDataService.fetchAndSaveTodayHeartRate(email);
        return ResponseEntity.ok("Heart rate synced successfully.");
    }

    // (Optional) GET endpoint for stress — Oura does not expose stress directly.
    @GetMapping("/sync-stress")
    public ResponseEntity<String> syncTodayStress(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        // You can implement this later if you're calculating stress from other metrics
        return ResponseEntity.ok("Stress score syncing not implemented yet.");
    }
}

