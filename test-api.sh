#!/bin/bash

# API Testing Script for Multi-System Integration
# This script demonstrates how to use the REST API

BASE_URL="http://localhost:8080/api"

echo "=========================================="
echo "Multi-System Integration - API Test Script"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_section() {
    echo ""
    echo -e "${BLUE}=========================================="
    echo -e "$1"
    echo -e "==========================================${NC}"
    echo ""
}

# Function to print success
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# 1. Check if application is running
print_section "1. Checking Application Status"
if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/configurations" | grep -q "200"; then
    print_success "Application is running"
else
    print_error "Application is not running. Please start it with: mvn spring-boot:run"
    exit 1
fi

# 2. Get all configurations
print_section "2. Getting All API Configurations"
echo "GET $BASE_URL/configurations"
curl -s -X GET "$BASE_URL/configurations" | jq '.'

# 3. Get active configurations
print_section "3. Getting Active Configurations"
echo "GET $BASE_URL/configurations/active"
curl -s -X GET "$BASE_URL/configurations/active" | jq '.'

# 4. Get Calendly configuration
print_section "4. Getting Calendly Configuration"
echo "GET $BASE_URL/configurations/calendly"
curl -s -X GET "$BASE_URL/configurations/calendly" | jq '.'

# 5. Add a new configuration (Example: Mock API)
print_section "5. Adding New Mock API Configuration"
echo "POST $BASE_URL/configurations"
curl -s -X POST "$BASE_URL/configurations" \
  -H "Content-Type: application/json" \
  -d '{
    "systemName": "mock-api",
    "apiUrl": "https://jsonplaceholder.typicode.com/users",
    "httpMethod": "GET",
    "headers": "{}",
    "queryParams": "{}",
    "requestBody": "",
    "fieldMappings": "{\"id\":\"externalId\",\"name\":\"name\",\"email\":\"email\",\"phone\":\"phoneNumber\",\"address.city\":\"timezone\",\"website\":\"schedulingUrl\"}",
    "dataPath": "",
    "active": true
  }' | jq '.'

print_success "Mock API configuration added"

# 6. Sync users from mock API
print_section "6. Syncing Users from Mock API"
echo "POST $BASE_URL/sync/mock-api"
SYNC_RESPONSE=$(curl -s -X POST "$BASE_URL/sync/mock-api")
echo "$SYNC_RESPONSE" | jq '.'

USERS_FETCHED=$(echo "$SYNC_RESPONSE" | jq -r '.usersFetched')
USERS_STORED=$(echo "$SYNC_RESPONSE" | jq -r '.usersStored')

if [ "$USERS_FETCHED" -gt 0 ]; then
    print_success "Fetched $USERS_FETCHED users"
    print_success "Stored $USERS_STORED users"
else
    print_error "No users fetched"
fi

# 7. Get all users
print_section "7. Getting All Fetched Users"
echo "GET $BASE_URL/users"
curl -s -X GET "$BASE_URL/users" | jq '. | length as $count | {total_users: $count, users: .}'

# 8. Get users by system
print_section "8. Getting Users from Mock API"
echo "GET $BASE_URL/users/mock-api"
curl -s -X GET "$BASE_URL/users/mock-api" | jq '.[0:3]'  # Show first 3 users

# 9. Try syncing from Calendly (will need API token)
print_section "9. Testing Calendly Sync (Update Token First!)"
echo "POST $BASE_URL/sync/calendly"
echo ""
echo "⚠️  This will fail without a valid Calendly API token"
echo "To configure:"
echo "1. Get token from https://calendly.com/integrations/api_webhooks"
echo "2. Update configuration with your token"
echo ""
read -p "Have you configured your Calendly token? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    curl -s -X POST "$BASE_URL/sync/calendly" | jq '.'
else
    echo "Skipping Calendly sync. Configure token first."
fi

# 10. Sync all systems
print_section "10. Syncing All Systems"
echo "POST $BASE_URL/sync/all"
curl -s -X POST "$BASE_URL/sync/all" | jq '.'

# 11. Clear users by system
print_section "11. Clearing Mock API Users"
echo "DELETE $BASE_URL/users/mock-api"
CLEAR_RESPONSE=$(curl -s -X DELETE "$BASE_URL/users/mock-api")
echo "$CLEAR_RESPONSE"
print_success "Mock API users cleared"

# 12. Verify users cleared
print_section "12. Verifying Mock API Users Cleared"
echo "GET $BASE_URL/users/mock-api"
REMAINING=$(curl -s -X GET "$BASE_URL/users/mock-api" | jq '. | length')
echo "Remaining mock-api users: $REMAINING"

if [ "$REMAINING" -eq 0 ]; then
    print_success "All mock-api users successfully cleared"
else
    print_error "Some mock-api users remain"
fi

# 13. Summary
print_section "13. Test Summary"
echo "✓ Configuration management tested"
echo "✓ User sync tested (mock API)"
echo "✓ User retrieval tested"
echo "✓ User deletion tested"
echo ""
echo "Next steps:"
echo "1. Configure your Calendly API token"
echo "2. Run: curl -X POST $BASE_URL/sync/calendly"
echo "3. Add more system configurations"
echo ""
echo "For more information, see README.md"

print_section "Testing Complete!"
