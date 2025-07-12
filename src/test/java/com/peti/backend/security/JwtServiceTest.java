package com.peti.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

  private JwtService jwtService;
  // A valid 256\-bit key encoded in Base64 (32 bytes)
  private final String testSecretKey = "e68194748dc4b3deb09c7d28bfb1c441a2f3450667fb76fab80b60a1cf160ff9";

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
  }

  @Test
  void testGenerateAndValidateToken() {
    UserDetails user = User.withUsername("testuser")
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    var authResponse = jwtService.generateAuthResponse(user);
    assertNotNull(authResponse);
    assertNotNull(authResponse.getToken());

    assertTrue(jwtService.isTokenValid(authResponse.getToken(), user));
    assertEquals("testuser", jwtService.extractUsername(authResponse.getToken()));
  }

  @Test
  void testInvalidTokenSignature() {
    UserDetails user = User.withUsername("testuser")
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    var authResponse = jwtService.generateAuthResponse(user);
    String tamperedToken = authResponse.getToken() + "tampered";

    Exception exception = assertThrows(AccessDeniedException.class, () -> {
      jwtService.extractUsername(tamperedToken);
    });
    assertTrue(exception.getMessage().contains("Access denied"));
  }

  @Test
  void testExpiredToken() throws InterruptedException {
    // Set expiration to 10ms for testing expiration
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 10L);

    UserDetails user = User.withUsername("expiringUser")
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    var authResponse = jwtService.generateAuthResponse(user);
    // Wait a bit for the token to expire
    Thread.sleep(20);

    Exception exception = assertThrows(AccessDeniedException.class, () -> {
      jwtService.extractUsername(authResponse.getToken());
    });
    assertTrue(exception.getMessage().contains("Access denied"));
  }
}
