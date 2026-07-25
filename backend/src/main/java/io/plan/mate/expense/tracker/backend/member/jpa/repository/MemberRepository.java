package io.plan.mate.expense.tracker.backend.member.jpa.repository;

import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  @EntityGraph(attributePaths = {"group", "user"})
  Optional<Member> findByGroupIdAndUserId(Long groupId, Long userId);

  @EntityGraph(attributePaths = {"user", "group"})
  List<Member> findByGroupId(Long groupId);

  @EntityGraph(attributePaths = {"user", "group"})
  Optional<Member> findByGroupIdAndId(Long groupId, Long memberId);

  boolean existsByUserId(Long userId);

  @EntityGraph(attributePaths = "group")
  List<Member> findByUserId(Long userId);

  @Query("select m.role from Member m where m.group.id = :groupId and m.user.id = :userId")
  Optional<MemberRole> findRoleByGroupIdAndUserId(
      @Param("groupId") Long groupId, @Param("userId") Long userId);

  long countByGroupIdAndRole(Long groupId, MemberRole role);

  @Query("select m.user.id from Member m where m.group.id = :groupId and m.user.id in :userIds")
  Set<Long> findMemberUserIdsByGroupIdAndUserIdIn(
      @Param("groupId") Long groupId, @Param("userIds") Collection<Long> userIds);
}
