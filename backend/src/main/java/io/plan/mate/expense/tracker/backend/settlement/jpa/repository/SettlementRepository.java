package io.plan.mate.expense.tracker.backend.settlement.jpa.repository;

import io.plan.mate.expense.tracker.backend.settlement.jpa.entity.Settlement;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  @EntityGraph(attributePaths = {"fromUser", "toUser", "group"})
  List<Settlement> findByGroupId(Long groupId);

  @Modifying
  @Transactional
  @Query("DELETE FROM Settlement s WHERE s.group.id = :groupId")
  void deleteByGroupId(Long groupId);
}
