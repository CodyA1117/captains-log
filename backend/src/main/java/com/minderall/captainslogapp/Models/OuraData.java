package com.minderall.captainslogapp.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "oura_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "data_date"}) // Ensure one entry per user per day
})
public class OuraData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "data_date", nullable = false)
    private LocalDate dataDate;

    @Column(name = "readiness_score")
    private Integer readinessScore;

    @Column(name = "sleep_score")
    private Integer sleepScore;

    @Column(name = "activity_score")
    private Integer activityScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}