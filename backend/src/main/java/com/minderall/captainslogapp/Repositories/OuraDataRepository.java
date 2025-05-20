package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OuraDataRepository extends JpaRepository<OuraData, Long> {
    Optional<OuraData> findByUserAndDate(User user, LocalDate date);


}
