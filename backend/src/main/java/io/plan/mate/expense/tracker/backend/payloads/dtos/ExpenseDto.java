package io.plan.mate.expense.tracker.backend.payloads.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
public class ExpenseDto {

  private Long id;
  private String description;
  private BigDecimal amount;
  private LocalDateTime createdAt;
  private Long groupId;
  private Long paidByUserId;
  private List<ExpenseParticipantDto> participants;
}
