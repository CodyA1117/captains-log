package com.minderall.captainslogapp.Services;

import com.minderall.captainslogapp.Models.*;
import com.minderall.captainslogapp.Repositories.*;
import com.minderall.captainslogapp.dto.EntryRequestDTO;
import com.minderall.captainslogapp.dto.EntryResponseDTO;
import com.minderall.captainslogapp.dto.TagDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntryService {

    private static final Logger logger = LoggerFactory.getLogger(EntryService.class);

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    // Helper to convert Entry entity to EntryResponseDTO
    private EntryResponseDTO convertToDTO(Entry entry) {
        EntryResponseDTO dto = new EntryResponseDTO();
        dto.setId(entry.getId());
        dto.setDate(entry.getDate());
        dto.setTitle(entry.getTitle());
        dto.setNote(entry.getNote());
        dto.setEnergy(entry.getEnergy());
        dto.setMood(entry.getMood());
        dto.setPromptUsed(entry.getPromptUsed());
        if (entry.getTagData() != null) {
            dto.setTags(entry.getTagData().stream()
                    .map(entryTagData -> new TagDTO(entryTagData.getTag().getId(), entryTagData.getTag().getName()))
                    .collect(Collectors.toList()));
        } else {
            dto.setTags(new ArrayList<>());
        }
        return dto;
    }


    @Transactional // Ensure operations are atomic
    public EntryResponseDTO createEntry(EntryRequestDTO entryRequestDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Entry newEntry = new Entry();
        newEntry.setUser(user);
        newEntry.setTitle(entryRequestDTO.getTitle());
        newEntry.setNote(entryRequestDTO.getNote());
        newEntry.setDate(entryRequestDTO.getDate() != null ? entryRequestDTO.getDate() : LocalDate.now());
        newEntry.setEnergy(entryRequestDTO.getEnergy());
        newEntry.setMood(entryRequestDTO.getMood());
        // newEntry.setPromptUsed(...); // If you have this in DTO

        // Handle Tags
        if (entryRequestDTO.getTagNames() != null && !entryRequestDTO.getTagNames().isEmpty()) {
            List<EntryTagData> entryTagDataList = new ArrayList<>();
            for (String tagName : entryRequestDTO.getTagNames()) {
                Tag tag = tagRepository.findByNameAndUser(tagName.trim(), user)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName.trim());
                            newTag.setUser(user);
                            return tagRepository.save(newTag);
                        });
                EntryTagData entryTagData = new EntryTagData();
                entryTagData.setEntry(newEntry); // Will be set properly once newEntry is persisted if part of cascade
                entryTagData.setTag(tag);
                entryTagDataList.add(entryTagData);
            }
            newEntry.setTagData(entryTagDataList); // Set the list
            // If EntryTagData is managed by Entry's cascade, this should be enough.
            // Explicitly saving EntryTagData might be needed if cascading isn't fully set or if entry_id is needed before saving EntryTagData
        }

        Entry savedEntry = entryRepository.save(newEntry);

        // If EntryTagData needs explicit linking after Entry is saved (due to entry_id FK)
        if (savedEntry.getTagData() != null) {
            for (EntryTagData etd : savedEntry.getTagData()) {
                etd.setEntry(savedEntry); // Ensure entry is set before saving EntryTagData if not handled by cascade from the start
            }
            // entryTagDataRepository.saveAll(savedEntry.getTagData()); // If you had this repo and needed explicit save
        }


        logger.info("Created new entry with ID: {} for user: {}", savedEntry.getId(), userEmail);
        return convertToDTO(savedEntry);
    }

    public List<EntryResponseDTO> getUserEntries(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return entryRepository.findByUserOrderByDateDescIdDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<EntryResponseDTO> getEntryByIdAndUser(Long entryId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));
        return entryRepository.findByIdAndUser(entryId, user)
                .map(this::convertToDTO);
    }

    @Transactional
    public EntryResponseDTO updateEntry(Long entryId, EntryRequestDTO entryRequestDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Entry entryToUpdate = entryRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found or user not authorized"));

        entryToUpdate.setTitle(entryRequestDTO.getTitle());
        entryToUpdate.setNote(entryRequestDTO.getNote());
        entryToUpdate.setDate(entryRequestDTO.getDate() != null ? entryRequestDTO.getDate() : entryToUpdate.getDate());
        entryToUpdate.setEnergy(entryRequestDTO.getEnergy());
        entryToUpdate.setMood(entryRequestDTO.getMood());

        // Clear existing tags for simplicity; more complex logic could update existing ones
        if (entryToUpdate.getTagData() != null) {
            entryToUpdate.getTagData().clear(); // This will trigger orphanRemoval if configured
        } else {
            entryToUpdate.setTagData(new ArrayList<>());
        }
        // entryRepository.saveAndFlush(entryToUpdate); // Save to clear old tags if orphanRemoval needs it immediately

        // Add new/updated tags
        List<EntryTagData> newEntryTagDataList = new ArrayList<>();
        if (entryRequestDTO.getTagNames() != null && !entryRequestDTO.getTagNames().isEmpty()) {
            for (String tagName : entryRequestDTO.getTagNames()) {
                Tag tag = tagRepository.findByNameAndUser(tagName.trim(), user)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName.trim());
                            newTag.setUser(user);
                            return tagRepository.save(newTag);
                        });
                EntryTagData entryTagData = new EntryTagData();
                entryTagData.setEntry(entryToUpdate);
                entryTagData.setTag(tag);
                newEntryTagDataList.add(entryTagData);
            }
        }
        entryToUpdate.getTagData().addAll(newEntryTagDataList); // Add all new tag associations

        Entry updatedEntry = entryRepository.save(entryToUpdate);
        logger.info("Updated entry with ID: {} for user: {}", updatedEntry.getId(), userEmail);
        return convertToDTO(updatedEntry);
    }

    @Transactional
    public void deleteEntry(Long entryId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        Entry entryToDelete = entryRepository.findByIdAndUser(entryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found or user not authorized"));

        entryRepository.delete(entryToDelete);
        logger.info("Deleted entry with ID: {} for user: {}", entryId, userEmail);
    }
}