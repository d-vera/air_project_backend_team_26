## Context

The backend currently deserializes MQTT air quality messages using Spanish-language JSON field names (`dispositivo`, `entorno`, `aire`) defined in `AirQualityMessage.java`. The IoT sensor nodes have been updated with new firmware that sends a redesigned English-language JSON payload with different nesting and some new/removed fields.

Current DTO structure:
- `dispositivo` → `{ id, nombre, firmware, secuencia, Timestamp }`
- `entorno` → `{ temperatura, humedad }`
- `aire` → `{ co2, pm1_0, pm2_5, pm10 }`

New payload structure from nodes:
- `device` → `{ id, firmware, timestamp }` (no `nombre`/`sequence`)
- `environment` → `{ temperature, humidity }`
- `airData` → `{ co2, pm1_0, pm2_5, pm10 }` (ignore `p03`)
- `stateSensors` → ignored entirely

The `AirQualityMessage` record, `AirQualityService.processAndSave()`, and related tests all need updating.

## Goals / Non-Goals

**Goals:**
- Update `AirQualityMessage` DTO to deserialize the new English-language JSON payload from sensor nodes.
- Update `AirQualityService.processAndSave()` to extract data from the new DTO field names.
- Ensure all existing tests pass with the new JSON structure.
- Gracefully handle `null` blocks (e.g., missing `environment` or `airData`).

**Non-Goals:**
- Backward compatibility with the old Spanish-language JSON format — all nodes will be flashed with new firmware before deployment.
- Removing the `deviceName` and `sequence` columns from the database schema — they remain but are no longer populated from MQTT.
- Changing the `AirQualityReading` entity or database migration — schema stays the same.
- Consuming the `stateSensors` or `p03` fields — they are not needed by the backend.

## Decisions

### Decision 1: Replace DTO in-place rather than adding a new version

**Choice**: Rewrite `AirQualityMessage.java` with the new structure instead of creating `AirQualityMessageV2`.

**Rationale**: All sensor nodes will be updated simultaneously. There is no transition period requiring dual-format support. A single DTO keeps the codebase simpler.

**Alternative considered**: Versioned DTOs with a format-detection layer — rejected because it adds complexity for a one-time migration.

### Decision 2: Use `@JsonIgnoreProperties(ignoreUnknown = true)` on nested records

**Choice**: Annotate nested records with `@JsonIgnoreProperties(ignoreUnknown = true)` so that `stateSensors`, `p03`, `sequence`, and any other unexpected fields in the JSON are silently ignored during deserialization.

**Rationale**: The node firmware may add fields in the future. Failing on unknown properties would cause ingestion outages. Jackson's default behavior can be configured globally (via `JacksonConfig`) or per-class — per-class is more explicit and safer.

### Decision 3: Set `deviceName` and `sequence` to `null` in entity mapping

**Choice**: Pass `null` for `deviceName` and `sequence` when constructing `AirQualityReading` in `processAndSave()`.

**Rationale**: These columns exist in the DB but the new payload doesn't include them. Setting them to `null` is safe since both columns are nullable. Removing the columns is a separate migration concern.

## Risks / Trade-offs

- **[Old firmware nodes]** → If any sensor node is not flashed with the new firmware, its messages will fail deserialization. **Mitigation**: The `messageArrived` handler already catches `Exception` and logs errors — old-format messages will be logged and skipped without crashing.
- **[Lost `deviceName` / `sequence` data]** → Historical records will retain these values, but new records will have `null`. **Mitigation**: Acceptable per user requirements. The `sequence` field is explicitly being dropped and `nombre` is no longer sent by nodes.
- **[`p03` data not stored]** → Particle count data ≥0.3µm is discarded. **Mitigation**: Per user requirements, this field is not needed. If needed in the future, a new column and DTO field can be added.
