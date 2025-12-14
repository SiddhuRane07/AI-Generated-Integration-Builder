package com.example.integration.config;

import com.example.integration.model.ApiConfiguration;
import com.example.integration.repository.ApiConfigurationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ApiConfigurationRepository configRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        // Check if Calendly configuration already exists
        if (configRepository.findBySystemNameAndActiveTrue("calendly").isEmpty()) {
            log.info("Initializing Calendly API configuration...");
            createCalendlyConfiguration();
        } else {
            log.info("Calendly configuration already exists");
        }
    }

    private void createCalendlyConfiguration() throws Exception {
        // Headers configuration for Calendly API
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer YOUR_CALENDLY_API_TOKEN");
        headers.put("Content-Type", "application/json");

        // Field mappings: API field -> User entity field
        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("uri", "externalId");           // Calendly user URI as external ID
        fieldMappings.put("name", "name");                // User's full name
        fieldMappings.put("email", "email");              // User's email
        fieldMappings.put("timezone", "timezone");        // User's timezone
        fieldMappings.put("avatar_url", "avatarUrl");     // Avatar URL
        fieldMappings.put("scheduling_url", "schedulingUrl"); // Scheduling page URL

        // Create the configuration
        ApiConfiguration calendlyConfig = ApiConfiguration.builder()
                .systemName("calendly")
                .apiUrl("https://api.calendly.com/users/me")
                .httpMethod("GET")
                .headers(objectMapper.writeValueAsString(headers))
                .queryParams("{}")  // No query params needed for this endpoint
                .requestBody("")    // No request body for GET
                .fieldMappings(objectMapper.writeValueAsString(fieldMappings))
                .dataPath("resource") // Calendly wraps user data in "resource" object
                .active(true)
                .build();

        configRepository.save(calendlyConfig);
        log.info("Calendly configuration created successfully");

        // Log instructions for users
        log.info("========================================");
        log.info("IMPORTANT: Update Calendly API Token");
        log.info("========================================");
        log.info("1. Get your Calendly API token from: https://calendly.com/integrations/api_webhooks");
        log.info("2. Update the configuration via API or H2 console:");
        log.info("   - H2 Console: http://localhost:8080/h2-console");
        log.info("   - JDBC URL: jdbc:h2:mem:integration_db");
        log.info("   - Username: sa");
        log.info("   - Password: (leave empty)");
        log.info("3. Or use the REST API: POST /api/configurations");
        log.info("========================================");
    }
}
