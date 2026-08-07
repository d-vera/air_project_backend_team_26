## Why

The current implementation stores user UI preferences (`preferred_theme` and `preferred_language`) directly inside the `users` table. Decoupling user settings into a dedicated `user_preferences` table modularizes the database schema, separates authentication/identity concerns from configuration settings, and provides a clean domain structure with Option A defaults (`Theme.SYSTEM`, `Language.ES`).

## What Changes

- **Database**: Add `user_preferences` table with `id`, `user_id` (FK to `users.id`, UNIQUE), `language` (VARCHAR(10) DEFAULT `'ES'`), `theme` (VARCHAR(10) DEFAULT `'SYSTEM'`), `active` (BOOLEAN DEFAULT TRUE), `created_at`, and `updated_at`. Remove legacy `preferred_theme` and `preferred_language` columns from `users`.
- **Domain Model**: Create `Theme` (`DARK`, `LIGHT`, `SYSTEM`) and `Language` (`ES`, `EN`) Enums. Create `UserPreference` entity (defaulting to `Theme.SYSTEM` and `Language.ES`), repository, and service. Remove legacy preference fields from `User` entity.
- **API Endpoints**: Move preference management from `/api/users/me/preferences` to dedicated `PreferenceController` mapped to `/api/preferences/me`:
  - `GET /api/preferences/me`: Retrieve user preferences (`verPerfil`).
  - `PATCH /api/preferences/me`: Partially update user preferences (`actualizarPerfil`).
- **DTOs**: Replace old preference fields with `PreferenceResponse` (`id`, `language`, `theme`, `active`) and `UpdatePreferenceRequest` (`language`, `theme`, `active`).

## Capabilities

### New Capabilities

*(None)*

### Modified Capabilities

- `user-preferences`: Extract user preferences into dedicated `user_preferences` table and `PreferenceController` (`/api/preferences/me`), using `Theme` (`DARK`, `LIGHT`, `SYSTEM`) and `Language` (`ES`, `EN`) Enums, with Option A default values (`Theme.SYSTEM`, `Language.ES`).

## Impact

- Database schema migration in `schema.sql`.
- JPA model modifications (`User.java`, new `UserPreference.java`, `Theme.java`, `Language.java`).
- `UserController`, `UserService`, and associated test suites updated to use `PreferenceController` and `UserPreferenceService`.
- OpenAPI / Swagger documentation updated to reflect the new `/api/preferences` endpoints.
