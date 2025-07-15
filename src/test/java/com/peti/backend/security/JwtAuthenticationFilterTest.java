package com.peti.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private HandlerExceptionResolver handlerExceptionResolver;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private UserDetails userDetails;

  @BeforeEach
  public void setUp() {
    // Clear any previous authentication
    SecurityContextHolder.clearContext();
  }

  @Test
  public void testFilterWithoutAuthorizationHeader() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService,
        handlerExceptionResolver);
    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    assert SecurityContextHolder.getContext().getAuthentication() == null;
  }

  @Test
  public void testFilterWithInvalidAuthorizationHeader() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Invalid token");

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService,
        handlerExceptionResolver);
    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    assert SecurityContextHolder.getContext().getAuthentication() == null;
  }

  @Test
  public void testFilterWithValidToken() throws ServletException, IOException {
    String token = "validToken";
    String email = "user@example.com";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn(email);
    when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService,
        handlerExceptionResolver);
    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assert authentication != null;
    assert authentication instanceof UsernamePasswordAuthenticationToken;
    assert userDetails.equals(authentication.getPrincipal());
  }

  @Test
  public void testExceptionHandling() throws ServletException, IOException {
    String token = "badToken";
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Token extraction failed"));

    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService,
        handlerExceptionResolver);
    filter.doFilterInternal(request, response, filterChain);

    verify(handlerExceptionResolver, times(1))
        .resolveException(eq(request), eq(response), isNull(), any(RuntimeException.class));
  }
}
