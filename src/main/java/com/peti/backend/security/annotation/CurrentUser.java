package com.peti.backend.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Injects the currently authenticated {@link com.peti.backend.model.projection.UserProjection}
 * from the Spring Security context. Replaces the repeated {@code @ModelAttribute("userProjection")}
 * pattern across controllers.
 *
 * <p>Usage:
 * <pre>
 *   public ResponseEntity<?> myEndpoint(@CurrentUser UserProjection user) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {

}

