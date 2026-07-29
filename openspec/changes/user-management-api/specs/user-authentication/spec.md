## ADDED Requirements

### Requirement: User login
The system SHALL allow users to authenticate via `POST /api/auth/login` by providing their email and password. The system SHALL verify the credentials against the database (active users only) and return a JWT token on success.

#### Scenario: Successful login
- **WHEN** a user sends a login request with a valid email and correct password for an active user
- **THEN** the system SHALL return HTTP 200 with a JWT token, token type "Bearer", email, and role

#### Scenario: Login with incorrect credentials
- **WHEN** a user sends a login request with an invalid email or wrong password
- **THEN** the system SHALL return HTTP 401 Unauthorized with message "Invalid email or password"

#### Scenario: Login with inactive (soft-deleted) account
- **WHEN** a user sends a login request with an email belonging to a user whose `active` field is `false`
- **THEN** the system SHALL return HTTP 401 Unauthorized with message "Invalid email or password"

### Requirement: User logout
The system SHALL allow authenticated users to logout via `POST /api/auth/logout`. The system SHALL extract the JWT token from the Authorization header and add it to the token blacklist so that it cannot be reused.

#### Scenario: Successful logout
- **WHEN** an authenticated user sends a logout request with a valid JWT token
- **THEN** the system SHALL blacklist the token and return HTTP 200 with message "Logged out successfully"

#### Scenario: Subsequent request with blacklisted token
- **WHEN** a request is made with a JWT token that has been blacklisted via logout
- **THEN** the system SHALL return HTTP 401 Unauthorized

### Requirement: JWT token validation
The system SHALL validate JWT tokens on every authenticated request by extracting the `Authorization: Bearer <token>` header, verifying the token signature and expiration, checking the token is not blacklisted, and loading the user from the database (active users only).

#### Scenario: Valid token on protected endpoint
- **WHEN** a request is made to a protected endpoint with a valid, non-blacklisted JWT token for an active user
- **THEN** the system SHALL set the security context and allow the request to proceed

#### Scenario: Expired token
- **WHEN** a request is made with an expired JWT token
- **THEN** the system SHALL return HTTP 401 Unauthorized

#### Scenario: Missing or malformed token
- **WHEN** a request is made to a protected endpoint without an Authorization header or with a malformed token
- **THEN** the system SHALL return HTTP 401 Unauthorized
