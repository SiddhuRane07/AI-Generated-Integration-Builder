package com.example.integration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String externalId; // ID from the external system

    @Column(nullable = false)
    private String systemName; // Which system this user came from

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private String timezone;

    @Column
    private String avatarUrl;

    @Column
    private String schedulingUrl;

    @Column(columnDefinition = "TEXT")
    private String additionalData; // JSON string for any extra fields

    @Column
    private LocalDateTime fetchedAt;

    @PrePersist
    protected void onCreate() {
        fetchedAt = LocalDateTime.now();
    }
}
