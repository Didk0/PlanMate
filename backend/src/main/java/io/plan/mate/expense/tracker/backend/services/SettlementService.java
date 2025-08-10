package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.payloads.dtos.SettlementDto;
import java.util.List;

public interface SettlementService {

  List<SettlementDto> getSettlementsByGroup(Long groupId);

    List<SettlementDto> calculateSettlements(Long groupId);
}
