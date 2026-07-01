package io.plan.mate.expense.tracker.backend.expense.jpa.repository;

import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  @EntityGraph(attributePaths = {"participants", "participants.participant"})
  List<Expense> findByGroupId(Long groupId);
}
