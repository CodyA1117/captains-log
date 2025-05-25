package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.UserResponse; // We created this DTO earlier

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    UserResponse getUserDtoById(Long id); // To get a user representation safe for API responses

    // Methods for Oura token management linked to a user
    void saveOuraTokens(Long userId, String accessToken, String refreshToken, Long expiresInSeconds);

    Optional<User> findUserWithValidOuraToken(Long userId); // Could be useful to check before making API calls

    // Potentially other methods like:
    // User updateUserProfile(Long userId, UserProfileUpdateRequestDto updateRequest);
    // void changePassword(Long userId, String oldPassword, String newPassword);
}