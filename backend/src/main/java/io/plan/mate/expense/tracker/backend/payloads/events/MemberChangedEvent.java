package io.plan.mate.expense.tracker.backend.payloads.events;

import io.plan.mate.expense.tracker.backend.db.dtos.MemberDto;

public record MemberChangedEvent(MemberChangeEnum changeType, Long groupId, MemberDto member) {}
