package io.plan.mate.expense.tracker.backend.db.repositories;

import io.plan.mate.expense.tracker.backend.db.entities.Settlement;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM Settlement s WHERE s.group.id = :groupId")
  void deleteByGroupId(Long groupId);
}
