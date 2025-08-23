package io.plan.mate.expense.tracker.backend.payloads.events;

import io.plan.mate.expense.tracker.backend.db.dtos.SettlementDto;
import java.util.List;

public record SettlementCalculatedEvent(Long groupId, List<SettlementDto> settlements) {}
