package io.plan.mate.expense.tracker.backend.group.jpa.repository;

import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

}
