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
        if (validBrokerUrl == null || validBrokerUrl.startsWith("http://") || validBrokerUrl.startsWith("https://")) {
            logger.warn("Invalid MQTT broker URL scheme '{}'. Defaulting to tcp://localhost:1883", brokerUrl);
            validBrokerUrl = "tcp://localhost:1883";
        }

        String actualClientId = (clientId != null && !clientId.isBlank())
                ? clientId + "-" + java.util.UUID.randomUUID().toString().substring(0, 8)
                : MqttClient.generateClientId();

        IMqttClient client = new MqttClient(validBrokerUrl, actualClientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        if (username != null && !username.isBlank()) {
            options.setUserName(username);
        }
        if (password != null && !password.isBlank()) {
            options.setPassword(password.toCharArray());
        }

        client.connect(options);
        return client;
    }
}
