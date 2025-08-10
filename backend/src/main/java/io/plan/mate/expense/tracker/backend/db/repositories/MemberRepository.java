package io.plan.mate.expense.tracker.backend.db.repositories;

import io.plan.mate.expense.tracker.backend.db.entities.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  @EntityGraph(attributePaths = {"user", "group"})
  Optional<Member> findByGroupIdAndUserId(Long groupId, Long userId);
}
