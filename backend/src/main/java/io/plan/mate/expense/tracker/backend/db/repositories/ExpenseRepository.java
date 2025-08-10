package io.plan.mate.expense.tracker.backend.db.repositories;

import io.plan.mate.expense.tracker.backend.db.entities.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  @EntityGraph(attributePaths = {"participants", "participants.participant"})
  Optional<List<Expense>> findByGroupId(Long groupId);
}
