package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.dto.MessageResponse;
import com.minderall.captainslogapp.dto.EntryRequest;
import com.minderall.captainslogapp.dto.EntryResponse;
import com.minderall.captainslogapp.Security.UserDetailsImpl;
import com.minderall.captainslogapp.Services.EntryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    @Autowired
    private EntryService entryService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntryResponse> createEntry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @Valid @RequestBody EntryRequest entryRequest) {
        EntryResponse createdEntry = entryService.createEntry(userDetails.getId(), entryRequest);
        return new ResponseEntity<>(createdEntry, HttpStatus.CREATED);
    }

    @GetMapping("/{entryId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntryResponse> getEntryById(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                      @PathVariable Long entryId) {
        EntryResponse entry = entryService.getEntryById(userDetails.getId(), entryId);
        return ResponseEntity.ok(entry);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<EntryResponse>> getAllEntriesByUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "entryDate,desc") String[] sort) { // Example: sort=entryDate,desc&sort=createdAt,desc

        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        // For multiple sort criteria, you'd need a more complex parsing or pass them differently

        Page<EntryResponse> entries = entryService.getAllEntriesByUser(userDetails.getId(), pageable);
        return ResponseEntity.ok(entries);
    }

    @PutMapping("/{entryId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntryResponse> updateEntry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     @PathVariable Long entryId,
                                                     @Valid @RequestBody EntryRequest entryRequest) {
        EntryResponse updatedEntry = entryService.updateEntry(userDetails.getId(), entryId, entryRequest);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> deleteEntry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @PathVariable Long entryId) {
        entryService.deleteEntry(userDetails.getId(), entryId);
        return ResponseEntity.ok(new MessageResponse("Entry deleted successfully"));
    }

    @GetMapping("/range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EntryResponse>> getEntriesByDateRange(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String startDate, // Expects YYYY-MM-DD
            @RequestParam String endDate) {  // Expects YYYY-MM-DD
        List<EntryResponse> entries = entryService.getEntriesByUserAndDateRange(userDetails.getId(), startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    // Optional: Endpoint to get a random prompt
    // @GetMapping("/prompt")
    // @PreAuthorize("hasRole('USER')")
    // public ResponseEntity<MessageResponse> getRandomPrompt() {
    //     String prompt = entryService.getRandomReflectionPrompt();
    // return ResponseEntity.ok(new MessageResponse(prompt));
    // }
}