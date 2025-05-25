package com.minderall.captainslogapp.Repositories;

import com.minderall.captainslogapp.Models.Entry;
import com.minderall.captainslogapp.Models.User;
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
public interface EntryRepository extends JpaRepository<Entry, Long> {

    // Find entries for a specific user, ordered by entry date descending
    Page<Entry> findByUserOrderByEntryDateDescCreatedAtDesc(User user, Pageable pageable);

    Optional<Entry> findByIdAndUser(Long id, User user);

    // For dashboard charts - fetching entries within a date range for a user
    List<Entry> findByUserAndEntryDateBetweenOrderByEntryDateAsc(User user, LocalDate startDate, LocalDate endDate);

    // Example: Find entries for a user on a specific date
    List<Entry> findByUserAndEntryDate(User user, LocalDate entryDate);

    // Query to fetch recent entries for the dashboard (e.g., last N entries)
    // This example uses findByUserOrderByEntryDateDesc, and you can limit with Pageable
    // Alternatively, if you always want a fixed number, you could use a native query or a more specific derived query if needed.
}