package org.software.open.source.vietnamese.provinces.service.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApplicationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final HttpStatus httpStatus;

  public ApplicationException(String message) {
    super(message);
    this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // Default value
  }

  public ApplicationException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

}
