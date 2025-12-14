package com.example.integration.service;

import com.example.integration.model.ApiConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.UriSpec;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GenericApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GenericApiClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Generic method to call any external API based on configuration
     * 
     * @param config The API configuration from database
     * @return List of raw data maps from the API response
     */
    public List<Map<String, Object>> callExternalApi(ApiConfiguration config) {
        try {
            log.info("Calling external API: {} - {}", config.getSystemName(), config.getApiUrl());

            // Parse headers
            Map<String, String> headers = parseJsonToMap(config.getHeaders());
            
            // Parse query parameters
            Map<String, String> queryParams = parseJsonToMap(config.getQueryParams());
            
            // Build the request
            WebClient.RequestHeadersSpec<?> requestSpec = buildRequest(config, headers, queryParams);
            
            // Execute request and get response
            String responseBody = requestSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.debug("API Response: {}", responseBody);

            // Parse response and extract user data
            return extractUserData(responseBody, config.getDataPath());

        } catch (Exception e) {
            log.error("Error calling external API for system: {}", config.getSystemName(), e);
            throw new RuntimeException("Failed to call external API: " + e.getMessage(), e);
        }
    }

    /**
     * Build the HTTP request based on configuration
     */
    private WebClient.RequestHeadersSpec<?> buildRequest(
            ApiConfiguration config, 
            Map<String, String> headers, 
            Map<String, String> queryParams) {
        
        String method = config.getHttpMethod().toUpperCase();
        
        // Build the appropriate request spec based on HTTP method
        WebClient.RequestHeadersSpec<?> requestSpec;
        
        switch (method) {
            case "GET":
                requestSpec = webClient.get()
                    .uri(uriBuilder -> {
                        ((UriSpec<?>) uriBuilder).uri(config.getApiUrl());
                        queryParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    });
                break;
                
            case "DELETE":
                requestSpec = webClient.delete()
                    .uri(uriBuilder -> {
                        ((UriSpec<?>) uriBuilder).uri(config.getApiUrl());
                        queryParams.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    });
                break;
                
            case "POST":
                WebClient.RequestBodyUriSpec postSpec = webClient.post();
                postSpec.uri(uriBuilder -> {
                    ((UriSpec<?>) uriBuilder).uri(config.getApiUrl());
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                });
                // Set request body if provided
                if (config.getRequestBody() != null && !config.getRequestBody().isEmpty()) {
                    requestSpec = postSpec.bodyValue(config.getRequestBody());
                } else {
                    requestSpec = postSpec;
                }
                break;
                
            case "PUT":
                WebClient.RequestBodyUriSpec putSpec = webClient.put();
                putSpec.uri(uriBuilder -> {
                    ((UriSpec<?>) uriBuilder).uri(config.getApiUrl());
                    queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                });
                // Set request body if provided
                if (config.getRequestBody() != null && !config.getRequestBody().isEmpty()) {
                    requestSpec = putSpec.bodyValue(config.getRequestBody());
                } else {
                    requestSpec = putSpec;
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + config.getHttpMethod());
        }

        // Set headers
        headers.forEach(requestSpec::header);
        requestSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return requestSpec;
    }

    /**
     * Extract user data from API response using the configured data path
     */
    private List<Map<String, Object>> extractUserData(String responseBody, String dataPath) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode dataNode = rootNode;

            // Navigate to the data using the path (e.g., "data.users" or "collection")
            if (dataPath != null && !dataPath.isEmpty()) {
                String[] pathSegments = dataPath.split("\\.");
                for (String segment : pathSegments) {
                    dataNode = dataNode.get(segment);
                    if (dataNode == null) {
                        log.warn("Data path segment '{}' not found in response", segment);
                        return new ArrayList<>();
                    }
                }
            }

            // Convert to list of maps
            if (dataNode.isArray()) {
                return objectMapper.convertValue(dataNode, new TypeReference<>() {});
            } else {
                // Single object - wrap in list
                Map<String, Object> singleItem = objectMapper.convertValue(dataNode, new TypeReference<>() {});
                return List.of(singleItem);
            }

        } catch (Exception e) {
            log.error("Error parsing API response", e);
            throw new RuntimeException("Failed to parse API response: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to parse JSON string to Map
     */
    private Map<String, String> parseJsonToMap(String json) {
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON: {}", json, e);
            return Map.of();
        }
    }
}
