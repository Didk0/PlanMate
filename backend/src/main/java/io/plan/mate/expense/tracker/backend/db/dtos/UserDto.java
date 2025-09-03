package io.plan.mate.expense.tracker.backend.db.dtos;

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
public class UserDto {

  private Long id;
  private UUID keycloakId;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private LocalDateTime createdAt;
}
