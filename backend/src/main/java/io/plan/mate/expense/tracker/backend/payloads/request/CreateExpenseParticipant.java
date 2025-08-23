package io.plan.mate.expense.tracker.backend.payloads.request;

import java.math.BigDecimal;

public record CreateExpenseParticipant(String userName, BigDecimal shareAmount) {}
