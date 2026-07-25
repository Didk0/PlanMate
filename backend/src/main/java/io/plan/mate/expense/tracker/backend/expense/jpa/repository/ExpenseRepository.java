package io.plan.mate.expense.tracker.backend.expense.jpa.repository;

import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  @EntityGraph(attributePaths = {"participants", "participants.participant", "paidBy"})
  List<Expense> findByGroupId(Long groupId);

  @Query(
      "select e.id from Expense e where e.group.id = :groupId and (:search is null "
          + "or lower(e.description) like lower(concat('%', cast(:search as string), '%')))")
  Page<Long> findExpenseIdsByGroupId(
      @Param("groupId") Long groupId, @Param("search") String search, Pageable pageable);

  @EntityGraph(attributePaths = {"participants", "participants.participant", "paidBy"})
  List<Expense> findByIdIn(Collection<Long> ids);

  boolean existsByPaidById(Long userId);

  boolean existsByParticipants_ParticipantId(Long userId);
}
