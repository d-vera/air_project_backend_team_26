## Context

Currently, `User` entity and `users` table store UI preference fields (`preferred_theme` and `preferred_language`) directly. To follow separation of concerns, we are extracting preference management into a standalone `user_preferences` table with a 1:1 foreign key relationship to `users`, managed by a dedicated `PreferenceController` mapped to `/api/preferences`.

## Goals / Non-Goals

**Goals:**
- Create `user_preferences` table in SQL database with `id`, `user_id` (FK to `users.id`, UNIQUE), `language` (VARCHAR(10) DEFAULT `'ES'`), `theme` (VARCHAR(10) DEFAULT `'SYSTEM'`), `active` (BOOLEAN DEFAULT TRUE), `created_at`, and `updated_at`.
- Create `Theme` (`DARK`, `LIGHT`, `SYSTEM`) and `Language` (`ES`, `EN`) Java Enums.
- Create `UserPreference` entity (defaulting to `Theme.SYSTEM` and `Language.ES`) and `UserPreferenceRepository`.
- Implement `UserPreferenceService` and `PreferenceController` mapped to `/api/preferences`.
- Provide `GET /api/preferences/me` (`verPerfil`) and `PATCH /api/preferences/me` (`actualizarPerfil`).
- Remove legacy preference columns from `users` table and `User` JPA model.

**Non-Goals:**
- Managing preferences for unauthenticated guests.
- Changing authentication mechanisms or non-preference endpoints in `UserController`.

## Decisions

- **Option A Defaults (`Theme.SYSTEM`, `Language.ES`)**: Newly registered users start with `Theme.SYSTEM` to adapt automatically to their OS theme (Dark/Light), and `Language.ES` as default locale.
- **Dedicated Table & Entity (`user_preferences` / `UserPreference`)**: Keeps authentication data separate from UI configuration.
- **Theme & Language Enums**: Uses `Theme` (`DARK`, `LIGHT`, `SYSTEM`) and `Language` (`ES`, `EN`) Enums for type safety, validation, and auto-theme support.
- **English Variable Names**: All Java fields, DTO attributes, DB columns, and endpoints are named strictly in English (`language`, `theme`, `active`).
- **Dedicated Controller (`/api/preferences`)**: Exposes REST endpoints at `/api/preferences/me` instead of keeping them nested in `/api/users/me/preferences`.

## Risks / Trade-offs

- **Breaking API Change**: Replacing `/api/users/me/preferences` with `/api/preferences/me` breaks clients relying on the old path or old JSON fields (`preferredTheme`, `preferredLanguage`).
  - *Mitigation*: Update unit/integration tests and Swagger OpenAPI documentation to reflect the new endpoints.
