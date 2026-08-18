## Why

The `active` attribute on user preferences is redundant and unnecessary because preference records are permanently linked to a user account and are always active when present. Removing this field simplifies the domain model, API contracts, database schema, and frontend integration.

## What Changes

- **BREAKING**: Remove `active` field from `UserPreference` entity model.
- **BREAKING**: Remove `active` field from `PreferenceResponse` and `UpdatePreferenceRequest` DTOs.
- Remove `active` column from database schema (`user_preferences` table in `schema.sql` and `data.sql`).
- Update `UserPreferenceService`, `PreferenceController`, and associated tests to remove references to `active`.
- Update OpenAPI schema documentation to remove the `active` attribute.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `user-preferences`: Remove the `active` attribute from user preference requirements, scenarios, request/response models, and default initialization.

## Impact

- **Database**: Remove `active` column from `user_preferences` table definition.
- **API Models**: `PreferenceResponse` and `UpdatePreferenceRequest` will no longer include the `active` field.
- **Backend Code**: `UserPreference`, `UserPreferenceService`, `PreferenceResponse`, `UpdatePreferenceRequest`, and controller/service unit tests.
