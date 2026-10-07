## Context

The backend currently stores incoming air quality readings from MQTT and tracks sensor lifecycle changes (ONLINE, OFFLINE, MAINTENANCE). Frontend dashboards require immediate feedback when new readings arrive or sensor statuses transition, without continuously polling REST endpoints. Additionally, changes made directly via database modifications (e.g., DBA scripts, manual corrections, or updates) need to be propagated to connected clients in real time.

## Goals / Non-Goals

**Goals:**
- Provide real-time STOMP-over-WebSocket broadcasting for:
  - New and updated air quality readings (`/topic/readings` and `/topic/readings/{sensorUid}`).
  - Sensor status changes (`/topic/sensors/status`).
- Enable database-level event capture using PostgreSQL triggers and `pg_notify` so both application writes and direct SQL updates are detected.
- Authenticate incoming STOMP connections using existing JWT bearer tokens on the `CONNECT` frame.
- Implement a dedicated background listener in Spring Boot (`PgNotificationListener`) using a persistent JDBC connection with automatic reconnection and error handling.
- Document WebSocket endpoints, STOMP topics, JWT authentication workflow, and payload examples inside `SwaggerConfig.java` so they appear prominently in `/swagger-ui.html`.
- Ensure the test suite (`./mvnw test`) remains fully operational without requiring an active PostgreSQL instance (using test profiles, mocks, or conditional configuration).

**Non-Goals:**
- Replacing existing REST endpoints (REST endpoints continue to serve queries, historical data, and CRUD).
- Replacing MQTT ingestion (MQTT broker remains the ingestion pathway for hardware sensors).
- Bidirectional sensor control via WebSocket (WebSockets are outbound event streams only; sensor commands remain through dedicated MQTT/REST APIs).

## Decisions

### 1. PostgreSQL LISTEN / NOTIFY over Application-Level Events
- **Rationale**: Application-level Spring events (`@TransactionalEventListener`) cannot detect manual SQL updates executed directly in PostgreSQL. PostgreSQL `LISTEN / NOTIFY` guarantees that changes from any source (MQTT ingest, REST API, scheduled tasks, or manual SQL) are captured and broadcasted.
- **Alternatives Considered**:
  - *Spring Application Events (`@TransactionalEventListener`)*: Simpler, but completely misses direct DB updates.
  - *Debezium / Kafka CDC*: Full-featured CDC, but introduces heavy external infrastructure overhead inappropriate for this architecture.

### 2. Dedicated Physical JDBC Connection for `LISTEN`
- **Rationale**: PostgreSQL `LISTEN` is session-scoped. A connection running `LISTEN` cannot be returned to HikariCP connection pool without blocking normal query traffic or risking connection resets.
- **Implementation**: `PgNotificationListener` obtains or creates a dedicated physical `PGConnection` (via `DataSource.getConnection().unwrap(PGConnection.class)` or `DriverManager`), issues `LISTEN air_quality_channel` and `LISTEN sensor_status_channel`, and invokes `pgConnection.getNotifications(timeoutMs)` in a loop on a dedicated daemon thread.

### 3. Separate Broadcast Channels
- `/topic/readings`: Global stream of all sensor readings.
- `/topic/readings/{sensorUid}`: Targeted stream for views monitoring a single sensor.
- `/topic/sensors/status`: Stream for sensor lifecycle events (ONLINE, OFFLINE, MAINTENANCE, active/inactive).
- **Alternatives Considered**:
  - *Single Unified Channel*: Clients would need to filter high-frequency readings from low-frequency status changes, wasting client bandwidth.

### 4. JWT Authentication at STOMP CONNECT Layer
- **Rationale**: Web browsers' native `WebSocket` API does not permit custom HTTP headers (such as `Authorization: Bearer <token>`) during the initial HTTP handshake.
- **Implementation**: The HTTP `/ws` handshake endpoint is configured as `permitAll` in `SecurityConfig`. During the STOMP handshake, a `ChannelInterceptor` inspects the `CONNECT` frame headers, validates the `Authorization` bearer token using `JwtTokenProvider`, and sets the `Authentication` in the message header accessor. Connections without a valid JWT are rejected.

### 5. PostgreSQL Triggers with JSON Payloads
- **Readings Trigger**: Fires `AFTER INSERT OR UPDATE ON air_quality_readings` to notify `air_quality_channel` with JSON including action (`INSERT` vs `UPDATE`), `sensorUid`, timestamp, and pollutant concentrations.
- **Sensor Status Trigger**: Fires `AFTER INSERT OR UPDATE ON sensors` only when `sensor_status` or `active` changes (avoiding notification storms on routine `last_seen` timestamp updates), notifying `sensor_status_channel`.

### 6. OpenAPI / Swagger Documentation via SwaggerConfig.java
- **Rationale**: OpenAPI 3.0 / Swagger UI does not natively generate interactive clients for persistent streaming protocols like WebSockets or STOMP. However, developers rely on `/swagger-ui.html` as the central API portal.
- **Implementation**: Update `SwaggerConfig.java` to augment the OpenAPI `Info` description with a structured markdown guide detailing the WebSocket handshake endpoint (`/ws`), connection protocol, STOMP frame authentication syntax (`Authorization: Bearer <token>`), subscription topics, and sample JSON payloads.
- **Alternatives Considered**:
  - *AsyncAPI*: Comprehensive for event-driven APIs, but introduces external tooling/build complexity. Documenting within Swagger UI provides immediate visibility to developers without new dependencies.

## Risks / Trade-offs

- **[PostgreSQL Notification 8KB Limit]** → The payloads (single reading or sensor status) are ~300 bytes, far below PostgreSQL's 8,000-byte `pg_notify` limit.
- **[Database Connection Dropped]** → `PgNotificationListener` wraps the polling loop in a reconnect loop with backoff (e.g., 2s, 5s, 10s) to re-establish the connection and re-issue `LISTEN`.
- **[Test Execution Without Live Postgres]** → `PgNotificationListener` is guarded with `@ConditionalOnProperty(name = "websocket.notifications.enabled", havingValue = "true", matchIfMissing = true)` or graceful initialization so that standard `./mvnw test` runs cleanly in environments with mock data sources or H2.
