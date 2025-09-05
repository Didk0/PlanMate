package io.plan.mate.expense.tracker.backend.db.dtos;

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
@Schema(description = "Settlement transfer between two users")
public class SettlementDto {

  @Schema(description = "Settlement ID", example = "55")
  private Long id;

  @Schema(description = "Group where the settlement belongs")
  private GroupDto group;

  @Schema(description = "Payer first name", example = "Bob")
  private String fromUserFirstName;

  @Schema(description = "Payer last name", example = "Smith")
  private String fromUserLastName;

  @Schema(description = "Receiver first name", example = "Alice")
  private String toUserFirstName;

  @Schema(description = "Receiver last name", example = "Johnson")
  private String toUserLastName;

  @Schema(description = "Amount to transfer", example = "37.90")
  private BigDecimal amount;
}
