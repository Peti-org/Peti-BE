package com.peti.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@SpringBootTest
public class SecurityConfigurationTest {

  @Autowired
  private SecurityFilterChain securityFilterChain;

  @Autowired
  private CorsConfigurationSource corsConfigurationSource;

  @Test
  public void testCorsConfigurationSource() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
    CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(request);
    assertNotNull(corsConfig);
    assertEquals(List.of("http://localhost:8082"), corsConfig.getAllowedOrigins());
    assertEquals(List.of("GET", "POST"), corsConfig.getAllowedMethods());
    assertEquals(List.of("Authorization", "Content-Type"), corsConfig.getAllowedHeaders());
  }

  @Test
  public void testSecurityFilterChainBean() {
    assertNotNull(securityFilterChain);
  }
}
