# Multi-System User Integration

Spring Boot app that syncs users from multiple APIs (Calendly, Salesforce, etc.) without hardcoding anything. Add new systems via database config - no code changes needed.

## Quick Start

```bash
# Clone and run
mvn spring-boot:run

# Test with mock API
curl -X POST http://localhost:8081/api/configurations \
  -H "Content-Type: application/json" \
  -d '{"systemName":"mock-api","apiUrl":"https://jsonplaceholder.typicode.com/users","httpMethod":"GET","headers":"{}","fieldMappings":"{\"id\":\"externalId\",\"name\":\"name\",\"email\":\"email\"}","dataPath":"","active":true}'

# Sync users
curl -X POST http://localhost:8081/api/sync/mock-api

# View users
curl http://localhost:8081/api/users/mock-api
```

## How It Works

1. Store API config in database (URL, headers, field mappings)
2. Hit `/api/sync/{systemName}` 
3. `GenericApiClient` calls the API using stored config
4. Maps response fields to User entity
5. Saves to `temporary_users` table

**The key:** One generic method handles ALL APIs. No separate service classes per system.

## Main Endpoints

```bash
POST   /api/sync/{systemName}        # Sync users from system
POST   /api/sync/all                 # Sync all systems
GET    /api/users                    # Get all users
GET    /api/configurations           # List configs
POST   /api/configurations           # Add new system
DELETE /api/users/{systemName}       # Clear users
```

## Add New System

Example - Salesforce:

```bash
curl -X POST http://localhost:8081/api/configurations \
  -H "Content-Type: application/json" \
  -d '{
    "systemName": "salesforce",
    "apiUrl": "https://instance.salesforce.com/services/data/v58.0/query",
    "httpMethod": "GET",
    "headers": "{\"Authorization\":\"Bearer YOUR_TOKEN\"}",
    "queryParams": "{\"q\":\"SELECT Id, Name, Email FROM User\"}",
    "fieldMappings": "{\"Id\":\"externalId\",\"Name\":\"name\",\"Email\":\"email\"}",
    "dataPath": "records",
    "active": true
  }'

# Then sync
curl -X POST http://localhost:8081/api/sync/salesforce
```

Done. No deployment needed.

## Field Mappings

Maps API response fields to User model:

```json
{
  "api_field": "userField",
  "id": "externalId",
  "name": "name",
  "user.email": "email"
}
```

Supports nested fields with dot notation: `user.profile.name`

**Available fields:** `externalId`, `name`, `email`, `phoneNumber`, `timezone`, `avatarUrl`, `schedulingUrl`

## Data Path

Tells where to find users in API response:

| API Response | dataPath |
|--------------|----------|
| `{"data":{"users":[...]}}` | `data.users` |
| `{"resource":{...}}` | `resource` |
| `[...]` | (empty) |

## Tech Stack

- Spring Boot 3.2.0
- Spring Data JPA (Hibernate)
- WebClient (reactive HTTP)
- H2 Database (swap to PostgreSQL for prod)
- Lombok

## Project Structure

```
src/main/java/com/example/integration/
├── controller/UserSyncController.java      # REST endpoints
├── service/
│   ├── GenericApiClient.java               # Calls ANY API
│   └── UserSyncService.java                # Sync logic
├── model/
│   ├── ApiConfiguration.java               # Config entity
│   └── User.java                           # User entity
└── repository/                             # JPA repos
```

## Database

**api_configurations** - API settings (URL, headers, mappings)  
**temporary_users** - Synced users

View in H2 console: `http://localhost:8081/h2-console`  
JDBC URL: `jdbc:h2:mem:integration_db` | User: `sa` | Pass: (empty)

## Debug

```properties
# application.properties
logging.level.com.example.integration=DEBUG
```

Check logs for API responses and field mappings.

## Common Issues

**No users synced?** Check `dataPath` matches API response structure  
**401 error?** Update API token in config headers  
**Fields not mapping?** Case-sensitive - check exact field names

## For Production

- [ ] Encrypt API tokens
- [ ] Use PostgreSQL
- [ ] Add Spring Security
- [ ] Rate limiting
- [ ] Proper error handling

---

**Why this approach?** Configuration > code. Add unlimited systems without touching Java. One `GenericApiClient` = infinite integrations.
