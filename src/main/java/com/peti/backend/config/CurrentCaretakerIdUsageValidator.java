package com.peti.backend.config;

import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.security.annotation.HasCaretakerRole;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Fails application startup if any controller method declares a {@link CurrentCaretakerId}
 * parameter without also being secured by {@link HasCaretakerRole}. This converts a class
 * of runtime authorization mistakes into a build-time failure (any {@code @SpringBootTest}
 * loading the full context will fail).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentCaretakerIdUsageValidator implements SmartInitializingSingleton {

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  @Override
  public void afterSingletonsInstantiated() {
    List<String> violations = findViolations();
    if (!violations.isEmpty()) {
      throw new IllegalStateException(
          "@CurrentCaretakerId is only allowed on handler methods annotated with @HasCaretakerRole. "
              + "Offending handlers:\n  - " + String.join("\n  - ", violations));
    }
    log.debug("@CurrentCaretakerId usage validation passed.");
  }

  private List<String> findViolations() {
    List<String> violations = new ArrayList<>();
    Map<RequestMappingInfo, HandlerMethod> handlers = requestMappingHandlerMapping.getHandlerMethods();
    for (HandlerMethod handler : handlers.values()) {
      Method method = handler.getMethod();
      if (!declaresCurrentCaretakerId(method) || method.isAnnotationPresent(HasCaretakerRole.class)) {
        continue;
      }
      violations.add(method.getDeclaringClass().getName() + "#" + method.getName());
    }
    return violations;
  }

  private static boolean declaresCurrentCaretakerId(Method method) {
    for (Parameter p : method.getParameters()) {
      if (p.isAnnotationPresent(CurrentCaretakerId.class)) {
        return true;
      }
    }
    return false;
  }
}

