package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.Entry;
import com.minderall.captainslogapp.Models.User; // Import User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    // Find entries by user, ordered by date descending (newest first)
    List<Entry> findByUserOrderByDateDescIdDesc(User user); // Changed from findByUserId

    // Find a specific entry by its ID and User (for authorization checks)
    Optional<Entry> findByIdAndUser(Long id, User user);
}