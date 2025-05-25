package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OuraDataRepository extends JpaRepository<OuraData, Long> {

    Optional<OuraData> findByUserAndDataDate(User user, LocalDate dataDate);

    List<OuraData> findByUserAndDataDateBetweenOrderByDataDateAsc(User user, LocalDate startDate, LocalDate endDate);

    // Find the most recent OuraData entry for a user (optional, if needed for a quick "today's" view fallback)
    Optional<OuraData> findTopByUserOrderByDataDateDesc(User user);
}