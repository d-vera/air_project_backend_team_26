## 1. Security Configuration

- [x] 1.1 Add `CorsConfigurationSource` bean to `SecurityConfig.java` configured for `http://localhost:4200` and allowed origins/methods/headers.
- [x] 1.2 Enable CORS in `SecurityFilterChain` in `SecurityConfig.java` via `.cors(Customizer.withDefaults())`.

## 2. Verification & Testing

- [x] 2.1 Verify application compilation using Maven (`./mvnw compile`).
- [x] 2.2 Verify CORS preflight and request headers for `/api/auth/login`, `/api/users/me`, and Swagger endpoints.
