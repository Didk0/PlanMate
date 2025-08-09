package io.plan.mate.expense.tracker.backend.payloads.request;

import io.plan.mate.expense.tracker.backend.payloads.dtos.ExpenseParticipantDto;
import java.math.BigDecimal;
import java.util.List;

public record CreateExpenseRequest(
    String description,
    BigDecimal amount,
    Long groupId,
    Long paidByUserId,
    List<ExpenseParticipantDto> participants) {}
