package com.exqzore.resourceservice.exception;

public class InvalidBodyOrValidationException extends RuntimeException {
  public InvalidBodyOrValidationException() {
    super();
  }

  public InvalidBodyOrValidationException(String message) {
    super(message);
  }

  public InvalidBodyOrValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidBodyOrValidationException(Throwable cause) {
    super(cause);
  }
}
