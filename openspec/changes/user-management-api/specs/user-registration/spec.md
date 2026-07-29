## ADDED Requirements

### Requirement: Visitor registration
The system SHALL allow unauthenticated visitors to register by providing an email, password, first name, and last name via `POST /api/auth/register`. Upon successful registration, the system SHALL hash the password with BCrypt, create a new user with role `REGISTERED_USER` and `active=true`, persist it to the database, and return a JWT token with the user's email and role.

#### Scenario: Successful registration
- **WHEN** a visitor sends a valid registration request with a unique email, password (min 8 characters), first name, and last name
- **THEN** the system SHALL create the user, return HTTP 200 with a JWT token, token type "Bearer", email, and role "REGISTERED_USER"

#### Scenario: Registration with existing email
- **WHEN** a visitor sends a registration request with an email that already exists in the database
- **THEN** the system SHALL return HTTP 409 Conflict with message "Email is already registered"

#### Scenario: Registration with invalid data
- **WHEN** a visitor sends a registration request with missing or invalid fields (blank email, invalid email format, password shorter than 8 characters, blank first name, or blank last name)
- **THEN** the system SHALL return HTTP 400 Bad Request with field-level validation error messages
