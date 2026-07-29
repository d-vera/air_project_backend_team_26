## Context

The Air Project backend is a Spring Boot 4.1 application (Java 21, Maven) with PostgreSQL/TimescaleDB for air quality data ingestion via MQTT. Currently there is no user entity, no authentication, no authorization, and no REST controllers for user operations. The project needs a complete user management module to support three actor types: unauthenticated visitors (registration), registered users (self-management), and administrators (user CRUD, role assignment).

Existing architecture: JPA entities + repositories, MQTT ingestion via Paho, Jackson serialization. No Spring Security dependency exists yet.

## Goals / Non-Goals

**Goals:**
- JWT-based stateless authentication suitable for REST API clients
- Role-based authorization (`REGISTERED_USER`, `ADMIN`) enforced at the Spring Security filter chain level
- Soft-delete pattern via `active` boolean field (no physical record deletion)
- Swagger/OpenAPI 3 documentation with JWT security scheme
- Global exception handling with consistent error response format
- BCrypt password hashing

**Non-Goals:**
- OAuth2 / social login integration
- Email verification or password reset flow
- Node management (separate module)
- Full RBAC with granular permissions — simple enum-based roles are sufficient
- Redis-backed token blacklist (in-memory is acceptable for now)
- Rate limiting on authentication endpoints
- Frontend / UI

## Decisions

### 1. JWT for authentication (over session-based)

**Choice**: Stateless JWT tokens with Bearer scheme.
**Rationale**: The API is designed for REST clients (mobile, IoT dashboards). JWT eliminates server-side session state and works naturally with the `Authorization: Bearer` header pattern. Swagger UI also supports this scheme natively.
**Alternatives considered**:
- Session-based auth: Simpler logout, but requires server-side state and doesn't fit REST conventions.
- OAuth2 Resource Server: Overkill for an internal project with two roles.

### 2. In-memory token blacklist for logout

**Choice**: `ConcurrentHashMap` storing invalidated JTI (JWT ID) claims until expiration.
**Rationale**: Keeps the implementation simple. The blacklist grows slowly (one entry per logout, entries auto-expire). For the expected user base, memory impact is negligible.
**Trade-off**: Token blacklist is lost on server restart — users who logged out before restart would still have valid tokens until expiration.
**Alternative**: Redis-backed blacklist (future enhancement if needed).

### 3. Soft-delete via `active` field (not physical delete)

**Choice**: `active` boolean column, default `true`. Admin "delete" sets `active=false`. All queries filter by `active=true`.
**Rationale**: Preserves audit trail and allows account recovery. The user's requirement explicitly maps `active=false` to "deleted from the system."

### 4. Simple enum roles (not RBAC tables)

**Choice**: `Role` enum with `REGISTERED_USER` and `ADMIN` values stored as a string column.
**Rationale**: Only two roles exist. A full role-permission table structure adds unnecessary complexity. The enum approach is easy to extend if a third role is needed later.

### 5. springdoc-openapi for Swagger

**Choice**: `springdoc-openapi-starter-webmvc-ui` 2.8.8.
**Rationale**: Auto-generates OpenAPI 3 spec from Spring annotations. Swagger UI served at `/swagger-ui.html`. Integrates with Spring Security's `@SecurityRequirement` annotations for documenting protected endpoints.

### 6. Lombok for boilerplate reduction

**Choice**: Add Lombok dependency for `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on DTOs and entities.
**Rationale**: Reduces boilerplate significantly. The project uses Java 21 but doesn't use records for JPA entities (JPA requires mutable entities with no-arg constructors).

### 7. Package structure

```
com.airproject.airproject/
├── controller/        # REST controllers (AuthController, UserController)
├── dto/               # Request/response DTOs
├── exception/         # Global exception handler + custom exceptions
├── model/             # JPA entities (User) + enums (Role)
├── repository/        # JPA repositories (UserRepository)
├── security/          # JWT provider, filter, UserDetailsService, SecurityConfig
├── service/           # Business logic (AuthService, UserService)
└── config/            # SwaggerConfig (existing: JacksonConfig, MqttConfig)
```

## Risks / Trade-offs

- **In-memory blacklist lost on restart** → Acceptable for development. Mitigate by keeping JWT expiration short (24h). Document Redis upgrade path for production.
- **No email verification** → Accounts are immediately active after registration. This is acceptable for the current scope but means any email can register.
- **Spring Security may affect MQTT ingestion** → The MQTT subscriber is a service-level component, not an HTTP endpoint. Spring Security's filter chain only applies to HTTP requests, so no impact expected. Verify during testing.
- **Lombok + JPA** → Known pitfall: `@Data` generates `equals`/`hashCode` using all fields including `id`, which can cause issues with detached entities. Mitigate by using `@Getter`/`@Setter` instead of `@Data` on the `User` entity.
