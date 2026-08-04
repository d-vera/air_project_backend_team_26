## Context

Currently, theme (Dark/Light) and language (en/es) settings are handled purely on the client side without backend persistence. While frontend `localStorage` provides immediate caching per browser session, it does not persist user preferences across different devices or browser instances when an authenticated user logs into their account.

By adding `preferredTheme` and `preferredLanguage` to the backend `users` entity and exposing a dedicated preference update endpoint (`PATCH /api/users/me/preferences`), the application achieves cross-device synchronization while preserving fast local frontend rendering.

## Goals / Non-Goals

**Goals:**
- Add `preferred_theme` and `preferred_language` columns to the database `users` table.
- Include preference fields in `UserResponse` DTO returned by `GET /api/users/me`.
- Provide a `PATCH /api/users/me/preferences` REST endpoint to allow users to update their theme and language settings.
- Enable full-stack integration: Angular frontend updates `localStorage` immediately for zero latency and syncs with backend asynchronously.

**Non-Goals:**
- Storing unauthenticated guest user preferences in the backend database (guests continue to use `localStorage` and OS system defaults).
- Storing additional non-UI user settings in this iteration.

## Decisions

### Decision 1: Extend `users` table with columns instead of creating a separate `user_preferences` table
- **Choice**: Add `preferred_theme` (`VARCHAR(10) DEFAULT 'DARK'`) and `preferred_language` (`VARCHAR(5) DEFAULT 'es'`) directly to `users` table.
- **Rationale**: Keeps database schema simple and avoids extra JOIN operations on every profile fetch (`GET /api/users/me`).
- **Alternative Considered**: Creating a `user_preferences` table with a foreign key to `users`. Rejected due to unnecessary relational overhead for two small scalar settings.

### Decision 2: Partial updates via `PATCH /api/users/me/preferences`
- **Choice**: Expose `PATCH /api/users/me/preferences` allowing partial updates (e.g. updating theme without changing language, or updating both).
- **Rationale**: Frontend components (Theme toggle vs Language selector) trigger actions independently.
- **Alternative Considered**: Requiring full `PUT /api/users/me` with all profile fields. Rejected because updating theme shouldn't require sending user's name or password payload.

### Decision 3: Local-first cache with background sync on Frontend
- **Choice**: Angular `ThemeService` and `LanguageService` read/write `localStorage` immediately, and send HTTP `PATCH` in the background when authenticated.
- **Rationale**: Eliminates page flicker and waiting for network requests during UI interaction.

## Risks / Trade-offs

- **[Risk] Sync conflict when switching devices** → Mitigation: `localStorage` is overwritten with backend values upon successful login (`POST /api/auth/login` or `GET /api/users/me`).
- **[Trade-off] Database column defaults vs null values** → Mitigation: Default `preferred_theme = 'DARK'` and `preferred_language = 'es'` in PostgreSQL table schema and JPA `@Builder.Default` annotations.
