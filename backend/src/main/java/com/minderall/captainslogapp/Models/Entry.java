package com.minderall.captainslogapp.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Add this
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

@Entity
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String note;

    private Integer energy;
    private Integer mood;
    private String promptUsed;

    @ManyToOne(fetch = FetchType.LAZY) // Eager fetching can cause issues
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-entries") // Avoid serialization loop with User
    private User user;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // Eager fetch tags for an entry
    @JsonManagedReference("entry-tagdata") // Parent side of Entry <-> EntryTagData
    private List<EntryTagData> tagData = new ArrayList<>(); // Initialize to prevent nulls

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getEnergy() { return energy; }
    public void setEnergy(Integer energy) { this.energy = energy; }
    public Integer getMood() { return mood; }
    public void setMood(Integer mood) { this.mood = mood; }
    public String getPromptUsed() { return promptUsed; }
    public void setPromptUsed(String promptUsed) { this.promptUsed = promptUsed; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<EntryTagData> getTagData() { return tagData; }
    public void setTagData(List<EntryTagData> tagData) { this.tagData = tagData; }
}