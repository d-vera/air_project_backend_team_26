## ADDED Requirements

### Requirement: Global CORS Configuration for All Endpoints
The backend system SHALL enable global Cross-Origin Resource Sharing (CORS) across all HTTP API endpoints to accept requests originating from frontend applications running on port 4200 as well as configured external origins or ports.

#### Scenario: Preflight OPTIONS request handling
- **WHEN** an HTTP `OPTIONS` preflight request is sent from a cross-origin client to any backend API endpoint
- **THEN** the system SHALL return HTTP status 200 OK with appropriate CORS headers (`Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Headers`) without enforcing authentication checks on the preflight request.

#### Scenario: Cross-origin HTTP requests from frontend on port 4200
- **WHEN** an HTTP request (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`) is sent from `http://localhost:4200` or a permitted origin to a backend endpoint (`/api/**`, `/swagger-ui/**`, `/v3/api-docs/**`)
- **THEN** the system SHALL process the request and include `Access-Control-Allow-Origin` matching the requesting origin and `Access-Control-Allow-Credentials: true` in the HTTP response headers.

#### Scenario: Support for headers and authentication credentials
- **WHEN** a client sends cross-origin requests containing headers such as `Authorization`, `Content-Type`, or `X-Requested-With`
- **THEN** the system SHALL allow these headers, permit credentials, and expose the `Authorization` header to the response.
