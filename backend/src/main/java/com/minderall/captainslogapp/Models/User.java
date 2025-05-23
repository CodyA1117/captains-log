package com.minderall.captainslogapp.Models;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Add this
import jakarta.persistence.*;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... other fields ...
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "nutritionix_user_id")
    private String nutritionixUserId;

    @Column(name = "is_nutritionix_connected")
    private boolean isNutritionixConnected = false;

    private boolean isRunwayComplete;
    private String role;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-entries") // Parent side of User <-> Entry
    private List<Entry> entries = new ArrayList<>(); // Initialize

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-ouradata") // Parent side of User <-> OuraData
    private List<OuraData> ouraData = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    // @JsonManagedReference("user-chronometerdata") // Add if ChronometerData has back-ref
    private List<ChronometerData> chronometerData = new ArrayList<>(); // Assuming ChronometerData exists

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-tags") // Parent side of User <-> Tag
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-nutritionlogs")
    private List<NutritionLog> nutritionLogs = new ArrayList<>();



    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNutritionixUserId() { return nutritionixUserId; }
    public void setNutritionixUserId(String nutritionixUserId) { this.nutritionixUserId = nutritionixUserId; }
    public boolean isNutritionixConnected() { return isNutritionixConnected; }
    public void setNutritionixConnected(boolean nutritionixConnected) { this.isNutritionixConnected = nutritionixConnected; }
    public boolean isRunwayComplete() { return isRunwayComplete; }
    public void setRunwayComplete(boolean runwayComplete) { this.isRunwayComplete = runwayComplete; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Entry> getEntries() { return entries; }
    public void setEntries(List<Entry> entries) { this.entries = entries; }
    public List<OuraData> getOuraData() { return ouraData; }
    public void setOuraData(List<OuraData> ouraData) { this.ouraData = ouraData; }
    public List<ChronometerData> getChronometerData() { return chronometerData; }
    public void setChronometerData(List<ChronometerData> chronometerData) { this.chronometerData = chronometerData; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    public List<NutritionLog> getNutritionLogs() { return nutritionLogs; }
    public void setNutritionLogs(List<NutritionLog> nutritionLogs) { this.nutritionLogs = nutritionLogs; }
}
