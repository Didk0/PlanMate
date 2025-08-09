package io.plan.mate.expense.tracker.backend.repositories;

import io.plan.mate.expense.tracker.backend.entities.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  Optional<List<Expense>> findByGroupId(Long groupId);
}
