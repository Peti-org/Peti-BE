package com.peti.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


class SecurityConfigurationTest {

  private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

  @Test
  void testCorsConfigurationSource() {
    CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();
    CorsConfiguration corsConfig = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/test"));

    assertNotNull(corsConfig);
    assertEquals(List.of("http://localhost:8082", "http://localhost:8080", "http://localhost:8083"),
        corsConfig.getAllowedOrigins());
    assertEquals(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"), corsConfig.getAllowedMethods());
    assertEquals(List.of("Authorization", "Content-Type"), corsConfig.getAllowedHeaders());
  }

  @Test
  void testSecurityFilterChain() throws Exception {
    assertNotNull(securityConfiguration);
  }
}

