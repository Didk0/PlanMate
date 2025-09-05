package io.plan.mate.expense.tracker.backend.db.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(description = "Group information")
public class GroupDto {

  @Schema(description = "Group ID", example = "7")
  private Long id;

  @Schema(description = "Group name", example = "Summer Trip 2025")
  private String name;

  @Schema(description = "Group description", example = "Expenses for the Italy trip")
  private String description;

  @Schema(description = "Creation timestamp (UTC)", example = "2025-08-01T09:00:00")
  private LocalDateTime createdAt;
}
