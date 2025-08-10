package io.plan.mate.expense.tracker.backend.db.repositories;

import io.plan.mate.expense.tracker.backend.db.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

}
