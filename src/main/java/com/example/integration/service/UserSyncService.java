package com.example.integration.service;

import com.example.integration.dto.UserSyncResponse;
import com.example.integration.model.ApiConfiguration;
import com.example.integration.model.User;
import com.example.integration.repository.ApiConfigurationRepository;
import com.example.integration.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSyncService {

    private final GenericApiClient apiClient;
    private final ApiConfigurationRepository configRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Sync users from a specific external system
     */
    @Transactional
    public UserSyncResponse syncUsersFromSystem(String systemName) {
        List<String> errors = new ArrayList<>();
        
        try {
            // Get API configuration for the system
            ApiConfiguration config = configRepository.findBySystemNameAndActiveTrue(systemName)
                    .orElseThrow(() -> new RuntimeException("No active configuration found for system: " + systemName));

            log.info("Starting user sync for system: {}", systemName);

            // Call external API
            List<Map<String, Object>> rawUsers = apiClient.callExternalApi(config);
            log.info("Fetched {} users from {}", rawUsers.size(), systemName);

            // Parse field mappings
            Map<String, String> fieldMappings = parseFieldMappings(config.getFieldMappings());

            // Map and store users
            int storedCount = 0;
            for (Map<String, Object> rawUser : rawUsers) {
                try {
                    User user = mapToUser(rawUser, fieldMappings, systemName);
                    
                    // Check if user already exists
                    if (user.getExternalId() != null) {
                        userRepository.findBySystemNameAndExternalId(systemName, user.getExternalId())
                                .ifPresent(existing -> user.setId(existing.getId()));
                    }
                    
                    userRepository.save(user);
                    storedCount++;
                } catch (Exception e) {
                    log.error("Error mapping user: {}", rawUser, e);
                    errors.add("Failed to map user: " + e.getMessage());
                }
            }

            log.info("Stored {} users for system: {}", storedCount, systemName);

            return UserSyncResponse.builder()
                    .systemName(systemName)
                    .usersFetched(rawUsers.size())
                    .usersStored(storedCount)
                    .success(true)
                    .message("Successfully synced users from " + systemName)
                    .errors(errors.isEmpty() ? null : errors)
                    .build();

        } catch (Exception e) {
            log.error("Failed to sync users from system: {}", systemName, e);
            errors.add(e.getMessage());
            
            return UserSyncResponse.builder()
                    .systemName(systemName)
                    .usersFetched(0)
                    .usersStored(0)
                    .success(false)
                    .message("Failed to sync users: " + e.getMessage())
                    .errors(errors)
                    .build();
        }
    }

    /**
     * Sync users from all active systems
     */
    public List<UserSyncResponse> syncUsersFromAllSystems() {
        List<ApiConfiguration> activeConfigs = configRepository.findByActiveTrue();
        List<UserSyncResponse> responses = new ArrayList<>();

        for (ApiConfiguration config : activeConfigs) {
            responses.add(syncUsersFromSystem(config.getSystemName()));
        }

        return responses;
    }

    /**
     * Map raw API data to User entity using field mappings
     */
    private User mapToUser(Map<String, Object> rawData, Map<String, String> fieldMappings, String systemName) {
        User.UserBuilder userBuilder = User.builder()
                .systemName(systemName);

        // Apply field mappings
        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            String apiField = mapping.getKey();      // Field name in API response
            String userField = mapping.getValue();   // Field name in our User entity

            Object value = extractNestedValue(rawData, apiField);
            
            if (value != null) {
                mapFieldToUser(userBuilder, userField, value.toString());
            }
        }

        // Store any additional unmapped data as JSON
        try {
            String additionalData = objectMapper.writeValueAsString(rawData);
            userBuilder.additionalData(additionalData);
        } catch (Exception e) {
            log.warn("Failed to serialize additional data", e);
        }

        return userBuilder.build();
    }

    /**
     * Extract value from nested object using dot notation (e.g., "user.profile.name")
     */
    private Object extractNestedValue(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * Map a field value to the appropriate User entity field
     */
    private void mapFieldToUser(User.UserBuilder builder, String fieldName, String value) {
        switch (fieldName.toLowerCase()) {
            case "externalid" -> builder.externalId(value);
            case "name" -> builder.name(value);
            case "email" -> builder.email(value);
            case "phonenumber" -> builder.phoneNumber(value);
            case "timezone" -> builder.timezone(value);
            case "avatarurl" -> builder.avatarUrl(value);
            case "schedulingurl" -> builder.schedulingUrl(value);
            default -> log.warn("Unknown user field: {}", fieldName);
        }
    }

    /**
     * Parse field mappings JSON string to Map
     */
    private Map<String, String> parseFieldMappings(String fieldMappingsJson) {
        try {
            return objectMapper.readValue(fieldMappingsJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse field mappings", e);
            throw new RuntimeException("Invalid field mappings configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Get all users from temporary storage
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get users from a specific system
     */
    public List<User> getUsersBySystem(String systemName) {
        return userRepository.findBySystemName(systemName);
    }

    /**
     * Clear all users from temporary storage
     */
    @Transactional
    public void clearAllUsers() {
        userRepository.deleteAll();
    }

    /**
     * Clear users from a specific system
     */
    @Transactional
    public void clearUsersBySystem(String systemName) {
        userRepository.deleteBySystemName(systemName);
    }
}
