package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.UserResponse;
import com.minderall.captainslogapp.exception.ResourceNotFoundException;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserDtoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        // Map User entity to UserResponse DTO
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }

    @Override
    @Transactional
    public void saveOuraTokens(Long userId, String accessToken, String refreshToken, Long expiresInSeconds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setOuraOAuthToken(accessToken);
        user.setOuraRefreshToken(refreshToken);
        if (expiresInSeconds != null) {
            user.setTokenExpiry(LocalDateTime.now().plusSeconds(expiresInSeconds));
        } else {
            // Handle case where expiry is not provided or is indefinite (less common for OAuth2)
            // For now, let's assume it will be provided. If not, set a far future date or null.
            user.setTokenExpiry(null);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserWithValidOuraToken(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getOuraOAuthToken() != null &&
                    (user.getTokenExpiry() == null || user.getTokenExpiry().isAfter(LocalDateTime.now()))) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

}