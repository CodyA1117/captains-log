package com.minderall.captainslogapp.dto;

import java.time.LocalDate;
import java.util.List;

public class EntryRequestDTO {
    private String title;
    private LocalDate date; // Frontend can send this, or backend can default to today
    private String note;
    private Integer energy;
    private Integer mood;
    private List<String> tagNames; // Frontend will send a list of tag names (strings)

    // Constructors, Getters, and Setters
    public EntryRequestDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}