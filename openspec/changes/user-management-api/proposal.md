## Why

The Air Project backend currently has no user management or authentication system. The system needs to support visitor registration, authenticated user operations, and administrative user management (CRUD, role assignment, soft-delete) as defined in the "Módulo de Gestión de Usuarios" use case. Without this, the API is completely open with no access control.

## What Changes

- Add a `User` entity with email, password (BCrypt-hashed), firstName, lastName, role, and active (soft-delete) fields
- Add JWT-based authentication (register, login, logout with token blacklist)
- Add role-based authorization with two roles: `REGISTERED_USER` and `ADMIN`
- Add REST endpoints for user self-management (`/api/users/me`) and admin user management (`/api/users`, `/api/users/{id}`, `/api/users/{id}/role`)
- Add Spring Security configuration with JWT filter
- Add Swagger/OpenAPI 3 documentation via springdoc
- Add global exception handling for validation errors, duplicate emails, unauthorized access, and not-found cases

## Capabilities

### New Capabilities
- `user-registration`: User registration with email validation and password hashing. Visitors can create accounts via `POST /api/auth/register`
- `user-authentication`: JWT-based login and logout. Login returns a Bearer token; logout blacklists the token. All admin and self-management endpoints require a valid token
- `user-management`: Admin CRUD operations on users — list all, get by ID, update, and soft-delete (set `active=false`). Registered users can view and update their own profile
- `role-assignment`: Admin can assign roles (`REGISTERED_USER`, `ADMIN`) to any user via `PUT /api/users/{id}/role`
- `api-documentation`: Swagger UI at `/swagger-ui.html` with OpenAPI 3 spec documenting all endpoints, request/response schemas, and JWT security scheme

### Modified Capabilities

_None — this is a new module with no changes to existing specs._

## Impact

- **New dependencies**: `spring-boot-starter-security`, `spring-boot-starter-validation`, `jjwt` (0.12.6), `springdoc-openapi-starter-webmvc-ui` (2.8.8), `lombok`
- **Database**: New `users` table with unique email constraint
- **Existing endpoints**: Currently unsecured MQTT ingestion is not affected (no controllers exist yet for air quality data)
- **Configuration**: New `jwt.secret` and `jwt.expiration-ms` properties in `application.properties`
- **Security**: Spring Security filter chain will be configured — public endpoints (register, login, Swagger) are explicitly permitted; all others require authentication
