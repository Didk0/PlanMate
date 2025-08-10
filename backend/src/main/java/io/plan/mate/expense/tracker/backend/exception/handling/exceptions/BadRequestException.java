package io.plan.mate.expense.tracker.backend.exception.handling.exceptions;

import java.io.Serial;

public class BadRequestException extends RuntimeException {

  @Serial private static final long serialVersionUID = -8139983163527244706L;

  public BadRequestException(final String message) {
    super(message);
  }

  public BadRequestException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
