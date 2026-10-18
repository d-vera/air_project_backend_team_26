## 1. Update AirQualityMessage DTO

- [x] 1.1 Rewrite `AirQualityMessage.java` nested records: replace `Dispositivo` → `Device` (fields: `id`, `firmware`, `timestamp`), `Entorno` → `Environment` (fields: `temperature`, `humidity`), `Aire` → `AirData` (fields: `co2`, `pm1_0`, `pm2_5`, `pm10`). Update `@JsonProperty` annotations to match new English JSON keys (`device`, `environment`, `airData`).
- [x] 1.2 Add `@JsonIgnoreProperties(ignoreUnknown = true)` on `AirQualityMessage` and all nested records to silently skip `stateSensors`, `device.sequence`, `airData.p03`, and any future unknown fields.

## 2. Update AirQualityService

- [x] 2.1 Update `processAndSave()` in `AirQualityService.java` to use new DTO accessor methods: `message.device()`, `message.environment()`, `message.airData()` and their inner fields (`id()`, `firmware()`, `timestamp()`, `temperature()`, `humidity()`, `co2()`, `pm1_0()`, `pm2_5()`, `pm10()`).
- [x] 2.2 Set `deviceName` to `null` (removed from payload) and `sequence` to `null` (explicitly ignored) when constructing `AirQualityReading`.

## 3. Update Tests

- [x] 3.1 Update `AirQualityMessageTest` to use the new English-language JSON payload for deserialization tests. Verify `stateSensors`, `sequence`, and `p03` are silently ignored.
- [x] 3.2 Update `AirQualityServiceTest` to construct `AirQualityMessage` instances using the new record structure (`Device`, `Environment`, `AirData`) and verify `deviceName` and `sequence` are `null` in persisted entities.
- [x] 3.3 Run full test suite (`./mvnw test`) and verify all tests pass.
