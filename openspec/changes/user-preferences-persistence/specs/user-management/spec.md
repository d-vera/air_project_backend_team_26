## MODIFIED Requirements

### Requirement: View own profile
The system SHALL allow authenticated users (any role) to retrieve their own profile via `GET /api/users/me`. The response SHALL include id, email, firstName, lastName, role, active status, preferredTheme, preferredLanguage, createdAt, and updatedAt. The password SHALL NOT be included in the response.

#### Scenario: Authenticated user views own profile
- **WHEN** an authenticated user sends a GET request to `/api/users/me`
- **THEN** the system SHALL return HTTP 200 with the user's profile data including `preferredTheme` and `preferredLanguage` (excluding password)
