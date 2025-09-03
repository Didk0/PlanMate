package io.plan.mate.expense.tracker.backend.db.dtos;

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
  private GroupDto group;
  private String fromUserFirstName;
  private String fromUserLastName;
  private String toUserFirstName;
  private String toUserLastName;
  private BigDecimal amount;
}
