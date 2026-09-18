## MODIFIED Requirements

### Requirement: Admin list all users
The system SHALL allow users with role `ADMIN` to retrieve all users (both active and inactive) via `GET /api/users`. The response SHALL be a list of user profiles (excluding passwords) with their respective `active` status.

#### Scenario: Admin lists all users
- **WHEN** an admin sends a GET request to `/api/users`
- **THEN** the system SHALL return HTTP 200 with a list of all users including those with `active=true` and `active=false`

#### Scenario: Non-admin attempts to list users
- **WHEN** a user with role `REGISTERED_USER` sends a GET request to `/api/users`
- **THEN** the system SHALL return HTTP 403 Forbidden

### Requirement: Admin get user by ID
The system SHALL allow users with role `ADMIN` to retrieve a specific user (active or inactive) by ID via `GET /api/users/{id}`.

#### Scenario: Admin gets active user by valid ID
- **WHEN** an admin sends a GET request to `/api/users/{id}` with an existing active user's ID
- **THEN** the system SHALL return HTTP 200 with the user's profile

#### Scenario: Admin gets inactive user by valid ID
- **WHEN** an admin sends a GET request to `/api/users/{id}` with an existing inactive user's ID
- **THEN** the system SHALL return HTTP 200 with the user's profile having `active=false`

#### Scenario: Admin gets user by non-existent ID
- **WHEN** an admin sends a GET request to `/api/users/{id}` with an ID that does not exist in the system
- **THEN** the system SHALL return HTTP 404 Not Found with message "User not found with id: {id}"

### Requirement: Admin update user
The system SHALL allow users with role `ADMIN` to update any user's profile and active status via `PUT /api/users/{id}`. Admins SHALL be able to update firstName, lastName, password, and active status (enabling user reactivation).

#### Scenario: Admin updates user profile
- **WHEN** an admin sends a PUT request to `/api/users/{id}` with updated fields
- **THEN** the system SHALL update the fields, set updatedAt, and return HTTP 200 with the updated profile

#### Scenario: Admin reactivates an inactive user
- **WHEN** an admin sends a PUT request to `/api/users/{id}` for an inactive user with `"active": true`
- **THEN** the system SHALL update `active` to `true`, set updatedAt, return HTTP 200 with the updated profile, and allow the user to authenticate successfully
