package io.plan.mate.expense.tracker.backend.payloads.events;

import io.plan.mate.expense.tracker.backend.db.dtos.ExpenseDto;

public record ExpenseCreatedEvent(ExpenseChangeEnum changeType, ExpenseDto expense) {}
