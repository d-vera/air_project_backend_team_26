## ADDED Requirements

### Requirement: Admin assigns role to user
The system SHALL allow users with role `ADMIN` to change any active user's role via `PUT /api/users/{id}/role`. The request body SHALL contain the target role (`REGISTERED_USER` or `ADMIN`).

#### Scenario: Admin assigns ADMIN role
- **WHEN** an admin sends a PUT request to `/api/users/{id}/role` with role `ADMIN` for an active user
- **THEN** the system SHALL update the user's role to `ADMIN` and return HTTP 200 with the updated user profile

#### Scenario: Admin assigns REGISTERED_USER role
- **WHEN** an admin sends a PUT request to `/api/users/{id}/role` with role `REGISTERED_USER` for an active user
- **THEN** the system SHALL update the user's role to `REGISTERED_USER` and return HTTP 200 with the updated user profile

#### Scenario: Non-admin attempts role assignment
- **WHEN** a user with role `REGISTERED_USER` sends a PUT request to `/api/users/{id}/role`
- **THEN** the system SHALL return HTTP 403 Forbidden

#### Scenario: Assign role to non-existent user
- **WHEN** an admin sends a PUT request to `/api/users/{id}/role` for an ID that does not exist or belongs to an inactive user
- **THEN** the system SHALL return HTTP 404 Not Found with message "User not found"

#### Scenario: Assign invalid role
- **WHEN** an admin sends a PUT request to `/api/users/{id}/role` with an invalid role value
- **THEN** the system SHALL return HTTP 400 Bad Request with a validation error
