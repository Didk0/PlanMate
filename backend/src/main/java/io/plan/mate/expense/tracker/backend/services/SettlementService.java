package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.db.dtos.SettlementDto;
import java.util.List;

public interface SettlementService {

  List<SettlementDto> getSettlementsByGroup(Long groupId);

  List<SettlementDto> calculateSettlements(Long groupId);

  void clearSettlementCache(Long groupId);
}
