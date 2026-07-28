package com.airproject.airproject.service;

import com.airproject.airproject.dto.AirQualityMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class MqttSubscriberService implements MqttCallbackExtended {

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriberService.class);

    private final IMqttClient mqttClient;
    private final AirQualityService airQualityService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.topic:calidad_aire/nodo1}")
    private String topic;

    @Value("${mqtt.qos:0}")
    private int qos;

    public MqttSubscriberService(IMqttClient mqttClient, AirQualityService airQualityService, ObjectMapper objectMapper) {
        this.mqttClient = mqttClient;
        this.airQualityService = airQualityService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(this);
            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topic, qos);
                logger.info("Subscribed to MQTT topic: {} with QoS {}", topic, qos);
            } else {
                logger.warn("MQTT client is not connected at startup. Subscription pending connection.");
            }
        } catch (MqttException e) {
            logger.error("Failed to subscribe to MQTT topic: {}", topic, e);
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            logger.info("MQTT connection re-established to {}. Re-subscribing to topic: {}", serverURI, topic);
            try {
                mqttClient.subscribe(topic, qos);
            } catch (MqttException e) {
                logger.error("Failed to re-subscribe to MQTT topic: {}", topic, e);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT connection lost: {}", cause.getMessage(), cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.debug("Received MQTT message on topic {}: {}", topic, payload);
        try {
            AirQualityMessage airQualityMessage = objectMapper.readValue(payload, AirQualityMessage.class);
            airQualityService.processAndSave(airQualityMessage, topic);
        } catch (Exception e) {
            logger.error("Failed to process MQTT message payload on topic {}: {}", topic, topic, e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No-op for subscriber
    }
}
