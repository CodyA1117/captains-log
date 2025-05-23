package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.NutritionLog;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {
    Optional<NutritionLog> findByUserAndDate(User user, LocalDate date);
    List<NutritionLog> findByUserOrderByDateDesc(User user);
}