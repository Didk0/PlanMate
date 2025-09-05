package io.plan.mate.expense.tracker.backend.db.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Group membership details")
public class MemberDto {

  @Schema(description = "Membership ID", example = "12")
  private Long id;

  @Schema(description = "Username of the member", example = "alice")
  private String username;

  @Schema(description = "First name", example = "Alice")
  private String firstName;

  @Schema(description = "Last name", example = "Johnson")
  private String lastName;

  @Schema(description = "Name of the group", example = "Summer Trip 2025")
  private String groupName;

  @Schema(description = "Join timestamp (UTC)", example = "2025-08-01T10:00:00")
  private LocalDateTime joinedAt;
}
