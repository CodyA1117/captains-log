package com.minderall.captainslogapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EntryResponse {
    private Long id;
    private Long userId; // Or a nested UserResponse if needed
    private LocalDate entryDate;
    private String title;
    private Integer mood;
    private Integer focus;
    private Integer energy;
    private Integer confidence;
    private Integer drive;
    private String note;
    private String promptUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}