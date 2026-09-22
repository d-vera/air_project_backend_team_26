## Why

Sensors currently remain in whatever status they were last set to, even when they stop sending data. This creates a misleading dashboard — sensors that haven't reported in hours still appear as ONLINE. Only admins should be able to manually change a sensor's status between the three stages (ONLINE, OFFLINE, MAINTENANCE). When a sensor is in MAINTENANCE, the system must not automatically change its status — only an admin can transition it out. Additionally, only admins should be able to soft-delete or reactivate sensors through the API.

## What Changes

- **Automatic sensor offline detection**: A scheduled job runs periodically and marks any sensor as OFFLINE if no data has been received for 1 hour (based on `lastSeen` timestamp). When new MQTT data arrives, the sensor is automatically set back to ONLINE.
- **Maintenance mode protection**: When an admin sets a sensor to MAINTENANCE status, the system (both the scheduled job and MQTT data ingestion) SHALL NOT change its status to ONLINE or OFFLINE. Only an admin can transition a sensor out of MAINTENANCE mode. Only admins can set a sensor to MAINTENANCE.
- **Admin-controlled status transitions**: Admins can manually set any of the three statuses (ONLINE, OFFLINE, MAINTENANCE) via the existing `PUT /api/sensors/{id}` endpoint.
- **Admin-only sensor soft-delete and reactivation**: Add an explicit `PUT /api/sensors/{id}/reactivate` endpoint to allow admins to reactivate soft-deleted sensors. The existing `DELETE /api/sensors/{id}` endpoint (already admin-only) performs soft-delete. This makes soft-delete and reactivation clearly available for the frontend UI.

## Capabilities

### New Capabilities
- `sensor-auto-offline`: Scheduled background job that detects stale sensors (no data for 1 hour) and marks them OFFLINE automatically. Respects MAINTENANCE lock.
- `admin-sensor-lifecycle`: Admin-only endpoint for reactivating soft-deleted sensors, complementing the existing soft-delete endpoint.

### Modified Capabilities
- `sensor-management`: Status update logic must respect MAINTENANCE lock — system cannot override MAINTENANCE status, only admin can set/unset it.
- `air-quality-ingestion`: When processing incoming MQTT data, the sensor status update must skip sensors in MAINTENANCE mode (do not set to ONLINE).

## Impact

- **Service layer**: `SensorService.updateSensorStatusFromReading()` must check for MAINTENANCE before setting ONLINE. New scheduled method needed for auto-offline. New `reactivateSensor()` method in `SensorService`.
- **Repository**: New query method to find sensors that are ONLINE with `lastSeen` older than 1 hour and not in MAINTENANCE.
- **Controller**: `SensorController` needs a new `PUT /api/sensors/{id}/reactivate` endpoint. `SensorController` update endpoint needs validation that only admins can set MAINTENANCE.
- **Security**: The reactivate sensor endpoint must be restricted to `ADMIN` role.
- **Scheduler**: New `@Scheduled` component to periodically check for stale sensors.
- **Tests**: New and updated tests for all modified services and controllers.
