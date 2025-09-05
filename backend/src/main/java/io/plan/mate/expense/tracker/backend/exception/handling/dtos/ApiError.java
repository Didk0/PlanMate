package io.plan.mate.expense.tracker.backend.exception.handling.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "Standard API error response")
public class ApiError {

  @Schema(description = "UTC timestamp of the error", example = "2025-09-05T10:15:30")
  private LocalDateTime timestamp;

  @Schema(description = "HTTP status code", example = "404")
  private int status;

  @Schema(description = "Short error name", example = "Not Found")
  private String error;

  @Schema(description = "Human-readable error message", example = "Group with id 7 not found")
  private String message;

  @Schema(description = "Request path", example = "/api/groups/7")
  private String path;
}
