package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.dto.EntryRequest;
import com.minderall.captainslogapp.dto.EntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EntryService {

    EntryResponse createEntry(Long userId, EntryRequest entryRequest);

    EntryResponse getEntryById(Long userId, Long entryId);

    Page<EntryResponse> getAllEntriesByUser(Long userId, Pageable pageable);

    EntryResponse updateEntry(Long userId, Long entryId, EntryRequest entryRequest);

    void deleteEntry(Long userId, Long entryId);

    // For dashboard charts - fetching entries within a date range for a user (could be more specialized later)
    List<EntryResponse> getEntriesByUserAndDateRange(Long userId, String startDateStr, String endDateStr);

    // Optional: For the "30-Day Starter Runway"
    // List<String> getReflectionPrompts(int count);
    // String getRandomReflectionPrompt();
}