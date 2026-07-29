## 1. Dependencies & Configuration

- [x] 1.1 Add Maven dependencies to `pom.xml`: `spring-boot-starter-security`, `spring-boot-starter-validation`, `jjwt-api`/`jjwt-impl`/`jjwt-jackson` (0.12.6), `springdoc-openapi-starter-webmvc-ui` (2.8.8), `lombok`
- [x] 1.2 Add JWT and Swagger configuration properties to `application.properties` (`jwt.secret`, `jwt.expiration-ms`)

## 2. Model & Repository

- [x] 2.1 Create `Role` enum with values `REGISTERED_USER` and `ADMIN`
- [x] 2.2 Create `User` JPA entity with fields: id, email (unique), password, firstName, lastName, role, active (default true), createdAt, updatedAt. Implement `UserDetails` interface
- [x] 2.3 Create `UserRepository` interface with methods: `findByEmailAndActiveTrue`, `findByIdAndActiveTrue`, `findAllByActiveTrue`, `existsByEmail`

## 3. DTOs

- [x] 3.1 Create `RegisterRequest` DTO with validation annotations (`@Email`, `@NotBlank`, `@Size(min=8)` for password)
- [x] 3.2 Create `LoginRequest` DTO with `@NotBlank` email and password
- [x] 3.3 Create `AuthResponse` DTO with token, tokenType, email, role
- [x] 3.4 Create `UserResponse` DTO with all user fields except password
- [x] 3.5 Create `UpdateUserRequest` DTO with optional firstName, lastName, password
- [x] 3.6 Create `AssignRoleRequest` DTO with `@NotNull` role field

## 4. Security Layer

- [x] 4.1 Create `JwtTokenProvider` utility: generate token (subject=email, role claim, JTI, 24h expiry), parse/validate token, extract email and role from token
- [x] 4.2 Create `TokenBlacklist` component with `ConcurrentHashMap` for storing blacklisted JTI values
- [x] 4.3 Create `CustomUserDetailsService` implementing `UserDetailsService` — loads active users by email
- [x] 4.4 Create `JwtAuthenticationFilter` extending `OncePerRequestFilter` — extracts Bearer token, validates, checks blacklist, sets SecurityContext
- [x] 4.5 Create `SecurityConfig` with filter chain: permit `/api/auth/register`, `/api/auth/login`, Swagger paths; require ADMIN for `/api/users/**` (except `/api/users/me`); require authentication for all other endpoints. Disable CSRF, set stateless session management

## 5. Service Layer

- [x] 5.1 Create `AuthService` with register (validate email uniqueness, hash password, save user, generate JWT), login (verify credentials for active users, generate JWT), and logout (blacklist token) methods
- [x] 5.2 Create `UserService` with getCurrentUser, updateCurrentUser, getAllUsers, getUserById, updateUser, deleteUser (soft-delete), and assignRole methods

## 6. Exception Handling

- [x] 6.1 Create `ErrorResponse` record/class with timestamp, status, error, and message fields
- [x] 6.2 Create custom exceptions: `EmailAlreadyExistsException`, `UserNotFoundException`, `InvalidCredentialsException`
- [x] 6.3 Create `GlobalExceptionHandler` with `@RestControllerAdvice` handling: validation errors (400), email conflict (409), invalid credentials (401), access denied (403), user not found (404), and generic errors (500)

## 7. Controller Layer

- [x] 7.1 Create `AuthController` (`/api/auth`) with endpoints: `POST /register` (public), `POST /login` (public), `POST /logout` (authenticated)
- [x] 7.2 Create `UserController` (`/api/users`) with endpoints: `GET /me`, `PUT /me` (authenticated), `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `PUT /{id}/role` (admin only)

## 8. Swagger Configuration

- [x] 8.1 Create `SwaggerConfig` class with OpenAPI bean: title "Air Project — User Management API", version "1.0", description, and JWT Bearer security scheme
- [x] 8.2 Add Swagger annotations to controllers: `@Tag`, `@Operation`, `@ApiResponse` for all endpoints

## 9. Verification

- [x] 9.1 Compile the project with `./mvnw compile` and fix any build errors
- [x] 9.2 Start the application and verify Swagger UI is accessible at `/swagger-ui.html`
- [x] 9.3 Test registration, login, logout, and user management flows via Swagger UI
