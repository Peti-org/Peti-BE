package com.peti.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.auth.GoogleTokenResponse;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.RequestOAuthCode;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.security.JwtService;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationService {

  private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

  private final UserRepository userRepository;
  private final UserService userService;
  private final CityService cityService;
  private final RoleService roleService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  private final RestClient restClient = RestClient.create();

  private GoogleIdTokenVerifier googleIdTokenVerifier;

  @Value("${google.oauth.client-id}")
  private String googleClientId;

  @Value("${google.oauth.client-secret}")
  private String googleClientSecret;

  @Value("${google.oauth.default-city-id:1}")
  private Long defaultCityId;

  @Value("${google.oauth.default-birth-date:2000-01-01}")
  private String defaultBirthDate;

  @PostConstruct
  void initializeVerifier() {
    if (isEmpty(googleClientId)) {
      return;
    }
    googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(), JacksonFactory.getDefaultInstance())
        .setAudience(Collections.singletonList(googleClientId))
        .setIssuer(Arrays.asList("https://accounts.google.com", "accounts.google.com"))
        .build();
  }

  public AuthResponse authenticate(RequestOAuthCode request) {
    GoogleTokenResponse tokenResponse = exchangeCodeForToken(request);
    GoogleIdToken.Payload payload = verifyIdToken(tokenResponse.getIdToken());

    String email = payload.getEmail();
    if (email == null || email.isBlank()) {
      throw new BadRequestException("Google account email is missing");
    }

    Optional<User> existingUser = userRepository.findByEmail(email);
    if (existingUser.isPresent() && existingUser.get().isUserIsDeleted()) {
      throw new BadRequestException("User account is deleted");
    }

    if (existingUser.isEmpty()) {
      createUserFromGooglePayload(payload);
    }

    return jwtService.generateAuthResponse(email);
  }

  private GoogleTokenResponse exchangeCodeForToken(RequestOAuthCode request) {
    if (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null || googleClientSecret.isBlank()) {
      throw new BadRequestException("Google OAuth client is not configured");
    }

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("code", request.getCode());
    body.add("client_id", googleClientId);
    body.add("client_secret", googleClientSecret);
    body.add("redirect_uri", request.getRedirectUri());
    body.add("grant_type", "authorization_code");

    GoogleTokenResponse response;
    try {
      response = restClient
          .post()
          .uri(GOOGLE_TOKEN_URL)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(body)
          .retrieve()
          .body(GoogleTokenResponse.class);
    } catch (RestClientResponseException ex) {
      throw new BadRequestException("Google OAuth exchange failed: " + ex.getStatusText());
    }

    if (response == null || response.getIdToken() == null || response.getIdToken().isBlank()) {
      throw new BadRequestException("Invalid Google OAuth response");
    }

    return response;
  }

  private GoogleIdToken.Payload verifyIdToken(String idToken) {
    try {
      if (googleIdTokenVerifier == null) {
        throw new BadRequestException("Google OAuth client is not configured");
      }

      GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
      if (googleIdToken == null) {
        throw new BadRequestException("Invalid Google ID token");
      }

      GoogleIdToken.Payload payload = googleIdToken.getPayload();
      String authorizedParty = payload.getAuthorizedParty();
      if (authorizedParty != null && !authorizedParty.isBlank() && !authorizedParty.equals(googleClientId)) {
        throw new BadRequestException("Invalid Google ID token audience");
      }
      Boolean emailVerified = payload.getEmailVerified();
      if (emailVerified == null || !emailVerified) {
        throw new BadRequestException("Google account email is not verified");
      }

      return payload;
    } catch (Exception e) {
      throw new BadRequestException("Google token verification failed: " + e.getMessage());
    }
  }

  private void createUserFromGooglePayload(GoogleIdToken.Payload payload) {
    CityDto cityDto = cityService.fetchById(defaultCityId)
        .orElseThrow(() -> new BadRequestException(
            String.format("Default city with id %s not found", defaultCityId)));

    User user = new User();
    user.setEmail(payload.getEmail());
    user.setFirstName(resolveString(payload, "given_name", "name", "User"));
    user.setLastName(resolveString(payload, "family_name", null, ""));
    user.setBirthday(Date.valueOf(LocalDate.parse(defaultBirthDate)));
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
    user.setUserDataFolder("default");
    user.setUserIsDeleted(false);
    user.setCityByCityId(cityDto.toCityWithId());
    user.setRole(roleService.getUserRole());

    userService.createUser(user);
  }

  private String resolveString(GoogleIdToken.Payload payload, String primaryKey, String fallbackKey, String defaultValue) {
    Object primary = primaryKey != null ? payload.get(primaryKey) : null;
    if (primary instanceof String primaryValue && !primaryValue.isBlank()) {
      return primaryValue;
    }
    Object fallback = fallbackKey != null ? payload.get(fallbackKey) : null;
    if (fallback instanceof String fallbackValue && !fallbackValue.isBlank()) {
      return fallbackValue;
    }
    return defaultValue;
  }
}