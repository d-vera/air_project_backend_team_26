# sensor-auto-offline Specification

## Purpose
Scheduled background job that detects stale sensors (no data for 1 hour) and marks them OFFLINE automatically. Respects MAINTENANCE lock — sensors in MAINTENANCE are never touched.

## Requirements

### Requirement: Automatic sensor offline detection
The system SHALL run a scheduled background job that checks all active sensors with status `ONLINE`. Any sensor whose `lastSeen` timestamp is older than 1 hour from the current time SHALL be automatically set to `OFFLINE`. Sensors with status `MAINTENANCE` SHALL NOT be affected by this job.

#### Scenario: Sensor goes offline after 1 hour of inactivity
- **WHEN** a sensor has `sensorStatus = ONLINE` and `lastSeen` is more than 1 hour ago
- **THEN** the system SHALL automatically set the sensor's `sensorStatus` to `OFFLINE` and update `updatedAt`

#### Scenario: Sensor in maintenance is not affected
- **WHEN** a sensor has `sensorStatus = MAINTENANCE` and `lastSeen` is more than 1 hour ago
- **THEN** the system SHALL NOT change the sensor's status

#### Scenario: Online sensor with recent data remains online
- **WHEN** a sensor has `sensorStatus = ONLINE` and `lastSeen` is less than 1 hour ago
- **THEN** the system SHALL NOT change the sensor's status

### Requirement: Scheduler execution frequency
The system SHALL execute the auto-offline detection job at a fixed interval (every 5 minutes). The job SHALL use a fixed delay to prevent overlapping executions.

#### Scenario: Scheduled job runs periodically
- **WHEN** the application is running
- **THEN** the system SHALL check for stale sensors every 5 minutes and update their statuses accordingly
