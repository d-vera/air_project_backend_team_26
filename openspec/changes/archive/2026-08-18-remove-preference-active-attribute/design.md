## Context

The `user_preferences` table and associated models currently define an `active` boolean column (`active = true` by default). Since user preferences are 1:1 bound to a user account and created automatically with default values, the `active` flag has no business meaning or operational utility. Removing it streamlines the DTOs, entity, tests, and database schema.

## Goals / Non-Goals

**Goals:**
- Completely remove the `active` attribute from the `UserPreference` entity model.
- Remove `active` from `PreferenceResponse` and `UpdatePreferenceRequest` DTOs.
- Remove `active` column from `user_preferences` table in `schema.sql` and any test fixture data (`data.sql`).
- Update `UserPreferenceService` update logic to only handle `theme` and `language`.
- Update all associated unit and integration tests.

**Non-Goals:**
- Modifying `language` or `theme` enum definitions or defaults (`Language.ES`, `Theme.SYSTEM`).
- Altering the REST endpoint routes (`GET /api/preferences`, `PUT /api/preferences`, or `/api/preferences/me`).

## Decisions

### Decision 1: Direct removal from schema and entity
Remove the column `active` from `user_preferences` table definition in `schema.sql`.
- **Rationale**: Keeps database schema clean and aligned with the simplified JPA model.

### Decision 2: Remove `active` from update and response DTOs
Remove `private Boolean active;` from `PreferenceResponse` and `UpdatePreferenceRequest`.
- **Rationale**: Eliminates dead payload attributes in API contracts and Swagger OpenAPI documentation.

## Risks / Trade-offs

- **[Risk]** Breaking change for any API consumers passing or expecting `active` field. → **Mitigation**: Update API documentation and ensure response objects clearly reflect the current fields (`id`, `language`, `theme`).
