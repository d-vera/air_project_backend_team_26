## Context

In the Admin User Management panel, administrators need complete visibility over the entire user directory, including active and deactivated accounts. Soft deletion (`DELETE /api/users/{id}`) sets `active = false`. 

Currently, `GET /api/users`, `GET /api/users/{id}`, and `PUT /api/users/{id}` strictly query active records via `findAllByActiveTrue()` and `findByIdAndActiveTrue(id)`. Consequently:
1. `GET /api/users` excludes deactivated users, preventing administrators from viewing the full user list and auditing deactivated accounts.
2. `GET /api/users/{id}` returns 404 for deactivated users.
3. `PUT /api/users/{id}` cannot find inactive users, and `UpdateUserRequest` does not support updating the `active` status field directly.

## Goals / Non-Goals

**Goals:**
- Update `GET /api/users` to return all users (both `active=true` and `active=false`) for `ADMIN` role.
- Support retrieving inactive users by ID for admins (`GET /api/users/{id}`).
- Support updating and reactivating users via `PUT /api/users/{id}` (e.g. supporting an optional `Boolean active` in `UpdateUserRequest` and/or activating via update).
- Keep `UserResponse` DTO returning `"active": boolean` (already present, ensuring it accurately reflects entity state).
- Ensure reactivated users (`active = true`) can authenticate via `CustomUserDetailsService.loadUserByUsername` and `/api/auth/login`.

**Non-Goals:**
- Self-service endpoints (`GET /api/users/me`, `PUT /api/users/me`) should continue operating only on active users or the authenticated principal.
- Modifying soft-delete behavior (`DELETE /api/users/{id}` sets `active=false`).

## Decisions

1. **Repository Query Methods**:
   - Use standard JPA `userRepository.findAll()` in `UserService.getAllUsers()` for admin listing instead of `findAllByActiveTrue()`.
   - Use standard JPA `userRepository.findById(id)` in `UserService.getUserById(id)`, `UserService.updateUser(id, ...)`, and `UserService.assignRole(id, ...)` so admins can view, modify, and assign roles to both active and inactive users.
   - For `deleteUser(id)`, if the user is already inactive or not found: `userRepository.findById(id)` is fetched; if not found throw `UserNotFoundException`. If already inactive, handle per spec requirements (return 404 or idempotently ensure inactive).

2. **Reactivation Support in DTO & Service**:
   - Add `private Boolean active;` (optional) to `UpdateUserRequest`.
   - In `UserService.updateUser(Long id, UpdateUserRequest request)`:
     - Check if `request.getActive() != null`, and if so, call `user.setActive(request.getActive())`.
     - Update name and password if present as before.
     - Save and return updated `UserResponse`.
   - In `UserController.java`: Update OpenAPI documentation on `GET /api/users` from "List all active users" to "List all users" to accurately describe the behavior.

3. **Authentication Verification**:
   - `CustomUserDetailsService` loads users with `findByEmailAndActiveTrue(email)`.
   - When a user is reactivated with `active = true`, they will immediately be loadable by `CustomUserDetailsService` and able to log in.

## Risks / Trade-offs

- **[Risk]** Breaking assumptions of existing callers expecting only active users from `GET /api/users`.
  - **Mitigation:** The frontend admin panel is explicitly designed to handle active/inactive statuses using the `active` boolean field on `UserResponse`.
- **[Risk]** Non-admins listing deactivated users.
  - **Mitigation:** `GET /api/users` is guarded by `.requestMatchers("/api/users/**").hasRole("ADMIN")` in `SecurityConfig`.
