# Example API Configurations

This file contains example configurations for various third-party systems.

## Calendly Configuration (Pre-configured)

```json
{
  "systemName": "calendly",
  "apiUrl": "https://api.calendly.com/users/me",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_CALENDLY_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{}",
  "requestBody": "",
  "fieldMappings": "{\"uri\":\"externalId\",\"name\":\"name\",\"email\":\"email\",\"timezone\":\"timezone\",\"avatar_url\":\"avatarUrl\",\"scheduling_url\":\"schedulingUrl\"}",
  "dataPath": "resource",
  "active": true
}
```

## Salesforce Configuration

```json
{
  "systemName": "salesforce",
  "apiUrl": "https://your-instance.salesforce.com/services/data/v58.0/query",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_SALESFORCE_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{\"q\":\"SELECT Id, Name, Email, Phone, TimeZoneSidKey FROM User WHERE IsActive = true\"}",
  "requestBody": "",
  "fieldMappings": "{\"Id\":\"externalId\",\"Name\":\"name\",\"Email\":\"email\",\"Phone\":\"phoneNumber\",\"TimeZoneSidKey\":\"timezone\"}",
  "dataPath": "records",
  "active": true
}
```

## HubSpot Configuration

```json
{
  "systemName": "hubspot",
  "apiUrl": "https://api.hubapi.com/crm/v3/objects/contacts",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_HUBSPOT_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{\"limit\":\"100\",\"properties\":\"email,firstname,lastname,phone,hs_timezone\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"properties.email\":\"email\",\"properties.firstname\":\"name\",\"properties.phone\":\"phoneNumber\",\"properties.hs_timezone\":\"timezone\"}",
  "dataPath": "results",
  "active": true
}
```

## Slack Configuration (Users List)

```json
{
  "systemName": "slack",
  "apiUrl": "https://slack.com/api/users.list",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_SLACK_BOT_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"real_name\":\"name\",\"profile.email\":\"email\",\"profile.phone\":\"phoneNumber\",\"tz\":\"timezone\",\"profile.image_192\":\"avatarUrl\"}",
  "dataPath": "members",
  "active": true
}
```

## GitHub Configuration (Organization Members)

```json
{
  "systemName": "github",
  "apiUrl": "https://api.github.com/orgs/YOUR_ORG/members",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"token YOUR_GITHUB_TOKEN\",\"Accept\":\"application/vnd.github.v3+json\"}",
  "queryParams": "{\"per_page\":\"100\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"login\":\"name\",\"avatar_url\":\"avatarUrl\",\"html_url\":\"schedulingUrl\"}",
  "dataPath": "",
  "active": true
}
```

## Zoom Configuration

```json
{
  "systemName": "zoom",
  "apiUrl": "https://api.zoom.us/v2/users",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_ZOOM_JWT\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{\"status\":\"active\",\"page_size\":\"300\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"first_name\":\"name\",\"email\":\"email\",\"timezone\":\"timezone\",\"pic_url\":\"avatarUrl\"}",
  "dataPath": "users",
  "active": true
}
```

## Google Workspace Configuration (Admin SDK)

```json
{
  "systemName": "google-workspace",
  "apiUrl": "https://admin.googleapis.com/admin/directory/v1/users",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_GOOGLE_ACCESS_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{\"customer\":\"my_customer\",\"maxResults\":\"500\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"name.fullName\":\"name\",\"primaryEmail\":\"email\",\"phones[0].value\":\"phoneNumber\",\"thumbnailPhotoUrl\":\"avatarUrl\"}",
  "dataPath": "users",
  "active": true
}
```

## Microsoft 365 Configuration (Graph API)

```json
{
  "systemName": "microsoft365",
  "apiUrl": "https://graph.microsoft.com/v1.0/users",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_MS_ACCESS_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{\"$select\":\"id,displayName,mail,mobilePhone,officeLocation\",\"$top\":\"999\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"displayName\":\"name\",\"mail\":\"email\",\"mobilePhone\":\"phoneNumber\",\"officeLocation\":\"timezone\"}",
  "dataPath": "value",
  "active": true
}
```

## Stripe Configuration (Customers)

```json
{
  "systemName": "stripe",
  "apiUrl": "https://api.stripe.com/v1/customers",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_STRIPE_SECRET_KEY\",\"Content-Type\":\"application/x-www-form-urlencoded\"}",
  "queryParams": "{\"limit\":\"100\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"name\":\"name\",\"email\":\"email\",\"phone\":\"phoneNumber\"}",
  "dataPath": "data",
  "active": true
}
```

## Shopify Configuration (Customers)

```json
{
  "systemName": "shopify",
  "apiUrl": "https://your-store.myshopify.com/admin/api/2024-01/customers.json",
  "httpMethod": "GET",
  "headers": "{\"X-Shopify-Access-Token\":\"YOUR_SHOPIFY_TOKEN\",\"Content-Type\":\"application/json\"}",
  "queryParams": "{\"limit\":\"250\"}",
  "requestBody": "",
  "fieldMappings": "{\"id\":\"externalId\",\"first_name\":\"name\",\"email\":\"email\",\"phone\":\"phoneNumber\"}",
  "dataPath": "customers",
  "active": true
}
```

## Custom REST API Example

For any generic REST API:

```json
{
  "systemName": "custom-api",
  "apiUrl": "https://api.example.com/v1/users",
  "httpMethod": "GET",
  "headers": "{\"Authorization\":\"Bearer YOUR_TOKEN\",\"X-API-Key\":\"YOUR_API_KEY\"}",
  "queryParams": "{\"status\":\"active\",\"limit\":\"1000\"}",
  "requestBody": "",
  "fieldMappings": "{\"user_id\":\"externalId\",\"full_name\":\"name\",\"email_address\":\"email\",\"mobile\":\"phoneNumber\"}",
  "dataPath": "data.users",
  "active": true
}
```

## Notes

### Getting API Tokens

- **Calendly**: https://calendly.com/integrations/api_webhooks
- **Salesforce**: Setup → Users → Generate New Token
- **HubSpot**: Settings → Integrations → Private Apps
- **Slack**: https://api.slack.com/apps → OAuth & Permissions
- **GitHub**: Settings → Developer settings → Personal access tokens
- **Zoom**: https://marketplace.zoom.us/develop/create
- **Google**: https://console.cloud.google.com/apis/credentials
- **Microsoft**: https://portal.azure.com → App registrations

### Field Mapping Tips

1. **Nested Fields**: Use dot notation (e.g., `user.profile.name`)
2. **Array Access**: Use `[0]` for first element (e.g., `phones[0].number`)
3. **Case Sensitivity**: Field names are case-sensitive
4. **Available Fields**: 
   - externalId
   - name
   - email
   - phoneNumber
   - timezone
   - avatarUrl
   - schedulingUrl

### Data Path Examples

- Single object: `resource` or `data.user`
- Array directly: leave empty or just `data`
- Nested array: `response.data.users`
- Top-level: `` (empty string)
