package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.Entry;
import com.minderall.captainslogapp.Models.User;
import com.minderall.captainslogapp.dto.EntryRequest;
import com.minderall.captainslogapp.dto.EntryResponse;
import com.minderall.captainslogapp.exception.ResourceNotFoundException;
import com.minderall.captainslogapp.exception.UnauthorizedActionException;
import com.minderall.captainslogapp.Repositories.EntryRepository;
import com.minderall.captainslogapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntryServiceImpl implements EntryService {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to convert Entity to DTO
    private EntryResponse mapToDto(Entry entry) {
        EntryResponse dto = new EntryResponse();
        dto.setId(entry.getId());
        dto.setUserId(entry.getUser().getId());
        dto.setEntryDate(entry.getEntryDate());
        dto.setTitle(entry.getTitle());
        dto.setMood(entry.getMood());
        dto.setFocus(entry.getFocus());
        dto.setEnergy(entry.getEnergy());
        dto.setConfidence(entry.getConfidence());
        dto.setDrive(entry.getDrive());
        dto.setNote(entry.getNote());
        dto.setPromptUsed(entry.getPromptUsed());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());
        return dto;
    }

    @Override
    @Transactional
    public EntryResponse createEntry(Long userId, EntryRequest entryRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Entry entry = Entry.builder()
                .user(user)
                .entryDate(entryRequest.getEntryDate() != null ? entryRequest.getEntryDate() : LocalDate.now())
                .title(entryRequest.getTitle())
                .mood(entryRequest.getMood())
                .focus(entryRequest.getFocus())
                .energy(entryRequest.getEnergy())
                .confidence(entryRequest.getConfidence())
                .drive(entryRequest.getDrive())
                .note(entryRequest.getNote())
                .promptUsed(entryRequest.getPromptUsed())
                // createdAt and updatedAt will be set by @CreationTimestamp and @UpdateTimestamp
                .build();

        Entry savedEntry = entryRepository.save(entry);
        return mapToDto(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public EntryResponse getEntryById(Long userId, Long entryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Entry entry = entryRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Entry", "id", entryId + " for user " + userId));
        return mapToDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EntryResponse> getAllEntriesByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Page<Entry> entriesPage = entryRepository.findByUserOrderByEntryDateDescCreatedAtDesc(user, pageable);
        return entriesPage.map(this::mapToDto);
    }

    @Override
    @Transactional
    public EntryResponse updateEntry(Long userId, Long entryId, EntryRequest entryRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Entry entry = entryRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Entry", "id", entryId + " for user " + userId));

        entry.setTitle(entryRequest.getTitle());
        if (entryRequest.getEntryDate() != null) {
            entry.setEntryDate(entryRequest.getEntryDate());
        }
        entry.setMood(entryRequest.getMood());
        entry.setFocus(entryRequest.getFocus());
        entry.setEnergy(entryRequest.getEnergy());
        entry.setConfidence(entryRequest.getConfidence());
        entry.setDrive(entryRequest.getDrive());
        entry.setNote(entryRequest.getNote());
        entry.setPromptUsed(entryRequest.getPromptUsed());
        // updatedAt will be updated automatically by @UpdateTimestamp

        Entry updatedEntry = entryRepository.save(entry);
        return mapToDto(updatedEntry);
    }

    @Override
    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Entry entry = entryRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Entry", "id", entryId + " for user " + userId));
        entryRepository.delete(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntryResponse> getEntriesByUserAndDateRange(Long userId, String startDateStr, String endDateStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr);
            endDate = LocalDate.parse(endDateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD.", e);
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        List<Entry> entries = entryRepository.findByUserAndEntryDateBetweenOrderByEntryDateAsc(user, startDate, endDate);
        return entries.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // For journal prompts:
    // A simple implementation for now. Can be expanded.
    // private static final List<String> DEFAULT_PROMPTS = List.of(
    // "What was the highlight of your day?",
    // "What's one thing you learned today?",
    // "How did you overcome a challenge today?",
    // "What are you grateful for right now?",
    // "What's a small win you celebrated today?"
    // );

    // @Override
    // public String getRandomReflectionPrompt() {
    // if (DEFAULT_PROMPTS.isEmpty()) return "Reflect on your day...";
    //     java.util.Random random = new java.util.Random();
    //     return DEFAULT_PROMPTS.get(random.nextInt(DEFAULT_PROMPTS.size()));
    // }
}