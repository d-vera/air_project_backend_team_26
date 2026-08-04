## Purpose
Manages user account profiles, authentication state, and administrative user operations.
## Requirements
### Requirement: View own profile
The system SHALL allow authenticated users (any role) to retrieve their own profile via `GET /api/users/me`. The response SHALL include id, email, firstName, lastName, role, active status, preferredTheme, preferredLanguage, createdAt, and updatedAt. The password SHALL NOT be included in the response.

#### Scenario: Authenticated user views own profile
- **WHEN** an authenticated user sends a GET request to `/api/users/me`
- **THEN** the system SHALL return HTTP 200 with the user's profile data including `preferredTheme` and `preferredLanguage` (excluding password)

### Requirement: Update own profile
The system SHALL allow authenticated users (any role) to update their own profile via `PUT /api/users/me`. Users SHALL be able to update their firstName, lastName, and/or password. The email and role SHALL NOT be modifiable by the user.

#### Scenario: Update own first name and last name
- **WHEN** an authenticated user sends a PUT request to `/api/users/me` with new firstName and lastName
- **THEN** the system SHALL update the fields, set updatedAt to the current timestamp, and return HTTP 200 with the updated profile

#### Scenario: Update own password
- **WHEN** an authenticated user sends a PUT request to `/api/users/me` with a new password (min 8 characters)
- **THEN** the system SHALL hash the new password with BCrypt, update it, and return HTTP 200

#### Scenario: Update with invalid password
- **WHEN** an authenticated user sends a PUT request with a password shorter than 8 characters
- **THEN** the system SHALL return HTTP 400 Bad Request with a validation error

### Requirement: Admin list all users
The system SHALL allow users with role `ADMIN` to retrieve all active users via `GET /api/users`. The response SHALL be a list of user profiles (excluding passwords).

#### Scenario: Admin lists all users
- **WHEN** an admin sends a GET request to `/api/users`
- **THEN** the system SHALL return HTTP 200 with a list of all users where `active=true`

#### Scenario: Non-admin attempts to list users
- **WHEN** a user with role `REGISTERED_USER` sends a GET request to `/api/users`
- **THEN** the system SHALL return HTTP 403 Forbidden

### Requirement: Admin get user by ID
The system SHALL allow users with role `ADMIN` to retrieve a specific active user by ID via `GET /api/users/{id}`.

#### Scenario: Admin gets user by valid ID
- **WHEN** an admin sends a GET request to `/api/users/{id}` with an existing active user's ID
- **THEN** the system SHALL return HTTP 200 with the user's profile

#### Scenario: Admin gets user by non-existent ID
- **WHEN** an admin sends a GET request to `/api/users/{id}` with an ID that does not exist or belongs to an inactive user
- **THEN** the system SHALL return HTTP 404 Not Found with message "User not found"

### Requirement: Admin update user
The system SHALL allow users with role `ADMIN` to update any active user's profile via `PUT /api/users/{id}`. Admins SHALL be able to update firstName, lastName, and password.

#### Scenario: Admin updates user profile
- **WHEN** an admin sends a PUT request to `/api/users/{id}` with updated fields
- **THEN** the system SHALL update the fields, set updatedAt, and return HTTP 200 with the updated profile

### Requirement: Admin soft-delete user
The system SHALL allow users with role `ADMIN` to soft-delete a user via `DELETE /api/users/{id}`. This SHALL set the user's `active` field to `false`. The user record SHALL remain in the database.

#### Scenario: Admin deletes a user
- **WHEN** an admin sends a DELETE request to `/api/users/{id}` for an active user
- **THEN** the system SHALL set `active=false`, and return HTTP 200 with message "User deleted successfully"

#### Scenario: Admin deletes already-inactive user
- **WHEN** an admin sends a DELETE request to `/api/users/{id}` for a user that is already inactive
- **THEN** the system SHALL return HTTP 404 Not Found with message "User not found"

