package com.airproject.airproject.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Bean
    public IMqttClient mqttClient() throws MqttException {
        String validBrokerUrl = brokerUrl;
        if (validBrokerUrl == null || validBrokerUrl.isBlank()) {
            logger.warn("MQTT broker URL is empty. Defaulting to tcp://localhost:1883");
            validBrokerUrl = "tcp://localhost:1883";
        } else if (validBrokerUrl.startsWith("http://") || validBrokerUrl.startsWith("https://")) {
            logger.warn("Invalid MQTT broker URL scheme '{}'. Defaulting to tcp://localhost:1883", brokerUrl);
            validBrokerUrl = "tcp://localhost:1883";
        } else if (!validBrokerUrl.contains("://")) {
            if (validBrokerUrl.contains(":8883")) {
                validBrokerUrl = "ssl://" + validBrokerUrl;
            } else {
                validBrokerUrl = "tcp://" + validBrokerUrl;
            }
            logger.info("Added scheme prefix to broker URL: {}", validBrokerUrl);
        } else if (validBrokerUrl.startsWith("tcp://") && validBrokerUrl.contains(":8883")) {
            validBrokerUrl = validBrokerUrl.replace("tcp://", "ssl://");
            logger.info("Port 8883 detected with tcp://. Automatically upgraded scheme to ssl://: {}", validBrokerUrl);
        }

        String actualClientId = (clientId != null && !clientId.isBlank())
                ? clientId + "-" + java.util.UUID.randomUUID().toString().substring(0, 8)
                : MqttClient.generateClientId();

        IMqttClient client = new MqttClient(validBrokerUrl, actualClientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(15);

        if (username != null && !username.isBlank()) {
            options.setUserName(username);
        }
        if (password != null && !password.isBlank()) {
            options.setPassword(password.toCharArray());
        }

        logger.info("Connecting to MQTT broker at {}", validBrokerUrl);
        client.connect(options);
        return client;
    }
}
