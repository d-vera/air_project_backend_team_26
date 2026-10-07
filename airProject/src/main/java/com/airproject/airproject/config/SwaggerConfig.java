package com.airproject.airproject.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String API_DESCRIPTION = """
            REST API and Real-Time WebSocket backend for the Air Project ecosystem.

            ### Features:
            - **Authentication & Users**: Register, login, logout, profile self-service, admin user management, role assignments.
            - **Sensor Management**: Physical sensor registry, status tracking (ONLINE, OFFLINE, MAINTENANCE), coordinates, and soft-delete/reactivation.
            - **Air Quality Data**: MQTT message ingestion, latest readings, historical time-series queries.

            ---

            ### Real-Time WebSocket / STOMP API:
            - **Handshake Endpoint**: `ws://<host>:<port>/ws` (also supports SockJS fallback at `http://<host>:<port>/ws`).
            - **Protocol**: STOMP over WebSocket.
            - **Authentication**: Provide a valid JWT token in the `Authorization` header during the STOMP `CONNECT` frame:
              ```
              CONNECT
              accept-version:1.2
              Authorization:Bearer <jwt_token>
              ```
            - **Subscription Topics**:
              - `/topic/readings`: Live broadcast of newly inserted or updated air quality readings across all sensors.
              - `/topic/readings/{sensorUid}`: Targeted stream filtered for a specific sensor UID.
              - `/topic/sensors/status`: Real-time sensor lifecycle changes (ONLINE, OFFLINE, MAINTENANCE, activated, deactivated).
            """;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Air Project — API & Real-Time Services")
                        .version("1.0")
                        .description(API_DESCRIPTION))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token")));
    }
}
