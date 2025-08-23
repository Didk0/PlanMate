package io.plan.mate.expense.tracker.backend.db.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
  @NotNull(message = "Participant ID is required")
  private Long id;

  @Schema(description = "Share amount of the participant in the expense", example = "41.15")
  @NotNull(message = "Share amount is required")
  @DecimalMin(value = "0.0", inclusive = false, message = "Share amount must be positive")
  private BigDecimal shareAmount;

  @JsonIgnore private ExpenseDto expense;

  private Long userId;

  private String userName;
}
