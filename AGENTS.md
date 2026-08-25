# Backend Agent Rules — Air Project (Team 26)

## Mandatory Testing on Every Change

**Any time code is modified, added, or deleted in the backend (`airProject/` directory), the agent MUST run the full test suite before considering the task complete.**

### How to Run Tests

```bash
cd airProject && ./mvnw test
```

- If tests **pass** → proceed normally.
- If tests **fail** → fix the failures before finishing the task. Do NOT leave the codebase in a failing state.

---

## When to Run Tests

Run `./mvnw test` after **every** change to any of the following:

| Layer | Path | Examples |
|---|---|---|
| **Controllers** | `src/main/java/**/controller/` | `UserController`, `SensorController`, `PreferenceController` |
| **Services** | `src/main/java/**/service/` | `UserService`, `SensorService`, `AirQualityService`, `AuthService`, `MqttSubscriberService`, `UserPreferenceService` |
| **Repositories** | `src/main/java/**/repository/` | `UserRepository`, `SensorRepository`, `AirQualityReadingRepository`, `UserPreferenceRepository` |
| **Models / Enums** | `src/main/java/**/model/` | `User`, `Sensor`, `AirQualityReading`, `Role`, `SensorStatus`, `Language`, `Theme` |
| **DTOs** | `src/main/java/**/dto/` | `RegisterRequest`, `LoginRequest`, `AirQualityMessage`, `SensorResponse`, etc. |
| **Security** | `src/main/java/**/security/` | `SecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter`, `TokenBlacklist`, `CustomUserDetailsService` |
| **Config** | `src/main/java/**/config/` | `JacksonConfig`, `MqttConfig`, `SwaggerConfig` |
| **Exceptions** | `src/main/java/**/exception/` | `GlobalExceptionHandler`, `UserNotFoundException`, `SensorNotFoundException`, etc. |
| **Tests** | `src/test/java/**/` | Any test file change also triggers the full suite |
| **Build config** | `pom.xml` | Dependency or plugin changes |
| **App properties** | `src/main/resources/` | `application.properties`, `application.yml`, profiles |

---

## Test Coverage Expectations

When adding or modifying a feature, ensure related tests exist:

- **New controller endpoint** → add or update the corresponding `*ControllerTest`.
- **New service method** → add or update the corresponding `*ServiceTest`.
- **Changed DTO fields** → verify `AirQualityMessageTest` and any deserialization tests still pass.
- **Security changes** → verify `SecurityConfigCorsTest` and authentication flows.

### Existing Test Files

| Test File | Covers |
|---|---|
| `UserControllerTest` | User registration, login, profile endpoints |
| `SensorControllerTest` | Sensor CRUD endpoints |
| `PreferenceControllerTest` | User preference endpoints |
| `UserServiceTest` | User business logic |
| `SensorServiceTest` | Sensor business logic |
| `AirQualityServiceTest` | Air quality data processing |
| `UserPreferenceServiceTest` | Preference management logic |
| `AirQualityMessageTest` | DTO serialization / deserialization |
| `SecurityConfigCorsTest` | CORS and security configuration |
| `AirprojectApplicationTests` | Application context loads |

---

## Additional Rules

1. **Never skip tests.** Do not use `-DskipTests` or `-Dmaven.test.skip=true`.
2. **Fix before commit.** All tests must be green before the task is marked complete.
3. **Report results.** After running tests, report the pass/fail count to the user.
4. **If a test is flaky**, investigate and fix it rather than ignoring it.
