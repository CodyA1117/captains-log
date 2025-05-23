package com.minderall.captainslogapp.dto;

import java.time.LocalDate;

public class OuraDataDTO {
    private LocalDate date;
    private Integer readinessScore;
    private Integer sleepScore;
    private Integer activityScore;
    private Integer heartRate;
    private Integer stressScore;

    // Constructors
    public OuraDataDTO() {
    }

    public OuraDataDTO(LocalDate date, Integer readinessScore, Integer sleepScore, Integer activityScore, Integer heartRate, Integer stressScore) {
        this.date = date;
        this.readinessScore = readinessScore;
        this.sleepScore = sleepScore;
        this.activityScore = activityScore;
        this.heartRate = heartRate;
        this.stressScore = stressScore;
    }

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getReadinessScore() { return readinessScore; }
    public void setReadinessScore(Integer readinessScore) { this.readinessScore = readinessScore; }
    public Integer getSleepScore() { return sleepScore; }
    public void setSleepScore(Integer sleepScore) { this.sleepScore = sleepScore; }
    public Integer getActivityScore() { return activityScore; }
    public void setActivityScore(Integer activityScore) { this.activityScore = activityScore; }
    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }
    public Integer getStressScore() { return stressScore; }
    public void setStressScore(Integer stressScore) { this.stressScore = stressScore; }
}