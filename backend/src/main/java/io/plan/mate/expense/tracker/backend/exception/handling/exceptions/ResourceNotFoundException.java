package io.plan.mate.expense.tracker.backend.exception.handling.exceptions;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {

  @Serial private static final long serialVersionUID = 3810459365739598781L;

  public ResourceNotFoundException(final String message) {
    super(message);
  }

  public ResourceNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
