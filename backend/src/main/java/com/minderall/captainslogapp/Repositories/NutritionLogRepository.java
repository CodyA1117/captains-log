package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.NutritionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {
    Optional<NutritionLog> findByUserIdAndDate(String userId, LocalDate date);
}
