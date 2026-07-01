package io.plan.mate.expense.tracker.backend.settlement.service;

import io.plan.mate.expense.tracker.backend.settlement.service.dto.SettlementDto;
import java.util.List;

public interface SettlementService {

  List<SettlementDto> calculateSettlements(Long groupId);

  void clearSettlementCache(Long groupId);
}
