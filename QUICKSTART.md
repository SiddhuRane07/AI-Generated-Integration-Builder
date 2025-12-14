# Quick Start Guide

Get up and running with the Multi-System Integration application in minutes!

## Prerequisites

- Java 17+
- Maven 3.6+
- curl or Postman (for testing)

## 5-Minute Setup

### 1. Build the Application

```bash
cd multi-system-integration
mvn clean install
```

### 2. Start the Application

```bash
mvn spring-boot:run
```

Wait for the application to start. You should see:
```
Started MultiSystemIntegrationApplication in X.XXX seconds
```

### 3. Test with Mock API (No Token Needed!)

Open a new terminal and run:

```bash
# Add mock API configuration
curl -X POST http://localhost:8080/api/configurations \
  -H "Content-Type: application/json" \
  -d '{
    "systemName": "mock-api",
    "apiUrl": "https://jsonplaceholder.typicode.com/users",
    "httpMethod": "GET",
    "headers": "{}",
    "queryParams": "{}",
    "fieldMappings": "{\"id\":\"externalId\",\"name\":\"name\",\"email\":\"email\",\"phone\":\"phoneNumber\"}",
    "dataPath": "",
    "active": true
  }'

# Sync users
curl -X POST http://localhost:8080/api/sync/mock-api

# View fetched users
curl http://localhost:8080/api/users/mock-api
```

**Expected Result**: You should see 10 users fetched from the mock API!

### 4. Setup Calendly (Optional)

To integrate with real Calendly data:

1. **Get your Calendly API token**:
   - Go to https://calendly.com/integrations/api_webhooks
   - Click "Create New Token"
   - Copy the token

2. **Update the configuration**:

```bash
curl -X POST http://localhost:8080/api/configurations \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "systemName": "calendly",
    "apiUrl": "https://api.calendly.com/users/me",
    "httpMethod": "GET",
    "headers": "{\"Authorization\":\"Bearer YOUR_TOKEN_HERE\",\"Content-Type\":\"application/json\"}",
    "queryParams": "{}",
    "fieldMappings": "{\"uri\":\"externalId\",\"name\":\"name\",\"email\":\"email\",\"timezone\":\"timezone\",\"avatar_url\":\"avatarUrl\",\"scheduling_url\":\"schedulingUrl\"}",
    "dataPath": "resource",
    "active": true
  }'
```

Replace `YOUR_TOKEN_HERE` with your actual token.

3. **Sync Calendly users**:

```bash
curl -X POST http://localhost:8080/api/sync/calendly
curl http://localhost:8080/api/users/calendly
```

## Using the Test Script

We've included an automated test script:

```bash
chmod +x test-api.sh
./test-api.sh
```

This will:
- ✓ Check application status
- ✓ Test all API endpoints
- ✓ Create mock API configuration
- ✓ Sync and verify users
- ✓ Test cleanup operations

## Using Postman

1. Import `postman-collection.json` into Postman
2. The collection includes all API endpoints
3. Start making requests!

## Common Commands

### Configuration Management

```bash
# View all configurations
curl http://localhost:8080/api/configurations

# View active configurations
curl http://localhost:8080/api/configurations/active

# Get specific system
curl http://localhost:8080/api/configurations/calendly
```

### User Sync

```bash
# Sync from specific system
curl -X POST http://localhost:8080/api/sync/calendly

# Sync from all systems
curl -X POST http://localhost:8080/api/sync/all
```

### User Retrieval

```bash
# Get all users
curl http://localhost:8080/api/users

# Get users by system
curl http://localhost:8080/api/users/calendly

# Get users by system (JSON formatted)
curl http://localhost:8080/api/users/calendly | jq '.'
```

### User Cleanup

```bash
# Clear all users
curl -X DELETE http://localhost:8080/api/users

# Clear users from specific system
curl -X DELETE http://localhost:8080/api/users/calendly
```

## Access H2 Database Console

For debugging and manual configuration updates:

1. Open: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:integration_db`
3. Username: `sa`
4. Password: (leave empty)
5. Click "Connect"

### Useful SQL Queries

```sql
-- View all configurations
SELECT * FROM api_configurations;

-- View all users
SELECT * FROM temporary_users;

-- Update Calendly token
UPDATE api_configurations 
SET headers = '{"Authorization":"Bearer YOUR_TOKEN","Content-Type":"application/json"}'
WHERE system_name = 'calendly';

-- Check user count by system
SELECT system_name, COUNT(*) as user_count 
FROM temporary_users 
GROUP BY system_name;
```

## Docker Setup (Optional)

### Build and Run with Docker

```bash
# Build image
docker build -t multi-system-integration .

# Run container
docker run -p 8080:8080 multi-system-integration
```

### Using Docker Compose

```bash
docker-compose up -d
```

## Adding More Systems

Want to integrate another system? It's easy!

1. Create configuration JSON
2. POST to `/api/configurations`
3. Sync with `/api/sync/{systemName}`

Example: Adding Salesforce

```bash
curl -X POST http://localhost:8080/api/configurations \
  -H "Content-Type: application/json" \
  -d '{
    "systemName": "salesforce",
    "apiUrl": "https://your-instance.salesforce.com/services/data/v58.0/query",
    "httpMethod": "GET",
    "headers": "{\"Authorization\":\"Bearer YOUR_SALESFORCE_TOKEN\"}",
    "queryParams": "{\"q\":\"SELECT Id, Name, Email FROM User\"}",
    "fieldMappings": "{\"Id\":\"externalId\",\"Name\":\"name\",\"Email\":\"email\"}",
    "dataPath": "records",
    "active": true
  }'

curl -X POST http://localhost:8080/api/sync/salesforce
```

See `EXAMPLE_CONFIGURATIONS.md` for 10+ pre-made configurations!

## Troubleshooting

### Application won't start
- Check Java version: `java -version` (should be 17+)
- Check Maven: `mvn -version`
- Check port 8080 isn't in use: `lsof -i :8080`

### No users fetched
- Verify API token is correct
- Check logs: `tail -f application.log`
- Check dataPath matches API response structure
- Test API directly with curl

### Field mapping issues
- Field names are case-sensitive
- Use dot notation for nested fields
- Check API response structure in logs

### API returns 401
- Token expired or invalid
- Update configuration with new token

## Next Steps

1. ✅ Test with mock API (completed above)
2. ⚡ Configure real system (Calendly, Salesforce, etc.)
3. 🔄 Set up scheduled syncs (use Spring @Scheduled)
4. 📊 Build dashboard to visualize user data
5. 🔐 Add authentication to REST endpoints
6. 🚀 Deploy to production

## Need Help?

- Check `README.md` for detailed documentation
- View `EXAMPLE_CONFIGURATIONS.md` for more system examples
- Check application logs with DEBUG level
- Use H2 console to inspect database

## Example Output

Successful sync response:
```json
{
  "systemName": "calendly",
  "usersFetched": 1,
  "usersStored": 1,
  "success": true,
  "message": "Successfully synced users from calendly",
  "errors": null
}
```

User data:
```json
{
  "id": 1,
  "externalId": "https://api.calendly.com/users/XXXXX",
  "systemName": "calendly",
  "name": "John Doe",
  "email": "john@example.com",
  "timezone": "America/New_York",
  "avatarUrl": "https://...",
  "schedulingUrl": "https://calendly.com/john",
  "fetchedAt": "2024-12-13T10:30:00"
}
```

---

🎉 **You're all set!** Start integrating your systems.

