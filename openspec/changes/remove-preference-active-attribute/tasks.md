## 1. Domain Model and DTOs

- [x] 1.1 Remove `active` field, builder defaults, and getters/setters from `UserPreference.java`
- [x] 1.2 Remove `active` field from `PreferenceResponse.java` and update `fromEntity` mapping
- [x] 1.3 Remove `active` field from `UpdatePreferenceRequest.java`

## 2. Service and Controller Layer

- [x] 2.1 Update `UserPreferenceService.java` to remove references to `active` in preference update and creation logic
- [x] 2.2 Update OpenAPI summary / description annotations in `PreferenceController.java` if needed

## 3. Database Schema and Fixtures

- [x] 3.1 Remove `active` column from `user_preferences` table in `schema.sql`
- [x] 3.2 Update any seed/fixture scripts in `data.sql` to remove the `active` column values

## 4. Tests and Verification

- [x] 4.1 Update `UserPreferenceServiceTest.java` to remove assertions on `active`
- [x] 4.2 Update `PreferenceControllerTest.java` to remove assertions on `active`
- [x] 4.3 Run full test suite `./mvnw test` to ensure all tests pass
