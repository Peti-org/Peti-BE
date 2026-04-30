package com.peti.backend.controller.auth;

import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.RequestOAuthCode;
import com.peti.backend.service.auth.GoogleAuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth/oauth")
@RestController
@Tag(name = "Auth", description = "Operations needed to authenticate user")
@RequiredArgsConstructor
public class OAuthController {

  private final GoogleAuthenticationService googleAuthenticationService;

  @PostMapping("/google")
  public ResponseEntity<AuthResponse> authenticateWithGoogle(@Valid @RequestBody RequestOAuthCode request) {
    AuthResponse authResponse = googleAuthenticationService.authenticate(request);
    return ResponseEntity.ok(authResponse);
  }
}
