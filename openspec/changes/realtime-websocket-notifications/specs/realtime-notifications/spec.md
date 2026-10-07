## ADDED Requirements

### Requirement: Real-time WebSocket connection and STOMP message broker
The system SHALL provide a STOMP over WebSocket endpoint at `/ws` that allows clients to establish real-time connections and subscribe to broadcast topics under the `/topic` destination prefix. The endpoint SHALL support cross-origin requests configured via allowed origin patterns.

#### Scenario: Successful WebSocket connection handshake
- **WHEN** an authenticated client initiates a WebSocket connection to `/ws`
- **THEN** the system completes the WebSocket handshake and responds with HTTP status 101 Switching Protocols.

### Requirement: JWT authentication on STOMP CONNECT frame
The system SHALL authenticate incoming STOMP connections by extracting and validating the bearer token provided in the `Authorization` native header of the `CONNECT` frame. The token SHALL be validated using the existing `JwtTokenProvider`. If the token is missing, expired, or invalid, the system SHALL reject the STOMP connection with an error.

#### Scenario: Valid JWT token connects successfully
- **WHEN** a client sends a STOMP `CONNECT` frame containing a valid JWT in the `Authorization` header (`Bearer <token>`)
- **THEN** the system accepts the connection, populates the authenticated user in the STOMP session, and returns a `CONNECTED` frame.

#### Scenario: Missing or invalid JWT token is rejected
- **WHEN** a client sends a STOMP `CONNECT` frame without an `Authorization` header or with an invalid/expired token
- **THEN** the system rejects the STOMP connection and closes the session.

### Requirement: Real-time air quality readings broadcasting
The system SHALL broadcast new and updated air quality readings to the `/topic/readings` channel and to the sensor-specific channel `/topic/readings/{sensorUid}` whenever a reading is inserted or updated in the `air_quality_readings` table. The message payload SHALL contain the action (`INSERT` or `UPDATE`), sensor identifier, reading timestamp, temperature, humidity, CO2, PM1.0, PM2.5, and PM10 values.

#### Scenario: New air quality reading inserted
- **WHEN** a new row is inserted into `air_quality_readings` (via MQTT ingestion, REST, or SQL)
- **THEN** a notification is pushed via PostgreSQL trigger and broadcasted to `/topic/readings` and `/topic/readings/{sensorUid}` with action `INSERT`.

#### Scenario: Existing air quality reading updated
- **WHEN** an existing row in `air_quality_readings` is updated
- **THEN** a notification is pushed via PostgreSQL trigger and broadcasted to `/topic/readings` and `/topic/readings/{sensorUid}` with action `UPDATE`.

### Requirement: Real-time sensor status change broadcasting
The system SHALL broadcast sensor status and lifecycle transitions to the `/topic/sensors/status` channel whenever a sensor's `sensor_status` or `active` flag is modified in the `sensors` table. The message payload SHALL contain the sensor ID, sensor UID, sensor name, previous status, new status, active state, and last seen timestamp. Routine updates that only change `last_seen` without altering `sensor_status` or `active` SHALL NOT trigger a status broadcast.

#### Scenario: Sensor status changes from ONLINE to OFFLINE
- **WHEN** a sensor's `sensor_status` transitions from `ONLINE` to `OFFLINE`
- **THEN** a notification is pushed via PostgreSQL trigger and broadcasted to `/topic/sensors/status` indicating the status change.

#### Scenario: Sensor is deactivated or reactivated
- **WHEN** a sensor's `active` flag is updated (soft-deleted or reactivated)
- **THEN** a notification is broadcasted to `/topic/sensors/status` reflecting the updated status and active state.

#### Scenario: Sensor lastSeen updated without status change
- **WHEN** an incoming reading updates a sensor's `last_seen` timestamp while `sensor_status` remains unchanged
- **THEN** no message is broadcasted to `/topic/sensors/status`.

### Requirement: Resilient PostgreSQL notification consumer
The system SHALL maintain a dedicated, persistent JDBC connection to PostgreSQL listening for `air_quality_channel` and `sensor_status_channel` notifications. If the database connection is interrupted, the system SHALL log a warning, attempt reconnection with backoff, and re-subscribe to the channels upon reconnecting.

#### Scenario: Successful recovery after temporary database disconnect
- **WHEN** the physical connection to PostgreSQL is terminated or resets
- **THEN** the listener logs the disconnection, waits with backoff, re-establishes the connection, and re-issues `LISTEN` commands without crashing the application.

### Requirement: OpenAPI Swagger documentation of real-time WebSocket protocol and topics
The system SHALL include documentation for real-time WebSocket communication in Swagger UI (`/swagger-ui.html`) via `SwaggerConfig.java`. The OpenAPI description SHALL outline the `/ws` handshake endpoint, STOMP `CONNECT` frame JWT authentication requirement, available subscription topics (`/topic/readings`, `/topic/readings/{sensorUid}`, `/topic/sensors/status`), and representative JSON message payloads.

#### Scenario: Swagger UI displays WebSocket documentation
- **WHEN** a user or client accesses the OpenAPI documentation via `/v3/api-docs` or `/swagger-ui.html`
- **THEN** the API description contains instructions and channel documentation for WebSocket connections and topics.
