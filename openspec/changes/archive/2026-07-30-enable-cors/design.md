## Context

The backend Spring Boot service (`airProject`) currently rejects cross-origin HTTP requests because Spring Security does not have CORS handling enabled in its `SecurityFilterChain`. Frontend clients running on different origins or ports (such as Angular on `http://localhost:4200`) receive browser CORS errors when attempting to call backend REST endpoints.

## Goals / Non-Goals

**Goals:**
- Enable Spring Security CORS handling globally across all controllers and API endpoints (`/**`).
- Explicitly permit origins from `http://localhost:4200` as well as configurable development ports/origins.
- Allow standard HTTP methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`).
- Allow essential headers (`Authorization`, `Content-Type`, `X-Requested-With`) and expose `Authorization`.
- Permit credentials (`allowCredentials = true`).

**Non-Goals:**
- Modifying authentication/authorization rules for specific endpoints (role requirements for `/api/users/**` remain unchanged).
- Creating custom CORS filters outside of Spring Security's standard `CorsConfigurationSource`.

## Decisions

### Decision 1: Use Spring Security `CorsConfigurationSource` Bean with `HttpSecurity.cors()`
- **Choice**: Define a `@Bean` of type `CorsConfigurationSource` in `SecurityConfig.java` and enable it in the `SecurityFilterChain` using `.cors(Customizer.withDefaults())`.
- **Rationale**: Integrating directly with Spring Security ensures that preflight `OPTIONS` requests are handled early in the filter chain before authentication checks.
- **Alternatives Considered**: 
  - Using `@CrossOrigin` annotations on individual controllers: Fragmented, easily missed on new endpoints, and doesn't handle global preflight requests consistently in Spring Security.
  - Adding a custom Servlet filter: Unnecessary complexity when Spring Security provides built-in `CorsConfigurationSource` integration.

### Decision 2: Allowed Origins Strategy
- **Choice**: Allow `http://localhost:4200` explicitly alongside wildcard patterns / configurable allowed origin patterns (such as `http://localhost:*` or `allowed-origin-patterns`).
- **Rationale**: `http://localhost:4200` is the standard Angular dev server port. Using pattern matching or configurable properties guarantees support for other frontend ports during development.

## Risks / Trade-offs

- **[Risk]**: Overly permissive CORS settings in production.
  - **Mitigation**: Use explicit origin whitelist for production environments or environment property injection (`cors.allowed-origins`).
- **[Risk]**: Preflight `OPTIONS` requests failing if security filter chain blocks unauthenticated `OPTIONS` calls.
  - **Mitigation**: Spring Security's `cors(Customizer.withDefaults())` automatically handles HTTP `OPTIONS` preflight requests using the registered `CorsConfigurationSource`.
