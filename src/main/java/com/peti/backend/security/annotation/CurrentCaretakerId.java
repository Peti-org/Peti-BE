package com.peti.backend.security.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the {@link java.util.UUID} caretaker ID of the currently authenticated user.
 * Resolved by {@link com.peti.backend.config.CurrentCaretakerIdArgumentResolver}.
 *
 * <p>Throws {@link com.peti.backend.model.exception.NotFoundException} if the authenticated
 * user has no caretaker profile.
 *
 * <p>Usage:
 * <pre>
 *   public ResponseEntity<?> myEndpoint(@CurrentCaretakerId UUID caretakerId) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
public @interface CurrentCaretakerId {

}

