package io.plan.mate.expense.tracker.backend.exception.handling.dtos;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ApiError {

  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
}
