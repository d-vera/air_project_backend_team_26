# mqtt-connection Specification

## Purpose
TBD - created by archiving change emqx-mqtt-integration. Update Purpose after archive.
## Requirements
### Requirement: MQTT client connects to EMQX broker on application startup
The system SHALL create an MQTT client bean that connects to the EMQX broker using the configured broker URL, client ID, username, and password on application startup. The connection SHALL use `cleanSession=true` and `automaticReconnect=true`.

#### Scenario: Successful connection with valid credentials
- **WHEN** the application starts with valid `MQTT_BROKER_URL`, `MQTT_USERNAME`, and `MQTT_PASSWORD` environment variables
- **THEN** the MQTT client connects to the EMQX broker and is ready to subscribe to topics

#### Scenario: Connection with default values
- **WHEN** the application starts without MQTT environment variables set
- **THEN** the MQTT client attempts to connect to `tcp://localhost:1883` with client ID `airproject-backend` and no authentication

### Requirement: MQTT connection parameters are configurable via environment variables
The system SHALL support the following environment variables for MQTT configuration, each with a default value:

| Variable | Default |
|----------|---------|
| `MQTT_BROKER_URL` | `tcp://localhost:1883` |
| `MQTT_CLIENT_ID` | `airproject-backend` |
| `MQTT_USERNAME` | _(empty)_ |
| `MQTT_PASSWORD` | _(empty)_ |
| `MQTT_TOPIC` | `calidad_aire/nodo1` |
| `MQTT_QOS` | `0` |

#### Scenario: Custom broker URL and credentials
- **WHEN** the environment variables `MQTT_BROKER_URL=tcp://emqx.example.com:1883`, `MQTT_USERNAME=admin`, and `MQTT_PASSWORD=secret` are set
- **THEN** the MQTT client connects to `tcp://emqx.example.com:1883` using username `admin` and password `secret`

### Requirement: MQTT client automatically reconnects on disconnection
The system SHALL configure the MQTT client with automatic reconnection enabled so that transient network failures do not permanently break the subscription.

#### Scenario: Broker temporarily unavailable
- **WHEN** the EMQX broker becomes temporarily unreachable after an established connection
- **THEN** the MQTT client automatically attempts to reconnect without manual intervention

