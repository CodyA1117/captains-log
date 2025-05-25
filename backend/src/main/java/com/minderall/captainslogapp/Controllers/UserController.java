package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.dto.UserResponse;
import com.minderall.captainslogapp.Security.UserDetailsImpl;
import com.minderall.captainslogapp.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Endpoint to get the current logged-in user's profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // Ensure only authenticated users can access
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // userDetails is directly injected by Spring Security, containing the authenticated user's info
        if (userDetails == null) {
            // This case should ideally be handled by security filters if the endpoint is protected
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        // We use the ID from UserDetailsImpl to fetch the full UserResponse DTO
        // This ensures we are using the DTO mapping defined in UserService
        UserResponse userResponse = userService.getUserDtoById(userDetails.getId());
        return ResponseEntity.ok(userResponse);
    }

    // You can add other user-specific endpoints here later, e.g.:
    // @PutMapping("/me/profile")
    // public ResponseEntity<?> updateUserProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody UserProfileUpdateRequestDto updateRequest) { ... }

    // @PostMapping("/me/change-password")
    // public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody ChangePasswordRequestDto changePasswordRequest) { ... }

}