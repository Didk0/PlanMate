package io.plan.mate.expense.tracker.backend.db.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
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
@Schema(description = "User information")
public class UserDto {

  @Schema(description = "User ID", example = "9")
  private Long id;

  @Schema(description = "Keycloak user ID", example = "1f8b6d02-5b66-4b2d-9f2e-2a01f91e2a11")
  private UUID keycloakId;

  @Schema(description = "Username", example = "alice")
  private String username;

  @Schema(description = "Email", example = "alice@example.com")
  private String email;

  @Schema(description = "First name", example = "Alice")
  private String firstName;

  @Schema(description = "Last name", example = "Johnson")
  private String lastName;

  @Schema(description = "Creation timestamp (UTC)", example = "2025-08-01T09:00:00")
  private LocalDateTime createdAt;
}
