package com.example.integration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String systemName; // e.g., "calendly", "salesforce"

    @Column(nullable = false)
    private String apiUrl; // Base URL or endpoint

    @Column(nullable = false)
    private String httpMethod; // GET, POST, etc.

    @Column(columnDefinition = "TEXT")
    private String headers; // JSON string of headers

    @Column(columnDefinition = "TEXT")
    private String queryParams; // JSON string of query parameters

    @Column(columnDefinition = "TEXT")
    private String requestBody; // JSON string for request body

    @Column(columnDefinition = "TEXT", nullable = false)
    private String fieldMappings; // JSON mapping from API fields to our User fields

    @Column(columnDefinition = "TEXT")
    private String dataPath; // JSONPath to extract user list from response (e.g., "data.users")

    @Column(nullable = false)
    private Boolean active = true;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
