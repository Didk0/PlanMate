package io.plan.mate.expense.tracker.backend.db.dtos;

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
public class MemberDto {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String groupName;
  private LocalDateTime joinedAt;
}
