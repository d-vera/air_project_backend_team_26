## Context

The project is a fresh Spring Boot 4.1.0 application (Java 21) with only `spring-boot-starter-data-jpa` and `postgresql` driver. It has no messaging integration, no service layer, and no database schema. IoT sensor nodes publish air quality data (temperature, humidity, CO2, PM1.0, PM2.5, PM10) as JSON to an EMQX MQTT broker on topic `calidad_aire/nodo1` with QoS 0. The backend must subscribe to this topic and persist every message into a PostgreSQL/TimescaleDB database.

## Goals / Non-Goals

**Goals:**
- Establish a persistent MQTT connection to EMQX with username/password authentication
- Subscribe to `calidad_aire/nodo1` and deserialize incoming JSON payloads
- Persist every air quality reading into a TimescaleDB hypertable with server-side arrival timestamps
- Externalize all connection parameters (MQTT and PostgreSQL) via environment variables with sensible defaults

**Non-Goals:**
- REST API for querying stored data (future work)
- Support for multiple MQTT topics or wildcard subscriptions
- TLS/SSL encryption for MQTT or database connections
- Message deduplication or exactly-once delivery guarantees
- Docker Compose or containerized deployment
- Data validation or alerting on sensor thresholds

## Decisions

### 1. MQTT Client: Eclipse Paho standalone

**Choice**: `org.eclipse.paho.client.mqttv3` (v1.2.5) used directly as a Spring `@Bean`.

**Alternatives considered**:
- *Spring Integration MQTT*: Provides auto-configuration and channel abstractions, but introduces significant boilerplate and pulls in the full Spring Integration framework — overkill for a single-topic subscriber.
- *HiveMQ MQTT Client*: Modern and reactive, but less mature Spring ecosystem integration and fewer community examples.

**Rationale**: Paho is lightweight, battle-tested, and gives full control over connection lifecycle. A `@Configuration` class creates the `MqttClient` bean, and a `@PostConstruct` method in the subscriber service handles topic subscription.

### 2. JSON Deserialization: Jackson via Java Records

**Choice**: Use Jackson `ObjectMapper` (from `spring-boot-starter-web`) with nested Java `record` types matching the MQTT JSON structure.

**Rationale**: Records are immutable, concise, and Jackson supports them natively since Java 16. The nested structure (`dispositivo`, `entorno`, `aire`) maps directly to nested records — no annotations needed if field names match.

### 3. Timestamp Strategy: Server-side arrival time

**Choice**: Use `Instant.now()` at message arrival rather than any timestamp from the payload.

**Rationale**: The MQTT payload does not include a timestamp field in the JSON body. The timestamp shown (`2026-07-18 21:21:26`) appears to be broker metadata. Using server arrival time ensures consistency and avoids clock-skew issues with IoT devices.

### 4. Database Schema: Flat wide table as TimescaleDB hypertable

**Choice**: A single `air_quality_readings` table with all sensor fields as columns, converted to a TimescaleDB hypertable partitioned on the `time` column.

**Alternatives considered**:
- *Normalized tables* (device table + readings table with foreign keys): Adds join overhead on every insert and query, which is counterproductive for time-series workloads.
- *JSONB column for sensor data*: Flexible but loses type safety and indexing performance.

**Rationale**: TimescaleDB hypertables are optimized for wide, append-only tables. A flat schema enables efficient compression, continuous aggregates, and simple queries without joins.

### 5. Table Creation: Hibernate `ddl-auto=update` + manual hypertable conversion

**Choice**: Let Hibernate create the base table, then manually run `SELECT create_hypertable(...)` via a provided SQL script.

**Rationale**: Hibernate cannot issue TimescaleDB-specific DDL. The SQL script (`schema.sql`) is provided for manual execution or future Flyway integration.

## Risks / Trade-offs

- **Single-threaded message processing** → If message volume is very high, the synchronous `messageArrived` → JPA `save()` flow could become a bottleneck. Mitigation: acceptable for current single-node scope; can add async processing or batching later.
- **No message persistence on MQTT disconnect** → QoS 0 means messages published while the backend is down are lost. Mitigation: EMQX retains no messages at QoS 0 by design; acceptable trade-off for sensor telemetry.
- **`ddl-auto=update` in production** → Hibernate auto-DDL can cause issues in production. Mitigation: appropriate for development; switch to Flyway migrations before production deployment.
- **No connection retry backoff** → Paho's `automaticReconnect` uses its own backoff, but if initial connection fails the app crashes. Mitigation: acceptable for development; add retry logic if needed later.
