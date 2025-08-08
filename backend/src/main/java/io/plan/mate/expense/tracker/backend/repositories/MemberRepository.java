package io.plan.mate.expense.tracker.backend.repositories;

import io.plan.mate.expense.tracker.backend.entities.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  @Query(
      "SELECT DISTINCT m FROM Member m JOIN FETCH m.group JOIN FETCH m.user "
          + "WHERE m.group.id = :groupId AND m.user.id = :userId")
  Optional<Member> findByGroupIdAndUserIdWithEagerFetch(Long groupId, Long userId);

  Optional<Member> findByGroupIdAndUserId(Long groupId, Long userId);

  @Query(
      "SELECT DISTINCT m FROM Member m JOIN FETCH m.group JOIN FETCH m.user WHERE m.group.id = :groupId")
  List<Member> findByGroupId(Long groupId);

  @Query(
      "SELECT DISTINCT m FROM Member m JOIN FETCH m.group JOIN FETCH m.user WHERE m.user.id = :userId")
  List<Member> findByUserId(Long userId);
}
