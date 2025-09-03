package io.plan.mate.expense.tracker.backend.db.repositories;

import io.plan.mate.expense.tracker.backend.db.entities.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  @EntityGraph(attributePaths = {"group", "user"})
  Optional<Member> findByGroupIdAndUserId(Long groupId, Long userId);

  @EntityGraph(attributePaths = {"user", "group"})
  List<Member> findByGroupId(Long groupId);

  @EntityGraph(attributePaths = {"user", "group"})
  Optional<Member> findByGroupIdAndId(Long groupId, Long memberId);
}
