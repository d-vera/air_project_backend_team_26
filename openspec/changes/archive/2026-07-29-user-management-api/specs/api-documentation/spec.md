## ADDED Requirements

### Requirement: Swagger UI availability
The system SHALL serve Swagger UI at `/swagger-ui.html` (or `/swagger-ui/index.html`). The Swagger UI page SHALL be publicly accessible without authentication.

#### Scenario: Access Swagger UI
- **WHEN** a user navigates to `/swagger-ui.html` in a browser
- **THEN** the system SHALL render the Swagger UI interface with all documented API endpoints

### Requirement: OpenAPI 3 specification
The system SHALL expose the OpenAPI 3 JSON specification at `/v3/api-docs`. The specification SHALL document all REST endpoints with their request/response schemas, HTTP methods, paths, and response codes.

#### Scenario: Retrieve OpenAPI spec
- **WHEN** a client sends a GET request to `/v3/api-docs`
- **THEN** the system SHALL return the OpenAPI 3 JSON document with complete API documentation

### Requirement: JWT security scheme documentation
The Swagger/OpenAPI specification SHALL include a Bearer JWT security scheme. Protected endpoints SHALL be annotated so that Swagger UI shows a lock icon and provides an "Authorize" button for entering the JWT token.

#### Scenario: Authorize in Swagger UI
- **WHEN** a user clicks "Authorize" in Swagger UI and enters a valid JWT token
- **THEN** subsequent API calls from Swagger UI SHALL include the `Authorization: Bearer <token>` header

### Requirement: API metadata
The OpenAPI specification SHALL include the following metadata: title "Air Project — User Management API", version "1.0", and a description summarizing the API capabilities. Endpoints SHALL be grouped with tags: `Authentication` and `User Management`.

#### Scenario: View API info in Swagger
- **WHEN** a user opens Swagger UI
- **THEN** the page SHALL display the API title, version, description, and endpoints grouped by tags
