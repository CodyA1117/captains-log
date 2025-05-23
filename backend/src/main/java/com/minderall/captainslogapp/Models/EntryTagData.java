package com.minderall.captainslogapp.Models;

import com.fasterxml.jackson.annotation.JsonBackReference; // Add this
import jakarta.persistence.*;

@Entity
public class EntryTagData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // private String value; // This field seems redundant if Tag has the name.
    // Usually, a join table just holds the foreign keys.
    // If you intend for this join table to have its own value, keep it.
    // For now, I'll comment it out assuming the tag name is sufficient.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    @JsonBackReference("entry-tagdata") // Child side
    private Entry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    @JsonBackReference("tag-tagdata") // Child side
    private Tag tag;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // public String getValue() { return value; }
    // public void setValue(String value) { this.value = value; }
    public Entry getEntry() { return entry; }
    public void setEntry(Entry entry) { this.entry = entry; }
    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
}