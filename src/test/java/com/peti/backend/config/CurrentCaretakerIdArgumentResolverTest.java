package com.peti.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.service.user.CaretakerService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentCaretakerIdArgumentResolverTest {

  @Mock
  private CaretakerService caretakerService;

  @InjectMocks
  private CurrentCaretakerIdArgumentResolver resolver;

  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final UUID CARETAKER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

  @BeforeEach
  void setUpSecurityContext() {
    UserProjection user = new UserProjection(USER_ID, "test@test.com", "pass", 2);
    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("supportsParameter returns true for @CurrentCaretakerId UUID parameter")
  void supportsParameter_annotatedUuid_returnsTrue() throws Exception {
    MethodParameter parameter = mock(MethodParameter.class);
    when(parameter.hasParameterAnnotation(CurrentCaretakerId.class)).thenReturn(true);
    when(parameter.getParameterType()).thenReturn((Class) UUID.class);

    assertThat(resolver.supportsParameter(parameter)).isTrue();
  }

  @Test
  @DisplayName("supportsParameter returns false for non-UUID parameter")
  void supportsParameter_wrongType_returnsFalse() throws Exception {
    MethodParameter parameter = mock(MethodParameter.class);
    when(parameter.hasParameterAnnotation(CurrentCaretakerId.class)).thenReturn(true);
    when(parameter.getParameterType()).thenReturn((Class) String.class);

    assertThat(resolver.supportsParameter(parameter)).isFalse();
  }

  @Test
  @DisplayName("supportsParameter returns false without annotation")
  void supportsParameter_noAnnotation_returnsFalse() throws Exception {
    MethodParameter parameter = mock(MethodParameter.class);
    when(parameter.hasParameterAnnotation(CurrentCaretakerId.class)).thenReturn(false);

    assertThat(resolver.supportsParameter(parameter)).isFalse();
  }

  @Test
  @DisplayName("resolveArgument returns caretaker ID when found")
  void resolveArgument_caretakerFound_returnsId() {
    when(caretakerService.getCaretakerIdByUserId(USER_ID)).thenReturn(Optional.of(CARETAKER_ID));

    MethodParameter parameter = mock(MethodParameter.class);
    UUID result = resolver.resolveArgument(parameter, null, null, null);

    assertThat(result).isEqualTo(CARETAKER_ID);
  }

  @Test
  @DisplayName("resolveArgument throws NotFoundException when caretaker not found")
  void resolveArgument_caretakerNotFound_throwsException() {
    when(caretakerService.getCaretakerIdByUserId(USER_ID)).thenReturn(Optional.empty());

    MethodParameter parameter = mock(MethodParameter.class);

    assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, null, null))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("Caretaker profile not found");
  }
}

