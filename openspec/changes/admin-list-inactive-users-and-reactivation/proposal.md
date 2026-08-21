## Why

In the Admin User Management panel, administrators require full visibility over all users in the system, including deactivated and soft-deleted accounts, to effectively audit the user base and re-enable/reactivate accounts when needed.

Currently, `GET /api/users` filters out inactive records and only returns users where `active = true`. Consequently, administrators cannot view deactivated accounts in the user list, nor can they reactivate them via administrative updates.

## What Changes

- Update `GET /api/users` (Admin user listing endpoint) to return all user records regardless of their `active` state (both `active = true` and `active = false`).
- Ensure `UserResponse` DTO includes the accurate `active` boolean field for each user.
- Allow administrators to retrieve inactive users by ID and update/reactivate them (setting `active = true` via `PUT /api/users/{id}`) so reactivated accounts can log in and use the platform again.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `user-management`: Update `GET /api/users` to return all users (including inactive ones) instead of filtering by `active=true`, update `GET /api/users/{id}` / `PUT /api/users/{id}` to allow accessing and reactivating inactive users.

## Impact

- `UserController`: Admin endpoints for retrieving all users and updating users.
- `UserService` & `UserServiceImpl`: User retrieval and update methods to include inactive users in admin queries and allow setting `active = true`.
- `UserRepository`: Queries for finding all users and finding by ID in admin context.
- Unit and integration tests for admin user management and reactivation.
