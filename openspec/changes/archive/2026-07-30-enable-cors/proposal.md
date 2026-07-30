## Why

The backend API endpoints currently reject cross-origin HTTP requests because CORS (Cross-Origin Resource Sharing) is not configured in Spring Security. Enabling CORS ensures that client applications (such as Angular running on port 4200 or other development/production web frontends on other ports) can seamlessly communicate with all backend REST endpoints without browser cross-origin policy errors.

## What Changes

- Enable global CORS configuration in Spring Security (`SecurityConfig`).
- Allow incoming requests from all frontend origins/ports (e.g., `http://localhost:4200` or configurable origins/wildcards for development).
- Allow standard HTTP methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`) across all endpoints (`/api/**`, `/swagger-ui/**`, `/v3/api-docs/**`).
- Configure CORS headers (`Authorization`, `Content-Type`, `X-Requested-With`) and expose necessary headers like `Authorization`.
- Ensure preflight `OPTIONS` requests are handled cleanly without requiring authentication.

## Capabilities

### New Capabilities
- `cors-configuration`: Global CORS configuration enabling cross-origin API access from different origins and ports.

### Modified Capabilities
- None

## Impact

- **Security & Networking**: Spring Security `SecurityFilterChain` will include `.cors(Customizer.withDefaults())` and supply a `CorsConfigurationSource` bean.
- **API Endpoints**: All controllers and public/authenticated endpoints will accept cross-origin requests from frontend applications running on port 4200 and other origins.
