package io.plan.mate.expense.tracker.backend.payloads.request;

import io.plan.mate.expense.tracker.backend.db.dtos.ExpenseParticipantDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request to create a new expense")
public record CreateExpenseRequest(
    @Schema(description = "Description of the expense", example = "Dinner at Italian restaurant")
        String description,
    @Schema(description = "Amount of the expense", example = "123.45")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
    @Schema(description = "ID of the group this expense belongs to", example = "10")
        @NotNull(message = "Group ID is required")
        Long groupId,
    @Schema(description = "User ID who paid the expense", example = "5")
        @NotNull(message = "Paid by User ID is required")
        Long paidByUserId,
    @Schema(description = "List of participants who share the expense")
        @NotNull(message = "Participants list cannot be null")
        @Size(min = 1, message = "At least one participant is required")
        @Valid
        List<ExpenseParticipantDto> participants) {}
