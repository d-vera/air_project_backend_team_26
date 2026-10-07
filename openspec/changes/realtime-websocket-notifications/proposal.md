## Why

Currently, clients (such as the web frontend) must poll REST endpoints (`GET /api/air-quality/latest` and `GET /api/sensors`) to detect new air quality measurements and sensor status changes (e.g. ONLINE, OFFLINE, MAINTENANCE). Polling creates unnecessary network overhead, causes latency in updating user dashboards, and fails to reflect immediate database updates—particularly those executed directly via database maintenance or administrator scripts.

Implementing a real-time event pipeline using PostgreSQL `LISTEN / NOTIFY` coupled with Spring Boot WebSockets (STOMP) enables immediate, low-latency streaming of new/updated air quality measurements and sensor status changes directly to frontend dashboards, secured via existing JWT tokens.

## What Changes

- Add Spring WebSocket and STOMP messaging support (`spring-boot-starter-websocket`) with a dedicated endpoint (`/ws`).
- Authenticate WebSocket STOMP connections using the existing JWT authentication mechanism during the STOMP `CONNECT` frame.
- Configure separate STOMP subscription topics:
  - `/topic/readings`: Live stream of new or updated air quality measurements.
  - `/topic/readings/{sensorUid}`: Stream of measurements filtered for a specific sensor.
  - `/topic/sensors/status`: Stream of sensor state changes (ONLINE, OFFLINE, MAINTENANCE, active/inactive).
- Implement PostgreSQL triggers using `pg_notify`:
  - `trg_air_quality_reading_change` on `air_quality_readings` (`AFTER INSERT OR UPDATE`) to broadcast new and corrected readings.
  - `trg_sensor_status_change` on `sensors` (`AFTER INSERT OR UPDATE`) to broadcast status transitions and activation changes.
- Implement a dedicated background listener service in Spring Boot (`PgNotificationListener`) to consume PostgreSQL notifications via a persistent JDBC connection and relay them to STOMP topics.
- Update `SwaggerConfig.java` to document the WebSocket STOMP endpoint (`/ws`), authentication protocol, subscription topics, and payload schemas within the OpenAPI `Info` description.

## Capabilities

### New Capabilities
- `realtime-notifications`: Provides real-time WebSocket communication over STOMP, supporting subscriptions to live air quality measurements and sensor status changes with JWT authentication, documented in Swagger UI.

### Modified Capabilities
<!-- No requirement changes to existing REST endpoints or MQTT ingestion logic -->

## Impact

- **Dependencies**: Adds `org.springframework.boot:spring-boot-starter-websocket` to `pom.xml`.
- **Database**: Adds trigger functions and triggers on `air_quality_readings` and `sensors` tables.
- **Security**: Updates `SecurityConfig` to permit the `/ws/**` handshake endpoint, and introduces STOMP channel interceptors for JWT token validation.
- **Backend Architecture**: Introduces a dedicated background connection thread listening for PostgreSQL notifications and routing to `SimpMessagingTemplate`.
- **Documentation**: Updates Swagger (`SwaggerConfig.java`) OpenAPI overview to include real-time WebSocket connection and topic reference.
