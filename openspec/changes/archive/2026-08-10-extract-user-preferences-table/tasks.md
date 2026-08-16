## 1. Database Schema & Migration

- [x] 1.1 Update `schema.sql` to create `user_preferences` table with `id`, `user_id` (FK, UNIQUE), `language`, `theme` (BOOLEAN), `active` (BOOLEAN), `created_at`, `updated_at`.
- [x] 1.2 Remove legacy `preferred_theme` and `preferred_language` columns from `users` table definition in `schema.sql`.

## 2. Domain Entities & Repositories

- [x] 2.1 Create `UserPreference` entity class mapped to `user_preferences` table with `@OneToOne` user relationship.
- [x] 2.2 Create `UserPreferenceRepository` interface for fetching and persisting user preferences.
- [x] 2.3 Update `User` entity class to remove `preferredTheme` and `preferredLanguage` fields and add `UserPreference` relationship.

## 3. DTOs & Service Layer

- [x] 3.1 Create `PreferenceResponse` and `UpdatePreferenceRequest` DTOs with English fields (`language`, `theme`, `active`).
- [x] 3.2 Update `UserResponse` DTO to remove legacy preference fields.
- [x] 3.3 Create `UserPreferenceService` with `getPreferencesByUserEmail` and `updatePreferences` methods.
- [x] 3.4 Update `UserService` and `UserRegistrationService` (if applicable) to initialize default preferences upon user creation.

## 4. Controllers & Security

- [x] 4.1 Create `PreferenceController` mapped to `/api/preferences` with `GET /api/preferences/me` and `PATCH /api/preferences/me` endpoints.
- [x] 4.2 Remove preference endpoints from `UserController`.

## 5. Verification & Tests

- [x] 5.1 Update or create unit tests for `UserPreferenceServiceTest` and `PreferenceControllerTest`.
- [x] 5.2 Update `UserControllerTest` and `UserServiceTest` to match updated `UserResponse`.
- [x] 5.3 Verify project builds cleanly with `mvn test` (or `mvn clean test`).
