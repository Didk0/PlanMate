package io.plan.mate.expense.tracker.backend.payloads.dtos;

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
  private String userName;
  private String groupName;
  private LocalDateTime joinedAt;
}
