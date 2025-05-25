package com.minderall.captainslogapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EntryRequest {

    // entryDate can be optional if the backend defaults to today.
    // If it's required from the frontend, add @NotNull.
    private LocalDate entryDate;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Min(value = 0, message = "Mood must be at least 0")
    @Max(value = 100, message = "Mood must be at most 100")
    private Integer mood;

    @Min(value = 0, message = "Focus must be at least 0")
    @Max(value = 100, message = "Focus must be at most 100")
    private Integer focus;

    @Min(value = 0, message = "Energy must be at least 0")
    @Max(value = 100, message = "Energy must be at most 100")
    private Integer energy;

    @Min(value = 0, message = "Confidence must be at least 0")
    @Max(value = 100, message = "Confidence must be at most 100")
    private Integer confidence;

    @Min(value = 0, message = "Drive must be at least 0")
    @Max(value = 100, message = "Drive must be at most 100")
    private Integer drive;

    @Size(max = 10000, message = "Note cannot exceed 10000 characters")
    private String note;

    @Size(max = 255, message = "Prompt used cannot exceed 255 characters")
    private String promptUsed;
}