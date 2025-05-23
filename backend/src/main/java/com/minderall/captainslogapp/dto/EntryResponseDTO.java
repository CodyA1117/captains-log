package com.minderall.captainslogapp.dto;

import java.time.LocalDate;
import java.util.List;

public class EntryResponseDTO {
    private Long id;
    private LocalDate date;
    private String title;
    private String note;
    private Integer energy;
    private Integer mood;
    private String promptUsed; // Keeping this if you plan to use it
    private List<TagDTO> tags; // We'll send back TagDTOs

    // Constructors, Getters, and Setters
    public EntryResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getEnergy() {
        return energy;
    }

    public void setEnergy(Integer energy) {
        this.energy = energy;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }

    public String getPromptUsed() {
        return promptUsed;
    }

    public void setPromptUsed(String promptUsed) {
        this.promptUsed = promptUsed;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }
}