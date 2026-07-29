## Why

The sensor nodes (e.g., `Node1`) updated their MQTT payload structure for topic `calidad_aire/nodo1` to include `nombre` (device name) and `Timestamp` (device timestamp in unix epoch seconds) inside the `dispositivo` object. Updating the DTO parsing and persistence logic ensures the backend system accurately captures device metadata and node-generated timestamps.

## What Changes

- Update `AirQualityMessage.Dispositivo` DTO record to parse `nombre` (`nombre`) and `Timestamp` (`Timestamp`) fields.
- Update `AirQualityReading` JPA entity to include `deviceName` (`device_name`) and update the `time` field assignment to use the payload's `Timestamp` (converted to `Instant`) when present, falling back to server `Instant.now()` if null.
- Update `AirQualityService` mapping logic to handle `nombre` and `Timestamp`.
- Update the `air-quality-ingestion` specification to document the updated JSON payload schema and persistence requirements.

## Capabilities

### New Capabilities

### Modified Capabilities
- `air-quality-ingestion`: Update payload schema specification to include `nombre` and `Timestamp` within the `dispositivo` block and define mapping rules.

## Impact

- `com.airproject.airproject.dto.AirQualityMessage`
- `com.airproject.airproject.model.AirQualityReading`
- `com.airproject.airproject.service.AirQualityService`
- Test classes covering message parsing and service processing
