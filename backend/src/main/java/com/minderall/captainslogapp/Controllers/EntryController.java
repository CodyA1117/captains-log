package com.minderall.captainslogapp.Controllers;

import com.minderall.captainslogapp.Services.EntryService;
import com.minderall.captainslogapp.dto.EntryRequestDTO;
import com.minderall.captainslogapp.dto.EntryResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/entries") // Changed from /api/logs for clarity, match frontend expectations
public class EntryController {

    @Autowired
    private EntryService entryService;

    // Get all entries for the authenticated user
    @GetMapping
    public ResponseEntity<List<EntryResponseDTO>> getUserEntries(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<EntryResponseDTO> entries = entryService.getUserEntries(userDetails.getUsername());
        return ResponseEntity.ok(entries);
    }

    // Get a specific entry by ID for the authenticated user
    @GetMapping("/{id}")
    public ResponseEntity<EntryResponseDTO> getEntryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return entryService.getEntryByIdAndUser(id, userDetails.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new entry for the authenticated user
    @PostMapping
    public ResponseEntity<EntryResponseDTO> createEntry(
            @RequestBody EntryRequestDTO entryRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            EntryResponseDTO createdEntry = entryService.createEntry(entryRequestDTO, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEntry);
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.badRequest().build(); // Or more specific error
        }
    }

    // Update an existing entry by ID for the authenticated user
    @PutMapping("/{id}")
    public ResponseEntity<EntryResponseDTO> updateEntry(
            @PathVariable Long id,
            @RequestBody EntryRequestDTO entryRequestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            EntryResponseDTO updatedEntry = entryService.updateEntry(id, entryRequestDTO, userDetails.getUsername());
            return ResponseEntity.ok(updatedEntry);
        } catch (ResponseStatusException e) { // Catch specific exception from service
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete an entry by ID for the authenticated user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            entryService.deleteEntry(id, userDetails.getUsername());
            return ResponseEntity.noContent().build(); // 204 No Content on successful delete
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            // Log exception e
            return ResponseEntity.internalServerError().build();
        }
    }
}