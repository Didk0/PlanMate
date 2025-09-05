package io.plan.mate.expense.tracker.backend.db.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Expense information returned by the API")
public class ExpenseDto {

  @Schema(description = "Expense ID", example = "101")
  private Long id;

  @Schema(description = "Expense description", example = "Dinner at Luigi's")
  private String description;

  @Schema(description = "Total amount of the expense", example = "123.45")
  private BigDecimal amount;

  @Schema(description = "Creation timestamp (UTC)", example = "2025-09-05T10:15:30")
  private LocalDateTime createdAt;

  @Schema(description = "First name of the payer", example = "Alice")
  private String paidByFirstName;

  @Schema(description = "Last name of the payer", example = "Johnson")
  private String paidByLastName;

  @Schema(description = "Participants sharing this expense")
  private List<ExpenseParticipantDto> participants;
}
