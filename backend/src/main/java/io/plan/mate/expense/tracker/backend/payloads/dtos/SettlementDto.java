package io.plan.mate.expense.tracker.backend.payloads.dtos;

import java.math.BigDecimal;
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
public class SettlementDto {

  private Long id;
  private Long groupId;
  private Long fromUserId;
  private String fromUserName;
  private Long toUserId;
  private String toUserName;
  private BigDecimal amount;
}
