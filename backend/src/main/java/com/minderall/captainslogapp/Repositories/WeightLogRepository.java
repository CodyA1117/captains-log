package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Models.WeightLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {

    Page<WeightLog> findByUserOrderByLogDateDescCreatedAtDesc(User user, Pageable pageable);

    Optional<WeightLog> findByIdAndUser(Long id, User user);

    Optional<WeightLog> findByUserAndLogDate(User user, LocalDate logDate);

    List<WeightLog> findByUserAndLogDateBetweenOrderByLogDateAsc(User user, LocalDate startDate, LocalDate endDate);

    Optional<WeightLog> findTopByUserOrderByLogDateDesc(User user);
}