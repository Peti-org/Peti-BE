package com.peti.backend.security;

import com.peti.backend.dto.user.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final String TYPE = "type";
  private static final String ACCESS = "access";
  private static final String REFRESH = "refresh";

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

  @Value("${security.jwt.refresh.expiration-time}")
  private long jwtRefreshExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public AuthResponse generateAuthResponse(String username) {
    String token = generateToken(new HashMap<>(Map.of(TYPE, ACCESS)), username, jwtExpiration);
    String refreshToken = generateToken(new HashMap<>(Map.of(TYPE, REFRESH)), username, jwtRefreshExpiration);
    return new AuthResponse(token, Instant.now().plusMillis(jwtExpiration).getEpochSecond(),
        refreshToken, Instant.now().plusMillis(jwtRefreshExpiration).getEpochSecond());
  }

  public AuthResponse updateAuthResponse(String oldRefreshToken) {
    final String username = extractUsername(oldRefreshToken);
    return generateAuthResponse(username);
  }


  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private String generateToken(Map<String, Object> extraClaims, String username, long expiration) {
    return buildToken(extraClaims, username, expiration);
  }

  private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
    return Jwts
        .builder()
        .claims(extraClaims)
        .subject(username)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey())
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    String type = extractClaim(token, claims -> (String) claims.get(TYPE));
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && ACCESS.equals(type);
  }


  public boolean isRefreshTokenValid(String token) {
    String type = extractClaim(token, claims -> (String) claims.get(TYPE));
    return !isTokenExpired(token) && REFRESH.equals(type);
  }


  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser()
          .verifyWith(getSignInKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (SignatureException | MalformedJwtException | ExpiredJwtException e) { // Invalid signature or expired token
      throw new AccessDeniedException("Access denied: " + e.getMessage());
    }
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
