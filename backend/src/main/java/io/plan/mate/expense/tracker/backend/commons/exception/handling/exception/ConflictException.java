package io.plan.mate.expense.tracker.backend.commons.exception.handling.exception;

import java.io.Serial;

public class ConflictException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public ConflictException(final String message) {
    super(message);
  }

  public ConflictException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
