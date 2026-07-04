package io.plan.mate.expense.tracker.backend.settlement.controller.payload.event;

public record SettlementsChangedEvent(SettlementsChangeEnum changeType, Long groupId) {}
