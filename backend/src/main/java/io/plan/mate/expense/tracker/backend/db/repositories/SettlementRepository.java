package io.plan.mate.expense.tracker.backend.db.repositories;

import io.plan.mate.expense.tracker.backend.db.entities.Settlement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  List<Settlement> findByGroupId(Long groupId);

  void deleteByGroupId(Long groupId);
}
