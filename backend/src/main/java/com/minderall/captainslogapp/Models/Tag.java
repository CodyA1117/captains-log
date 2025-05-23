package com.minderall.captainslogapp.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Add this
import jakarta.persistence.*;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-tags") // Avoid serialization loop with User
    private User user;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("tag-tagdata") // Parent side of Tag <-> EntryTagData
    private List<EntryTagData> tagData = new ArrayList<>(); // Initialize

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<EntryTagData> getTagData() { return tagData; }
    public void setTagData(List<EntryTagData> tagData) { this.tagData = tagData; }
}