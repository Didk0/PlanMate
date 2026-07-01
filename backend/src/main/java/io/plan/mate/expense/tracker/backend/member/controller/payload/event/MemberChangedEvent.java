package io.plan.mate.expense.tracker.backend.member.controller.payload.event;

import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;

public record MemberChangedEvent(MemberChangeEnum changeType, Long groupId, MemberDto member) {}
