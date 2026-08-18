## MODIFIED Requirements

### Requirement: Update own preferences
The system SHALL allow authenticated users (any role) to view and update their UI preferences via `GET /api/preferences/me` and `PATCH /api/preferences/me`. Newly registered users SHALL be initialized with default preferences: `theme = SYSTEM` and `language = ES`. The payload MAY contain `language` (`ES` or `EN`) and/or `theme` (`DARK`, `LIGHT`, or `SYSTEM`). The system SHALL update only the provided fields and return the updated `PreferenceResponse`.

#### Scenario: Get own preferences
- **WHEN** an authenticated user sends a GET request to `/api/preferences/me`
- **THEN** the system SHALL return HTTP 200 with the user's `PreferenceResponse` containing `id`, `language`, and `theme`

#### Scenario: Newly registered user default preferences
- **WHEN** a new user account is created
- **THEN** the system SHALL initialize default preferences with `theme = SYSTEM` and `language = ES`

#### Scenario: Update preferred theme to SYSTEM
- **WHEN** an authenticated user sends a PATCH request to `/api/preferences/me` with `{"theme": "SYSTEM"}`
- **THEN** the system SHALL update `theme` to `"SYSTEM"`, set `updatedAt` timestamp, and return HTTP 200 with updated `PreferenceResponse`

#### Scenario: Update preferred language to EN
- **WHEN** an authenticated user sends a PATCH request to `/api/preferences/me` with `{"language": "EN"}`
- **THEN** the system SHALL update `language` to `"EN"`, set `updatedAt` timestamp, and return HTTP 200 with updated `PreferenceResponse`

#### Scenario: Update both theme and language
- **WHEN** an authenticated user sends a PATCH request to `/api/preferences/me` with `{"theme": "LIGHT", "language": "ES"}`
- **THEN** the system SHALL update all fields and return HTTP 200 with updated `PreferenceResponse`

#### Scenario: Update preferences with invalid theme value
- **WHEN** an authenticated user sends a PATCH request to `/api/preferences/me` with `{"theme": "NEON"}`
- **THEN** the system SHALL return HTTP 400 Bad Request with a validation error message

#### Scenario: Unauthenticated user updates preferences
- **WHEN** an unauthenticated request is sent to `GET /api/preferences/me` or `PATCH /api/preferences/me`
- **THEN** the system SHALL return HTTP 401 Unauthorized
