package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.OuraToken;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OuraTokenRepository extends JpaRepository<OuraToken, Long> {
    Optional<OuraToken> findByUser(User user);
    Optional<OuraToken> findByUserEmail(String email);
}
