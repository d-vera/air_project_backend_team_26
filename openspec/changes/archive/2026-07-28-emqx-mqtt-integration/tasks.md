## 1. Dependencies & Configuration

- [x] 1.1 Add Eclipse Paho MQTT client dependency (`org.eclipse.paho.client.mqttv3:1.2.5`) to `pom.xml`
- [x] 1.2 Add `spring-boot-starter-web` dependency to `pom.xml` (provides Jackson for JSON deserialization)
- [x] 1.3 Update `application.properties` with MQTT environment variables (`MQTT_BROKER_URL`, `MQTT_CLIENT_ID`, `MQTT_USERNAME`, `MQTT_PASSWORD`, `MQTT_TOPIC`, `MQTT_QOS`) with defaults
- [x] 1.4 Update `application.properties` with PostgreSQL environment variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`) and JPA/Hibernate settings

## 2. Data Model & Repository

- [x] 2.1 Create `AirQualityReading` JPA entity in `model/` with all columns (time, deviceId, firmware, sequence, topic, temperature, humidity, co2, pm1_0, pm2_5, pm10)
- [x] 2.2 Create `AirQualityReadingRepository` interface in `repository/` extending `JpaRepository`
- [x] 2.3 Create `AirQualityMessage` DTO record in `dto/` with nested records (`Dispositivo`, `Entorno`, `Aire`) matching the MQTT JSON payload

## 3. MQTT Connection

- [x] 3.1 Create `MqttConfig` configuration class in `config/` that builds an `MqttClient` bean with `MqttConnectOptions` (username/password, cleanSession, automaticReconnect)
- [x] 3.2 Wire `@Value`-annotated fields for broker URL, client ID, username, and password from `application.properties`

## 4. Ingestion Service

- [x] 4.1 Create `MqttSubscriberService` in `service/` that subscribes to the configured topic on `@PostConstruct` and implements `MqttCallback` to handle incoming messages
- [x] 4.2 Implement `messageArrived` to deserialize JSON payload via Jackson `ObjectMapper` into `AirQualityMessage`, with error handling for malformed messages
- [x] 4.3 Create `AirQualityService` in `service/` that maps `AirQualityMessage` DTO to `AirQualityReading` entity (setting `time = Instant.now()` and `topic` from the MQTT topic) and persists via the repository
- [x] 4.4 Add INFO-level logging on successful ingestion (device ID + topic)

## 5. Database Initialization

- [x] 5.1 Create `schema.sql` in `src/main/resources/` with TimescaleDB extension creation, hypertable conversion, and composite index on `(device_id, time DESC)`

## 6. Verification

- [x] 6.1 Run `./mvnw clean compile` to verify the project builds without errors
