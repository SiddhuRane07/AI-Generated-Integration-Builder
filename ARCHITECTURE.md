# Architecture Documentation

## System Overview

The Multi-System Integration application is designed with a configuration-driven architecture that allows dynamic integration with any REST API without code changes or redeployment.

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Applications                       │
│                  (Postman, curl, Web UI, etc.)                   │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                                │ HTTP/REST
                                │
┌───────────────────────────────▼─────────────────────────────────┐
│                    Spring Boot Application                        │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │              REST API Layer (Controllers)                    │ │
│  │  • Configuration Management   • User Sync                   │ │
│  │  • User Retrieval            • User Cleanup                 │ │
│  └────────────────┬─────────────────────────────┬──────────────┘ │
│                   │                             │                 │
│  ┌────────────────▼─────────────┐  ┌───────────▼──────────────┐ │
│  │      Service Layer           │  │   Generic API Client      │ │
│  │  • UserSyncService           │◄─┤   • Dynamic HTTP client   │ │
│  │  • Field Mapping Logic       │  │   • Response parsing      │ │
│  │  • Data Transformation       │  │   • Error handling        │ │
│  └────────────────┬─────────────┘  └───────────┬──────────────┘ │
│                   │                             │                 │
│  ┌────────────────▼─────────────────────────────▼──────────────┐ │
│  │              Data Access Layer (Repositories)                │ │
│  │  • ApiConfigurationRepository  • UserRepository             │ │
│  └────────────────┬─────────────────────────────┬──────────────┘ │
└───────────────────┼─────────────────────────────┼────────────────┘
                    │                             │
┌───────────────────▼─────────────────────────────▼────────────────┐
│                         Database (H2)                             │
│  ┌─────────────────────────┐  ┌──────────────────────────────┐  │
│  │  api_configurations     │  │  temporary_users             │  │
│  │  • system_name          │  │  • id                        │  │
│  │  • api_url              │  │  • external_id               │  │
│  │  • http_method          │  │  • system_name               │  │
│  │  • headers              │  │  • name, email, etc.         │  │
│  │  • field_mappings       │  │  • additional_data           │  │
│  │  • data_path            │  │  • fetched_at                │  │
│  └─────────────────────────┘  └──────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
                    ▲
                    │ API Calls (via WebClient)
                    │
┌───────────────────▼───────────────────────────────────────────────┐
│                    External Systems                                │
│  • Calendly  • Salesforce  • HubSpot  • Slack  • GitHub  • Zoom   │
└────────────────────────────────────────────────────────────────────┘
```

## Component Details

### 1. REST API Layer (Controllers)

**UserSyncController**
- Exposes RESTful endpoints for all operations
- Handles HTTP requests/responses
- Validates inputs and manages HTTP status codes

**Key Endpoints:**
- `POST /api/sync/{systemName}` - Sync users from specific system
- `POST /api/sync/all` - Sync from all active systems
- `GET /api/users` - Retrieve all users
- `GET /api/users/{systemName}` - Retrieve system-specific users
- `POST /api/configurations` - Create/update API configurations
- `DELETE /api/users/{systemName}` - Clear system users

### 2. Service Layer

**UserSyncService**
- Orchestrates the sync process
- Applies field mappings
- Handles data transformation
- Manages transactions

**Key Methods:**
```java
UserSyncResponse syncUsersFromSystem(String systemName)
List<UserSyncResponse> syncUsersFromAllSystems()
User mapToUser(Map<String, Object> rawData, Map<String, String> fieldMappings, String systemName)
```

**GenericApiClient**
- Single method to call ANY external API
- Dynamic HTTP request building
- Response parsing with JSONPath
- Error handling and retry logic

**Key Method:**
```java
List<Map<String, Object>> callExternalApi(ApiConfiguration config)
```

### 3. Data Access Layer

**Repositories:**
- `ApiConfigurationRepository` - Manages API configurations
- `UserRepository` - Manages temporary user storage

**Key Features:**
- Spring Data JPA
- Custom query methods
- Transaction management

### 4. Domain Model

**ApiConfiguration Entity**
```
┌─────────────────────────┐
│  ApiConfiguration       │
├─────────────────────────┤
│ + id: Long              │
│ + systemName: String    │
│ + apiUrl: String        │
│ + httpMethod: String    │
│ + headers: String       │
│ + queryParams: String   │
│ + requestBody: String   │
│ + fieldMappings: String │
│ + dataPath: String      │
│ + active: Boolean       │
│ + createdAt: DateTime   │
│ + updatedAt: DateTime   │
└─────────────────────────┘
```

**User Entity**
```
┌─────────────────────────┐
│  User                   │
├─────────────────────────┤
│ + id: Long              │
│ + externalId: String    │
│ + systemName: String    │
│ + name: String          │
│ + email: String         │
│ + phoneNumber: String   │
│ + timezone: String      │
│ + avatarUrl: String     │
│ + schedulingUrl: String │
│ + additionalData: String│
│ + fetchedAt: DateTime   │
└─────────────────────────┘
```

## Data Flow

### User Sync Flow

```
1. Client Request
   POST /api/sync/calendly
         │
         ▼
2. Controller
   UserSyncController.syncUsers()
         │
         ▼
3. Service Layer
   UserSyncService.syncUsersFromSystem("calendly")
         │
         ├─► Fetch API configuration from DB
         │
         ▼
4. Generic API Client
   GenericApiClient.callExternalApi(config)
         │
         ├─► Build HTTP request dynamically
         ├─► Call external API (Calendly)
         ├─► Parse JSON response
         └─► Extract user data using dataPath
         │
         ▼
5. Back to Service Layer
         │
         ├─► Parse field mappings
         ├─► Map raw data to User entity
         └─► Handle nested fields
         │
         ▼
6. Repository
   UserRepository.save()
         │
         ▼
7. Response
   Return UserSyncResponse with stats
```

### Configuration Flow

```
1. Bootstrap (Application Startup)
   DataInitializer.run()
         │
         ├─► Create default Calendly configuration
         └─► Save to database
         
2. Runtime Configuration Update
   POST /api/configurations
         │
         ├─► Validate configuration
         ├─► Store in database
         └─► No application restart needed
         
3. Configuration Retrieval
   Sync process fetches active configs
         │
         └─► Used by GenericApiClient
```

## Key Design Patterns

### 1. Strategy Pattern
- Generic API client accepts configuration objects
- Behavior changes based on configuration
- No code changes needed for new systems

### 2. Template Method Pattern
- UserSyncService defines the sync algorithm
- Field mapping strategy is configurable
- Data extraction is parameterized

### 3. Repository Pattern
- Abstracts data access
- Separates business logic from persistence
- Easy to swap databases

### 4. Builder Pattern
- ApiConfiguration.builder()
- User.builder()
- Fluent API for object creation

## Configuration-Driven Architecture

### Why Configuration-Driven?

**Traditional Approach (Bad):**
```java
// Need separate service for each system
public class CalendlyService { ... }
public class SalesforceService { ... }
public class HubSpotService { ... }
// Code changes and redeployment for each new system!
```

**Our Approach (Good):**
```java
// One service handles ALL systems
public class GenericApiClient {
    public List<Map<String, Object>> callExternalApi(ApiConfiguration config) {
        // Works with ANY REST API based on config
    }
}
```

### Benefits

1. **No Code Changes**: Add new systems via configuration
2. **No Redeployment**: Update endpoints without restart
3. **Dynamic Field Mapping**: Map any API structure
4. **Easy Testing**: Mock configurations for testing
5. **Flexibility**: Support any REST API
6. **Maintainability**: Single codebase for all integrations

## Field Mapping System

### How It Works

```
API Response:
{
  "user": {
    "profile": {
      "full_name": "John Doe",
      "contact": {
        "email": "john@example.com"
      }
    }
  }
}

Field Mappings Configuration:
{
  "user.profile.full_name": "name",
  "user.profile.contact.email": "email"
}

Result (User Entity):
{
  "name": "John Doe",
  "email": "john@example.com"
}
```

### Supported Features

1. **Nested Fields**: Use dot notation (`user.profile.name`)
2. **Array Access**: Use brackets (`contacts[0].email`)
3. **Type Conversion**: Automatic string conversion
4. **Default Values**: Graceful handling of missing fields
5. **Additional Data**: Unmapped fields stored as JSON

## Data Path Resolution

The `dataPath` field navigates JSON structures:

```
Example 1: Nested Array
API Response:
{
  "data": {
    "users": [
      {"id": 1, "name": "John"}
    ]
  }
}
dataPath: "data.users"

Example 2: Top-Level Array
API Response:
[
  {"id": 1, "name": "John"}
]
dataPath: "" (empty)

Example 3: Single Object
API Response:
{
  "resource": {
    "id": 1,
    "name": "John"
  }
}
dataPath: "resource"
```

## Error Handling

### Layers of Error Handling

1. **Controller Layer**
   - Catches service exceptions
   - Returns appropriate HTTP status codes
   - Provides error messages in response

2. **Service Layer**
   - Validates configurations
   - Handles mapping errors
   - Collects error details

3. **API Client Layer**
   - HTTP timeout handling
   - Network error recovery
   - Response parsing errors

### Error Response Format

```json
{
  "systemName": "calendly",
  "usersFetched": 0,
  "usersStored": 0,
  "success": false,
  "message": "Failed to sync users: Connection timeout",
  "errors": [
    "Connection timeout after 30 seconds",
    "Network unreachable"
  ]
}
```

## Performance Considerations

### Current Implementation
- **Synchronous**: One system at a time
- **In-Memory**: H2 database
- **No Caching**: Fresh data on each sync

### Scalability Improvements

1. **Async Processing**
   ```java
   @Async
   public CompletableFuture<UserSyncResponse> syncUsersFromSystem(String systemName)
   ```

2. **Batch Processing**
   ```java
   userRepository.saveAll(users); // Batch insert
   ```

3. **Caching**
   ```java
   @Cacheable("configurations")
   public ApiConfiguration getConfiguration(String systemName)
   ```

4. **Rate Limiting**
   ```java
   @RateLimiter(name = "apiCalls")
   public List<Map<String, Object>> callExternalApi(ApiConfiguration config)
   ```

5. **Connection Pooling**
   ```java
   WebClient.builder()
       .clientConnector(new ReactorClientHttpConnector(
           HttpClient.create().responseTimeout(Duration.ofSeconds(30))
       ))
   ```

## Security Architecture

### Current State (Demo)
⚠️ Basic security - suitable for development only

### Production Recommendations

1. **API Token Storage**
   - Use secrets manager (AWS Secrets Manager, Vault)
   - Encrypt sensitive configuration fields
   - Never log tokens

2. **REST API Security**
   - Add Spring Security
   - Implement JWT authentication
   - Use API keys for machine access

3. **Network Security**
   - Use HTTPS only
   - Implement IP whitelisting
   - Add request signing

4. **Data Security**
   - Encrypt PII fields
   - Implement data retention policies
   - Add audit logging

## Database Schema Evolution

### Current: H2 In-Memory
- Fast for development
- Resets on restart
- No persistence needed for temporary data

### Production Options

**PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/integration_db
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**MySQL:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/integration_db
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Schema Migration
- Use Flyway or Liquibase
- Version control database changes
- Support rollbacks

## Testing Strategy

### Unit Tests
- Service layer logic
- Field mapping functions
- Data transformation

### Integration Tests
- End-to-end API flow
- Database operations
- Mock external APIs

### Example Test
```java
@Test
void testSyncUsersFromSystem() {
    // Given
    ApiConfiguration config = createTestConfig();
    when(apiClient.callExternalApi(config))
        .thenReturn(mockUserData());
    
    // When
    UserSyncResponse response = service.syncUsersFromSystem("test");
    
    // Then
    assertEquals(10, response.getUsersFetched());
    assertEquals(10, response.getUsersStored());
    assertTrue(response.isSuccess());
}
```

## Monitoring and Observability

### Logging
- Request/response logging
- Performance metrics
- Error tracking

### Metrics
- Users synced per system
- API call success rate
- Response times
- Error rates

### Health Checks
```java
@GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of(
        "status", "UP",
        "database", "connected",
        "active_systems", String.valueOf(activeSystemCount)
    ));
}
```

## Extension Points

### 1. Add Pagination Support
```java
public interface PaginatedApiClient {
    List<Map<String, Object>> fetchAllPages(ApiConfiguration config);
}
```

### 2. Add Webhooks
```java
@PostMapping("/webhook/{systemName}")
public ResponseEntity<Void> handleWebhook(
    @PathVariable String systemName,
    @RequestBody Map<String, Object> payload
) {
    // Real-time updates
}
```

### 3. Add Data Transformation
```java
public interface DataTransformer {
    Map<String, Object> transform(
        Map<String, Object> raw,
        TransformationRule rule
    );
}
```

### 4. Add Scheduling
```java
@Scheduled(cron = "0 0 * * * *") // Every hour
public void scheduledSync() {
    syncUsersFromAllSystems();
}
```

## Conclusion

This architecture provides:
- ✅ Flexibility through configuration
- ✅ Extensibility without code changes
- ✅ Clear separation of concerns
- ✅ Easy to test and maintain
- ✅ Production-ready patterns

The key innovation is the **generic API client** combined with **database-stored configurations**, allowing infinite integrations without touching code.
