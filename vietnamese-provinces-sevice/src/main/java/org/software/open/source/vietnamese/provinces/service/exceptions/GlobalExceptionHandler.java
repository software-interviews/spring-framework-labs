package org.software.open.source.vietnamese.provinces.service.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.BaseResponse;
import org.software.open.source.vietnamese.provinces.service.entries.apis.models.responses.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<BaseResponse<Void>> handleApplicationException(
      ApplicationException ex,
      WebRequest request) {
    log.error("ApplicationException occurred: {}", ex.getMessage(), ex);

    HttpStatus status = ex.getHttpStatus() != null
        ? ex.getHttpStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR;

    return buildErrorResponse(
        status,
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    log.error("MethodArgumentNotValidException occurred: {}", ex.getMessage(), ex);

    List<String> details = new ArrayList<>();

    ex.getBindingResult().getAllErrors().forEach(error -> {
      if (error instanceof FieldError fieldError) {
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage() != null
            ? fieldError.getDefaultMessage()
            : "Invalid value";

        details.add(field + ": " + message);
      } else {
        details.add(error.getDefaultMessage());
      }
    });

    ErrorDetails errorDetails = new ErrorDetails(
        "VALIDATION_ERROR",
        "Validation failed",
        details);

    BaseResponse<Void> body = BaseResponse.<Void>validationError(List.of(errorDetails));

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseResponse<Void>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    log.error("ConstraintViolationException occurred: {}", ex.getMessage(), ex);

    List<String> details = new ArrayList<>();

    ex.getConstraintViolations().forEach(violation -> {
      String field = "param";

      if (violation.getPropertyPath() != null) {
        field = violation.getPropertyPath().toString();

        int lastDotIndex = field.lastIndexOf('.');
        if (lastDotIndex >= 0) {
          field = field.substring(lastDotIndex + 1);
        }
      }

      String message = violation.getMessage() != null
          ? violation.getMessage()
          : "Invalid value";

      details.add(field + ": " + message);
    });

    ErrorDetails errorDetails = new ErrorDetails(
        "VALIDATION_ERROR",
        "Validation failed",
        details);

    BaseResponse<Void> body = BaseResponse.<Void>validationError(List.of(errorDetails));

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleNoResourceFoundException(
      NoResourceFoundException ex,
      WebRequest request) {
    log.error("NoResourceFoundException occurred: {}", ex.getMessage(), ex);

    return buildErrorResponse(
        HttpStatus.NOT_FOUND,
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleGlobalException(
      Exception ex,
      WebRequest request) {
    log.error("Unhandled exception occurred", ex);

    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getMessage(),
        request.getDescription(false));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(
      Exception ex,
      WebRequest request) {
    log.error("Unhandled exception occurred", ex);

    return buildErrorResponse(
        HttpStatus.BAD_REQUEST,
        ex.getMessage(),
        request.getDescription(false));
  }

  private ResponseEntity<BaseResponse<Void>> buildErrorResponse(
      HttpStatus status,
      String message,
      String path) {
    String errorMessage = message != null ? message : status.getReasonPhrase();

    List<String> details = new ArrayList<>();

    if (path != null && !path.isBlank()) {
      details.add(path);
    }

    ErrorDetails errorDetails = new ErrorDetails(
        status.getReasonPhrase(),
        errorMessage,
        details);

    BaseResponse<Void> body = BaseResponse.<Void>error(resolveCode(status), errorMessage)
        .withErrors(List.of(errorDetails));

    return new ResponseEntity<>(body, status);
  }

  private String resolveCode(HttpStatus status) {
    switch (status) {
      case BAD_REQUEST:
        return "BAD_REQUEST";

      case NOT_FOUND:
        return "NOT_FOUND";

      case INTERNAL_SERVER_ERROR:
        return "INTERNAL_ERROR";

      default:
        return status.name();
    }
  }

}
