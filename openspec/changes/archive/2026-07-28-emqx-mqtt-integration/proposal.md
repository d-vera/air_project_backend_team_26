## Why

The Spring Boot backend needs to receive real-time air quality sensor data from IoT nodes publishing via MQTT to an EMQX broker. Currently there is no messaging integration and no persistent storage for sensor readings. Without this, the application cannot ingest or store the air quality measurements that are the core purpose of the system.

## What Changes

- Add Eclipse Paho MQTT client to subscribe to EMQX broker topic `calidad_aire/nodo1`
- Add environment-variable-driven configuration for both MQTT (broker URL, credentials, topic) and PostgreSQL (host, port, database, credentials)
- Create a data model and JPA entity for air quality readings (temperature, humidity, CO2, PM1.0, PM2.5, PM10)
- Persist incoming MQTT messages into a PostgreSQL database using TimescaleDB hypertables for time-series optimization
- Add a SQL initialization script for TimescaleDB extension and hypertable creation

## Capabilities

### New Capabilities
- `mqtt-connection`: MQTT client configuration and connection management to EMQX broker with username/password authentication and automatic reconnect
- `air-quality-ingestion`: Subscribing to MQTT topics, deserializing JSON payloads from IoT sensors, mapping to domain entities, and persisting to the database
- `timescaledb-storage`: PostgreSQL/TimescaleDB schema design with hypertable for time-series air quality data, including indexing strategy

### Modified Capabilities
_(none — this is a greenfield integration on a fresh Spring Boot project)_

## Impact

- **Dependencies**: Adds `org.eclipse.paho.client.mqttv3` (1.2.5) and `spring-boot-starter-web` (for Jackson JSON) to `pom.xml`
- **Configuration**: `application.properties` gains 11 new environment-variable-backed properties for MQTT and database connectivity
- **Database**: Requires PostgreSQL with TimescaleDB extension installed; creates `air_quality_readings` table as a hypertable
- **New packages**: `config/`, `model/`, `dto/`, `repository/`, `service/` under `com.airproject.airproject`
- **Runtime**: Application will maintain a persistent MQTT connection to EMQX on startup
