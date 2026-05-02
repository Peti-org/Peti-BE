package com.peti.backend.config;

import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.service.user.CaretakerService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves method parameters annotated with {@link CurrentCaretakerId}.
 * Extracts the authenticated {@link UserProjection} from the security context,
 * then looks up the corresponding caretaker ID via {@link CaretakerService}.
 */
@Component
@RequiredArgsConstructor
public class CurrentCaretakerIdArgumentResolver implements HandlerMethodArgumentResolver {

  private final CaretakerService caretakerService;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentCaretakerId.class)
        && parameter.getParameterType().equals(UUID.class);
  }

  @Override
  public UUID resolveArgument(@NonNull MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserProjection user = (UserProjection) authentication.getPrincipal();
    return caretakerService.getCaretakerIdByUserId(user.getUserId())
        .orElseThrow(() -> new NotFoundException(
            "Caretaker profile not found for user: " + user.getUserId()));
  }
}

