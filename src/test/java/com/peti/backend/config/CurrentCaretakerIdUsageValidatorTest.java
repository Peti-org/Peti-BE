package com.peti.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.security.annotation.HasCaretakerRole;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@ExtendWith(MockitoExtension.class)
class CurrentCaretakerIdUsageValidatorTest {

  @Mock
  private RequestMappingHandlerMapping handlerMapping;

  @InjectMocks
  private CurrentCaretakerIdUsageValidator validator;

  @Test
  void passes_whenAllHandlersAreCompliant() throws Exception {
    when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(
        info("a"), handlerFor(Handlers.class.getDeclaredMethod("compliant", UUID.class)),
        info("b"), handlerFor(Handlers.class.getDeclaredMethod("noCaretakerParam"))
    ));

    assertThatCode(validator::afterSingletonsInstantiated).doesNotThrowAnyException();
  }

  @Test
  void fails_whenCurrentCaretakerIdMissingHasCaretakerRole() throws Exception {
    when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(
        info("a"), handlerFor(Handlers.class.getDeclaredMethod("missingRole", UUID.class))
    ));

    assertThatThrownBy(validator::afterSingletonsInstantiated)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("@HasCaretakerRole")
        .hasMessageContaining("missingRole");
  }

  @Test
  void aggregatesMultipleViolations() throws Exception {
    when(handlerMapping.getHandlerMethods()).thenReturn(Map.of(
        info("a"), handlerFor(Handlers.class.getDeclaredMethod("missingRole", UUID.class)),
        info("b"), handlerFor(Handlers.class.getDeclaredMethod("alsoMissingRole", UUID.class)),
        info("c"), handlerFor(Handlers.class.getDeclaredMethod("compliant", UUID.class))
    ));

    assertThatThrownBy(validator::afterSingletonsInstantiated)
        .isInstanceOf(IllegalStateException.class)
        .satisfies(ex -> {
          assertThat(ex.getMessage()).contains("missingRole");
          assertThat(ex.getMessage()).contains("alsoMissingRole");
          assertThat(ex.getMessage()).doesNotContain("compliant");
        });
  }

  private static HandlerMethod handlerFor(Method method) {
    return new HandlerMethod(new Handlers(), method);
  }

  private static RequestMappingInfo info(String path) {
    return RequestMappingInfo.paths("/" + path).build();
  }

  @SuppressWarnings("unused")
  static class Handlers {
    @HasCaretakerRole
    public void compliant(@CurrentCaretakerId UUID caretakerId) {
    }

    public void missingRole(@CurrentCaretakerId UUID caretakerId) {
    }

    public void alsoMissingRole(@CurrentCaretakerId UUID caretakerId) {
    }

    public void noCaretakerParam() {
    }
  }
}

