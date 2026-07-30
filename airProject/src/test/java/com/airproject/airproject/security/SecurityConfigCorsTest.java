package com.airproject.airproject.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigCorsTest {

    @Test
    void corsConfiguration_ShouldAllowOriginPort4200AndOtherPorts() {
        SecurityConfig config = new SecurityConfig(null);
        CorsConfigurationSource source = config.corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        request.addHeader("Origin", "http://localhost:4200");

        CorsConfiguration corsConfig = source.getCorsConfiguration(request);
        assertNotNull(corsConfig);
        assertEquals("http://localhost:4200", corsConfig.checkOrigin("http://localhost:4200"));
        assertEquals("http://localhost:3000", corsConfig.checkOrigin("http://localhost:3000"));
        assertTrue(corsConfig.getAllowCredentials());
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }
}
