package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.OuraData;
import com.minderall.captainslogapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List; // << CHANGED: Import List
// import java.util.Optional; // Optional is no longer needed for this method's return type

@Repository
public interface OuraDataRepository extends JpaRepository<OuraData, Long> { // Assuming Long is the ID type for OuraData
    // VVV CHANGED: Return type is now List<OuraData>
    List<OuraData> findByUserAndDate(User user, LocalDate date);
}
