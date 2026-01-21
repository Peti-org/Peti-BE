package com.peti.backend.controller;


import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.dto.exception.NotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ProblemDetail handleBadRequestException(BadRequestException exception) {
    ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    errorDetail.setProperty("description", exception.getMessage());
    return errorDetail;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
        "Invalid request body. Please check the format and content.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
    ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
    errorDetail.setProperty("errors", errors);
    return errorDetail;
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
    if (exception.getMessage().contains("email")) {
      return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Email already exists");
    }
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Data integrity violation");
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ProblemDetail handleUerNotFoundException(UsernameNotFoundException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleUerNotFoundException(NotFoundException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(AccessDeniedException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }


  @ExceptionHandler(Exception.class)
  public ProblemDetail handleSecurityException(Exception exception) {
    ProblemDetail errorDetail = null;

    // TODO send this stack trace to an observability tool
    // TODO add logging, move it to separate methods
    exception.printStackTrace();

    if (exception instanceof BadCredentialsException) {
      errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
      errorDetail.setProperty("description", "The username or password is incorrect");

      return errorDetail;
    }

    if (exception instanceof AccountStatusException) {
      errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
      errorDetail.setProperty("description", "The account is locked");
    }

    if (exception instanceof SignatureException) {
      errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
      errorDetail.setProperty("description", "The JWT signature is invalid");
    }

    if (exception instanceof ExpiredJwtException) {
      errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
      errorDetail.setProperty("description", "The JWT token has expired");
    }

    if (errorDetail == null) {
      errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
      errorDetail.setProperty("description", "Unknown internal server error.");
    }

    return errorDetail;
  }
}
