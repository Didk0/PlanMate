package io.plan.mate.expense.tracker.backend.expense.controller.payload.event;

import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;

public record ExpenseCreatedEvent(ExpenseChangeEnum changeType, Long groupId, ExpenseDto expense) {}
