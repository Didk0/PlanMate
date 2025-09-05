package io.plan.mate.expense.tracker.backend.payloads.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Participant in a new expense")
public record CreateExpenseParticipant(
    @Schema(description = "Username of the participant", example = "alice")
        @NotBlank(message = "Username is required")
        String userName,
    @Schema(description = "Share amount for the participant", example = "41.15")
        @NotNull(message = "Share amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Share must be positive")
        BigDecimal shareAmount) {}
