package io.plan.mate.expense.tracker.backend.repositories;

import io.plan.mate.expense.tracker.backend.entities.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {}
