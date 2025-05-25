package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.Models.WaterIntake;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterIntakeRepository extends JpaRepository<WaterIntake, Long> {

    Page<WaterIntake> findByUserOrderByLogDateDescCreatedAtDesc(User user, Pageable pageable);

    Optional<WaterIntake> findByIdAndUser(Long id, User user);

    // Find all water intakes for a user on a specific date
    List<WaterIntake> findByUserAndLogDate(User user, LocalDate logDate);

    // Sum water intake for a user on a specific date
    @Query("SELECT SUM(wi.amountMl) FROM WaterIntake wi WHERE wi.user = :user AND wi.logDate = :logDate")
    Optional<Integer> sumAmountMlByUserAndLogDate(@Param("user") User user, @Param("logDate") LocalDate logDate);

    List<WaterIntake> findByUserAndLogDateBetweenOrderByLogDateAsc(User user, LocalDate startDate, LocalDate endDate);
}