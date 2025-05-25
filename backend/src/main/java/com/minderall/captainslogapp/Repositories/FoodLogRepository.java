package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.FoodLog;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    Page<FoodLog> findByUserOrderByLogDateDescCreatedAtDesc(User user, Pageable pageable);

    Optional<FoodLog> findByIdAndUser(Long id, User user);

    List<FoodLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<FoodLog> findByUserAndLogDateBetweenOrderByLogDateAsc(User user, LocalDate startDate, LocalDate endDate);
}