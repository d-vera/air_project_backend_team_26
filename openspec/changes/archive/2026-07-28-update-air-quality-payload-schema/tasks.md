## 1. DTO & Model Updates

- [x] 1.1 Update `AirQualityMessage.Dispositivo` record to include `nombre` (`@JsonProperty("nombre")`) and `timestamp` (`@JsonProperty("Timestamp")`)
- [x] 1.2 Update `AirQualityReading` JPA entity to add `deviceName` (`device_name`) field, getters/setters, and constructor parameters

## 2. Service & Mapping Logic

- [x] 2.1 Update `AirQualityService.processAndSave` to extract `nombre` and map payload `Timestamp` to `Instant` (with fallback to `Instant.now()`) when constructing `AirQualityReading`

## 3. Verification & Testing

- [x] 3.2 Update unit and integration tests to verify deserialization and persistence of the updated payload structure containing `nombre` and `Timestamp`
- [x] 3.3 Verify project builds and all tests pass via `mvn test`
