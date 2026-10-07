## 1. Dependencies and Configuration

- [x] 1.1 Add `spring-boot-starter-websocket` dependency to `pom.xml`.
- [x] 1.2 Add WebSocket and PostgreSQL notification properties to `application.properties` (with sensible defaults and enable/disable toggles).
- [x] 1.3 Update `SecurityConfig.java` to permit public HTTP handshake at `/ws/**`.

## 2. WebSocket Infrastructure and JWT Security

- [x] 2.1 Create `WebSocketAuthInterceptor.java` (`ChannelInterceptor`) to validate JWT bearer tokens from the `Authorization` header on STOMP `CONNECT` frames.
- [x] 2.2 Create `WebSocketConfig.java` implementing `WebSocketMessageBrokerConfigurer` with `/ws` endpoint, `/topic` broker prefix, and the auth interceptor.
- [x] 2.3 Create DTOs for WebSocket payloads (`AirQualityReadingNotificationDto`, `SensorStatusNotificationDto`).
- [x] 2.4 Update `SwaggerConfig.java` to document WebSocket endpoints, STOMP topics, and JWT authentication instructions within OpenAPI description.

## 3. Database Triggers (PostgreSQL)

- [x] 3.1 Create SQL migration script `V_add_realtime_triggers.sql` defining `notify_air_quality_reading()` trigger function and trigger on `air_quality_readings` (`AFTER INSERT OR UPDATE`).
- [x] 3.2 Add `notify_sensor_status_change()` trigger function and trigger on `sensors` (`AFTER INSERT OR UPDATE`) to broadcast only on `sensor_status` or `active` changes.

## 4. PostgreSQL Notification Listener

- [x] 4.1 Implement `PgNotificationListener.java` running on a dedicated background thread with lifecycle management (`@PostConstruct` / `@PreDestroy`).
- [x] 4.2 Add channel listening logic for `air_quality_channel` and `sensor_status_channel` using `PGConnection.getNotifications()`.
- [x] 4.3 Add reconnection and backoff logic to handle transient database connection drops.
- [x] 4.4 Dispatch parsed notifications to STOMP destinations (`/topic/readings`, `/topic/readings/{sensorUid}`, `/topic/sensors/status`) via `SimpMessagingTemplate`.

## 5. Testing and Validation

- [x] 5.1 Implement unit tests for `WebSocketAuthInterceptor` testing valid, invalid, and missing JWT tokens.
- [x] 5.2 Implement unit tests for `PgNotificationListener` notification parsing and STOMP message dispatching.
- [x] 5.3 Run full test suite (`./mvnw test`) to guarantee all existing and new tests pass with zero regressions.
