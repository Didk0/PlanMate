package io.plan.mate.expense.tracker.backend.repositories;

import io.plan.mate.expense.tracker.backend.entities.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  Optional<Member> findByGroupIdAndUserId(Long groupId, Long userId);

  List<Member> findByGroupId(Long groupId);

  List<Member> findByUserId(Long userId);
}
