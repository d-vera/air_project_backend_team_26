package com.airproject.airproject.service;

import com.airproject.airproject.dto.AirQualityReadingNotificationDto;
import com.airproject.airproject.dto.SensorStatusNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class PgNotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(PgNotificationListener.class);

    public static final String AIR_QUALITY_CHANNEL = "air_quality_channel";
    public static final String SENSOR_STATUS_CHANNEL = "sensor_status_channel";

    public static final String TOPIC_READINGS = "/topic/readings";
    public static final String TOPIC_SENSORS_STATUS = "/topic/sensors/status";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/airproject}")
    private String dbUrl;

    @Value("${spring.datasource.username:postgres}")
    private String dbUsername;

    @Value("${spring.datasource.password:postgres}")
    private String dbPassword;

    @Value("${websocket.notifications.enabled:true}")
    private boolean enabled;

    @Value("${websocket.notifications.reconnect-interval-ms:5000}")
    private long reconnectIntervalMs;

    @Value("${websocket.notifications.poll-timeout-ms:1000}")
    private int pollTimeoutMs;

    private volatile boolean running = false;
    private Thread listenerThread;
    private Connection activeConnection;

    public PgNotificationListener(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        if (!enabled) {
            logger.info("Realtime PostgreSQL notification listener is disabled via configuration");
            return;
        }

        running = true;
        listenerThread = new Thread(this::listenLoop, "pg-notification-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        logger.info("Started PostgreSQL notification listener thread");
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        closeActiveConnection();
        logger.info("Stopped PostgreSQL notification listener");
    }

    private void listenLoop() {
        while (running) {
            try (Connection connection = createDedicatedConnection()) {
                activeConnection = connection;
                PGConnection pgConnection = connection.unwrap(PGConnection.class);

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("LISTEN " + AIR_QUALITY_CHANNEL);
                    stmt.execute("LISTEN " + SENSOR_STATUS_CHANNEL);
                }

                logger.info("Successfully subscribed to PostgreSQL channels: {}, {}",
                        AIR_QUALITY_CHANNEL, SENSOR_STATUS_CHANNEL);

                while (running && !connection.isClosed()) {
                    PGNotification[] notifications = pgConnection.getNotifications(pollTimeoutMs);
                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            processNotification(notification.getName(), notification.getParameter());
                        }
                    }
                }
            } catch (SQLException e) {
                if (!running) {
                    break;
                }
                logger.warn("PostgreSQL notification listener disconnected ({}). Reconnecting in {}ms...",
                        e.getMessage(), reconnectIntervalMs);
                try {
                    Thread.sleep(reconnectIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                if (!running) {
                    break;
                }
                logger.error("Unexpected error in PostgreSQL notification listener: {}", e.getMessage(), e);
                try {
                    Thread.sleep(reconnectIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                activeConnection = null;
            }
        }
    }

    protected Connection createDedicatedConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    public void processNotification(String channelName, String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }

        try {
            if (AIR_QUALITY_CHANNEL.equals(channelName)) {
                AirQualityReadingNotificationDto readingDto =
                        objectMapper.readValue(payload, AirQualityReadingNotificationDto.class);

                // Broadcast to global topic
                messagingTemplate.convertAndSend(TOPIC_READINGS, readingDto);

                // Broadcast to sensor-specific topic if device UID is present
                if (readingDto.getSensorUid() != null && !readingDto.getSensorUid().isBlank()) {
                    messagingTemplate.convertAndSend(TOPIC_READINGS + "/" + readingDto.getSensorUid(), readingDto);
                }

                logger.debug("Broadcasted air quality reading update for sensor: {}", readingDto.getSensorUid());
            } else if (SENSOR_STATUS_CHANNEL.equals(channelName)) {
                SensorStatusNotificationDto statusDto =
                        objectMapper.readValue(payload, SensorStatusNotificationDto.class);

                messagingTemplate.convertAndSend(TOPIC_SENSORS_STATUS, statusDto);
                logger.debug("Broadcasted sensor status update for sensor: {}", statusDto.getUidSensor());
            }
        } catch (Exception e) {
            logger.error("Failed to process PostgreSQL notification on channel [{}]: {}", channelName, e.getMessage(), e);
        }
    }

    private synchronized void closeActiveConnection() {
        if (activeConnection != null) {
            try {
                if (!activeConnection.isClosed()) {
                    activeConnection.close();
                }
            } catch (SQLException ignored) {
            }
            activeConnection = null;
        }
    }
}
