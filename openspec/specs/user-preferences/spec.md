# user-preferences Specification

## Purpose
Manages user UI preferences such as theme and language selection.
## Requirements
### Requirement: Update own preferences
The system SHALL allow authenticated users (any role, including non-admin registered users) to update their UI preferences via `PATCH /api/users/me/preferences`. The payload MAY contain `preferredTheme` (`DARK` or `LIGHT`) and/or `preferredLanguage` (`en` or `es`). The system SHALL update only the provided fields and return the updated user profile. Access to `/api/users/me/**` SHALL NOT be restricted to `ROLE_ADMIN`.

#### Scenario: Update preferred theme to LIGHT
- **WHEN** an authenticated user sends a PATCH request to `/api/users/me/preferences` with `{"preferredTheme": "LIGHT"}`
- **THEN** the system SHALL update `preferred_theme` to `"LIGHT"`, set `updatedAt` timestamp, and return HTTP 200 with updated profile

#### Scenario: Update preferred language to es
- **WHEN** an authenticated user sends a PATCH request to `/api/users/me/preferences` with `{"preferredLanguage": "es"}`
- **THEN** the system SHALL update `preferred_language` to `"es"`, set `updatedAt` timestamp, and return HTTP 200 with updated profile

#### Scenario: Update both theme and language
- **WHEN** an authenticated user sends a PATCH request to `/api/users/me/preferences` with `{"preferredTheme": "DARK", "preferredLanguage": "en"}`
- **THEN** the system SHALL update both fields and return HTTP 200 with updated profile

#### Scenario: Update preferences with invalid theme value
- **WHEN** an authenticated user sends a PATCH request to `/api/users/me/preferences` with `{"preferredTheme": "NEON"}`
- **THEN** the system SHALL return HTTP 400 Bad Request with a validation error message

#### Scenario: Unauthenticated user updates preferences
- **WHEN** an unauthenticated request is sent to `PATCH /api/users/me/preferences`
- **THEN** the system SHALL return HTTP 401 Unauthorized

