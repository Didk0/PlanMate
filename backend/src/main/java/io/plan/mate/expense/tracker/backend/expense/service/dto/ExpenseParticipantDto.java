package io.plan.mate.expense.tracker.backend.expense.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Details of a participant sharing an expense")
public class ExpenseParticipantDto {

  @Schema(description = "ID of the expense participant", example = "42")
  private Long id;

  @Schema(description = "Share amount of the participant in the expense", example = "41.15")
  private BigDecimal shareAmount;

  private String firstName;

  private String lastName;
}
