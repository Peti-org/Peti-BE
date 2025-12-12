package com.peti.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import com.peti.backend.dto.user.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest {

  // A valid 256\-bit key encoded in Base64 (32 bytes)
  private final String testSecretKey = "e68194748dc4b3deb09c7d28bfb1c441a2f3450667fb76fab80b60a1cf160ff9";
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    ReflectionTestUtils.setField(jwtService, "jwtRefreshExpiration", 7200000L); // 2 hours
  }

  @Test
  void testGenerateAndValidateToken() {
    UserDetails user = User.withUsername("testuser")
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    AuthResponse authResponse = jwtService.generateAuthResponse("testuser");
    assertNotNull(authResponse);
    assertNotNull(authResponse.getToken());

    assertTrue(jwtService.isTokenValid(authResponse.getToken(), user));
    assertEquals("testuser", jwtService.extractUsername(authResponse.getToken()));
  }

  @Test
  void testInvalidTokenSignature() {
    AuthResponse authResponse = jwtService.generateAuthResponse("testuser");
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

    AuthResponse authResponse = jwtService.generateAuthResponse("expiringUser");
    Thread.sleep(20);

    Exception exception = assertThrows(AccessDeniedException.class, () -> {
      jwtService.extractUsername(authResponse.getToken());
    });
    assertTrue(exception.getMessage().contains("Access denied"));
  }

  @Test
  void testGenerateAndValidateRefreshToken() {
    AuthResponse authResponse = jwtService.generateAuthResponse("refreshUser");
    String refreshToken = authResponse.getRefreshToken();

    assertNotNull(refreshToken);
    assertTrue(jwtService.isRefreshTokenValid(refreshToken));
    assertEquals("refreshUser", jwtService.extractUsername(refreshToken));
  }

  @Test
  void testInvalidRefreshTokenType() {
    AuthResponse authResponse = jwtService.generateAuthResponse("wrongTypeUser");
    String accessToken = authResponse.getToken();

    assertThrows(AccessDeniedException.class, ()->jwtService.isRefreshTokenValid(accessToken+"wrong"));
  }
}
