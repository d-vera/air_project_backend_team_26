## Context

The MQTT air quality payload delivered by sensor nodes on topic `calidad_aire/nodo1` (QoS 0) has been updated to include additional device metadata in the `dispositivo` object:
- `nombre`: string (e.g. `"Node1"`)
- `Timestamp`: integer unix timestamp in seconds (e.g. `1785274877`)

Currently, `AirQualityMessage.Dispositivo` only parses `id`, `firmware`, and `secuencia`.
Furthermore, `AirQualityReading` sets `time` exclusively to `Instant.now()` without storing the device name or parsing node timestamp.

## Goals / Non-Goals

**Goals:**
- Update `AirQualityMessage.Dispositivo` DTO to map `nombre` and `Timestamp`.
- Update `AirQualityReading` JPA entity to store `deviceName` (`device_name`) and use the message's `Timestamp` (converted to `Instant`) for `time` if provided.
- Maintain backwards compatibility if `nombre` or `Timestamp` are missing or null in incoming messages.

**Non-Goals:**
- Modifying TimescaleDB hypertable partition configuration or primary key structure.

## Decisions

### 1. DTO Mapping with Jackson
Add fields to `AirQualityMessage.Dispositivo`:
- `@JsonProperty("nombre") String nombre`
- `@JsonProperty("Timestamp") Long timestamp`

### 2. Entity Mapping & Timestamp Handling
- Add `@Column(name = "device_name", length = 100)` field to `AirQualityReading`.
- For `time`: convert `Timestamp` to `Instant` (`Instant.ofEpochSecond(timestamp)` or `Instant.ofEpochMilli(timestamp)` depending on digit count), falling back to `Instant.now()` if `timestamp` is null or invalid.

## Risks / Trade-offs

- **[Timestamp Resolution / Scale]** → Device timestamp `1785274877` represents epoch seconds. If a node sends milliseconds (> 10^11), the service should dynamically detect millisecond vs second timestamps.
- **[Null Field Safety]** → Older firmware versions might omit `nombre` or `Timestamp`. Service will safely default `deviceName` to null and `time` to `Instant.now()`.
