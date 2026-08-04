## Why

Currently, frontend user preferences (UI theme like Light/Dark and language like English/Spanish) are not saved in the backend database. While client-side storage can store local settings, persisting user preferences on the backend enables cross-device synchronization upon authentication and ensures a consistent user experience regardless of device or browser session.

## What Changes

- Add `preferred_theme` (default `'DARK'`) and `preferred_language` (default `'es'`) columns to the `users` database table.
- Update Spring Boot JPA `User` entity to include `preferredTheme` and `preferredLanguage` properties.
- Enhance `UserResponse` DTO and `GET /api/users/me` endpoint to return user preference fields.
- Introduce `PATCH /api/users/me/preferences` endpoint to allow authenticated users to update their theme and language preferences.
- Support Angular frontend integration by caching user preferences in `localStorage` while synchronizing with the backend REST API upon login and preference toggling.

## Capabilities

### New Capabilities
- `user-preferences`: Backend endpoint (`PATCH /api/users/me/preferences`) and domain model support for retrieving and updating user UI preferences (Theme: `DARK`/`LIGHT`, Language: `en`/`es`).

### Modified Capabilities
- `user-management`: Update `GET /api/users/me` profile responses to include `preferredTheme` and `preferredLanguage` fields alongside existing user profile attributes.

## Impact

- **Database**: SQL migration adding `preferred_theme` and `preferred_language` columns to `users` table.
- **Backend**: Update `User.java`, `UserResponse.java`, `UserService.java`, `UserController.java`, and create request DTO `UpdatePreferencesRequest.java`.
- **Frontend**: Angular `ThemeService` and `LanguageService` update to sync with backend REST API while retaining local fallback behavior.
- **API Specs**: Add Swagger/OpenAPI documentation for the new `PATCH /api/users/me/preferences` endpoint.
