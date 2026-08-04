## 1. Database & Domain Model Setup

- [x] 1.1 Update `schema.sql` database initialization script to add `preferred_theme` (`VARCHAR(10) DEFAULT 'DARK'`) and `preferred_language` (`VARCHAR(5) DEFAULT 'es'`) to the `users` table.
- [x] 1.2 Update JPA `User.java` entity model with `@Column` definitions for `preferredTheme` and `preferredLanguage`.

## 2. Backend DTO & Controller Implementation

- [x] 2.1 Update `UserResponse.java` DTO to include `preferredTheme` and `preferredLanguage` fields.
- [x] 2.2 Create `UpdatePreferencesRequest.java` DTO with validation annotations (`@Pattern` for theme and language values).
- [x] 2.3 Update `UserService.java` to handle fetching and updating user preference fields.
- [x] 2.4 Add `PATCH /api/users/me/preferences` endpoint to `UserController.java` with Swagger OpenAPI annotations.

## 3. Automated & Verification Testing

- [x] 3.1 Write unit and integration tests for `UserService` and `UserController` verifying `GET /api/users/me` profile responses and `PATCH /api/users/me/preferences` updates.
- [x] 3.2 Verify Spring Boot application compilation and test suite execution.
