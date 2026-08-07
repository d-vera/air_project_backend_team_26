package com.airproject.airproject;

import com.airproject.airproject.service.MqttSubscriberService;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class AirprojectApplicationTests {

    @MockitoBean
    private IMqttClient mqttClient;

    @MockitoBean
    private MqttSubscriberService mqttSubscriberService;

    @Test
    void contextLoads() {
    }
}
