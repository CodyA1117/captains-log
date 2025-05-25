package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.MessageResponse;
import com.minderall.captainslogapp.dto.OuraDataResponse; // We created this before
import com.minderall.captainslogapp.dto.TodayOuraResponse; // We created this before
import com.minderall.captainslogapp.exception.ResourceNotFoundException;
import com.minderall.captainslogapp.Repositories.OuraDataRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import com.minderall.captainslogapp.Security.UserDetailsImpl;
import com.minderall.captainslogapp.Services.OuraService;
import com.minderall.captainslogapp.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/oura")
public class OuraController {

    private static final Logger logger = LoggerFactory.getLogger(OuraController.class);

    @Autowired
    private OuraService ouraService;

    @Autowired
    private OuraDataRepository ouraDataRepository;

    @Autowired
    private UserRepository userRepository; // For fetching user if needed

    @Autowired
    private UserService userService;


    // Endpoint to manually trigger fetching and storing of today's Oura data for the logged-in user
    @PostMapping("/fetch-today")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> fetchAndStoreTodaysOuraData(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }
        try {
            // Check if user has valid Oura token before attempting to fetch
            Optional<User> userOpt = userService.findUserWithValidOuraToken(userDetails.getId());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Error: Oura account not connected or token invalid. Please connect/reconnect Oura."));
            }

            ouraService.fetchAndStoreTodayOuraData(userDetails.getId());
            // The service method is asynchronous (returns void but triggers reactive chain).
            // For a simple trigger, returning an "accepted" or "ok" immediately is fine.
            // If you need to wait for completion and return the result, the service method would need to return a Mono,
            // and this controller method would block or return the Mono.
            return ResponseEntity.ok(new MessageResponse("Successfully triggered Oura data fetch for today. Data will be updated shortly."));
        } catch (Exception e) {
            logger.error("Error triggering Oura data fetch for user {}: {}", userDetails.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error triggering Oura data fetch: " + e.getMessage()));
        }
    }

    // Endpoint to get today's Oura data for the logged-in user (from our database)
    @GetMapping("/today")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getTodaysOuraData(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        LocalDate today = LocalDate.now();
        Optional<OuraData> ouraDataOptional = ouraDataRepository.findByUserAndDataDate(user, today);

        if (ouraDataOptional.isEmpty()) {
            // Optionally, you could trigger a fetch here if no data is found for today,
            // but that might make this GET request slow.
            // For now, just return a message or a 404.
            // Or return a default/empty TodayOuraResponse
            logger.info("No Oura data found in DB for user {} on {}", user.getId(), today);
            return ResponseEntity.ok(new TodayOuraResponse(today, null, null, null)); // Return empty scores
            // Alternative: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("No Oura data found for today. Try fetching it first."));
        }

        OuraData ouraData = ouraDataOptional.get();
        TodayOuraResponse responseDto = new TodayOuraResponse(
                ouraData.getDataDate(),
                ouraData.getReadinessScore(),
                ouraData.getSleepScore(),
                ouraData.getActivityScore()
        );
        return ResponseEntity.ok(responseDto);
    }

    // Optional: Endpoint to get Oura data for a specific date from your database
    @GetMapping("/date/{dateString}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getOuraDataForDate(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable String dateString) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        LocalDate date;
        try {
            date = LocalDate.parse(dateString); // Expects yyyy-MM-dd
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid date format. Please use yyyy-MM-dd."));
        }

        Optional<OuraData> ouraDataOptional = ouraDataRepository.findByUserAndDataDate(user, date);
        if (ouraDataOptional.isEmpty()) {
            return ResponseEntity.ok(new TodayOuraResponse(date, null, null, null));
        }
        OuraData ouraData = ouraDataOptional.get();
        TodayOuraResponse responseDto = new TodayOuraResponse(
                ouraData.getDataDate(),
                ouraData.getReadinessScore(),
                ouraData.getSleepScore(),
                ouraData.getActivityScore()
        );
        return ResponseEntity.ok(responseDto);
    }
}