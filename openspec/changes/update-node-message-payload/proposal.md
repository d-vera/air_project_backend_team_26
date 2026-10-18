## Why

The IoT sensor nodes have been updated with a new firmware that sends MQTT messages in a redesigned JSON format. The field names have changed from Spanish (`dispositivo`, `entorno`, `aire`) to English (`device`, `environment`, `airData`), the nesting structure has changed, and some fields have been added or removed. The backend must update its deserialization layer to match the new payload so that air quality data continues to be ingested correctly.

## What Changes

- **BREAKING**: Update `AirQualityMessage` DTO to match the new English-language JSON structure from sensor nodes.
- Replace `dispositivo` → `device` (with fields: `id`, `firmware`, `timestamp`). Drop `nombre` (no longer sent) and `secuencia`/`sequence` (explicitly ignored).
- Replace `entorno` → `environment` (with fields: `temperature`, `humidity`).
- Replace `aire` → `airData` (with fields: `co2`, `pm1_0`, `pm2_5`, `pm10`). Ignore `p03` (particle count ≥0.3µm) — not needed by the backend.
- Ignore the `stateSensors` block entirely — sensor health status is not consumed by the backend.
- Update `AirQualityService.processAndSave()` to use the new DTO field names.
- Remove the `deviceName` / `nombre` mapping since the new payload does not include a device name.
- Remove the `sequence` / `secuencia` mapping since it is explicitly ignored.
- Update all related tests (`AirQualityMessageTest`, `AirQualityServiceTest`) to use the new JSON structure.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `air-quality-ingestion`: The MQTT JSON payload structure is changing — the DTO, deserialization mapping, and entity mapping requirements must be updated to reflect the new English-language field names and the removal of `sequence`, `nombre`, `stateSensors`, and `p03`.

## Impact

- **DTO**: `AirQualityMessage.java` — complete rewrite of nested records to match new JSON keys.
- **Service**: `AirQualityService.processAndSave()` — update field accessors for new DTO structure; remove `deviceName` and `sequence` extraction.
- **Model**: `AirQualityReading` — the `deviceName` and `sequence` columns remain in the DB schema but will no longer be populated from MQTT messages (set to `null`).
- **Tests**: `AirQualityMessageTest`, `AirQualityServiceTest`, and any other test referencing the old Spanish-language JSON payloads.
- **MQTT messages**: Any producer (sensor node) must be running the new firmware that sends the English-format JSON. Old-format messages will fail deserialization.
