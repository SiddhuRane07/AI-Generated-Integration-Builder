package com.example.integration.controller;

import com.example.integration.dto.UserSyncResponse;
import com.example.integration.model.ApiConfiguration;
import com.example.integration.model.User;
import com.example.integration.repository.ApiConfigurationRepository;
import com.example.integration.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserSyncController {

    private final UserSyncService userSyncService;
    private final ApiConfigurationRepository configRepository;

    /**
     * Sync users from a specific system
     */
    @PostMapping("/sync/{systemName}")
    public ResponseEntity<UserSyncResponse> syncUsers(@PathVariable String systemName) {
        UserSyncResponse response = userSyncService.syncUsersFromSystem(systemName);
        
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Sync users from all configured systems
     */
    @PostMapping("/sync/all")
    public ResponseEntity<List<UserSyncResponse>> syncAllUsers() {
        List<UserSyncResponse> responses = userSyncService.syncUsersFromAllSystems();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all users from temporary storage
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userSyncService.getAllUsers());
    }

    /**
     * Get users from a specific system
     */
    @GetMapping("/users/{systemName}")
    public ResponseEntity<List<User>> getUsersBySystem(@PathVariable String systemName) {
        return ResponseEntity.ok(userSyncService.getUsersBySystem(systemName));
    }

    /**
     * Clear all users from temporary storage
     */
    @DeleteMapping("/users")
    public ResponseEntity<String> clearAllUsers() {
        userSyncService.clearAllUsers();
        return ResponseEntity.ok("All users cleared from temporary storage");
    }

    /**
     * Clear users from a specific system
     */
    @DeleteMapping("/users/{systemName}")
    public ResponseEntity<String> clearUsersBySystem(@PathVariable String systemName) {
        userSyncService.clearUsersBySystem(systemName);
        return ResponseEntity.ok("Users from " + systemName + " cleared from temporary storage");
    }

    /**
     * Get all API configurations
     */
    @GetMapping("/configurations")
    public ResponseEntity<List<ApiConfiguration>> getAllConfigurations() {
        return ResponseEntity.ok(configRepository.findAll());
    }

    /**
     * Get active API configurations
     */
    @GetMapping("/configurations/active")
    public ResponseEntity<List<ApiConfiguration>> getActiveConfigurations() {
        return ResponseEntity.ok(configRepository.findByActiveTrue());
    }

    /**
     * Get API configuration for a specific system
     */
    @GetMapping("/configurations/{systemName}")
    public ResponseEntity<ApiConfiguration> getConfiguration(@PathVariable String systemName) {
        return configRepository.findBySystemNameAndActiveTrue(systemName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create or update API configuration
     */
    @PostMapping("/configurations")
    public ResponseEntity<ApiConfiguration> saveConfiguration(@RequestBody ApiConfiguration config) {
        ApiConfiguration saved = configRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Delete API configuration
     */
    @DeleteMapping("/configurations/{id}")
    public ResponseEntity<String> deleteConfiguration(@PathVariable Long id) {
        configRepository.deleteById(id);
        return ResponseEntity.ok("Configuration deleted");
    }
}
