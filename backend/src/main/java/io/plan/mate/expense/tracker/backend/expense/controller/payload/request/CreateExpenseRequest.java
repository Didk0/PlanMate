package io.plan.mate.expense.tracker.backend.expense.controller.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request to create a new expense")
public record CreateExpenseRequest(
    @Schema(description = "Description of the expense", example = "Dinner at Italian restaurant")
        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,
    @Schema(description = "Amount of the expense", example = "123.45")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,
    @Schema(description = "Name of the user who paid the expense", example = "John")
        @NotBlank(message = "Paid by Username is required")
        String paidByUsername,
    @Schema(description = "List of participants who share the expense")
        @NotNull(message = "Participants list cannot be null")
        @Size(min = 1, message = "At least one participant is required")
        @Valid
        List<CreateExpenseParticipant> participants) {}
