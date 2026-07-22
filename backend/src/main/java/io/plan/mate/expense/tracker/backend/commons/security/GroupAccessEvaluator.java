package io.plan.mate.expense.tracker.backend.commons.security;

import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SpEL entry point for {@code @PreAuthorize} group checks. ADMIN short-circuits every check here
 * so the annotations stay a single expression and the bypass has one home.
 */
@Component("groupAccess")
@RequiredArgsConstructor
public class GroupAccessEvaluator {

  private static final Set<MemberRole> ANY_MEMBER = EnumSet.allOf(MemberRole.class);
  private static final Set<MemberRole> OWNER_ONLY = EnumSet.of(MemberRole.OWNER);

  private final CurrentUserService currentUserService;
  private final MemberRepository memberRepository;

  public boolean isMember(final Long groupId) {
    return hasGroupRole(groupId, ANY_MEMBER);
  }

  public boolean isOwner(final Long groupId) {
    return hasGroupRole(groupId, OWNER_ONLY);
  }

  public boolean isSelf(final Long userId) {

    if (currentUserService.isAdmin()) {
      return true;
    }

    return currentUserService.findCurrentUserId().filter(userId::equals).isPresent();
  }

  public boolean isOwnMembership(final Long groupId, final Long memberId) {

    return currentUserService
        .findCurrentUserId()
        .flatMap(userId -> memberRepository.findByGroupIdAndUserId(groupId, userId))
        .filter(member -> member.getId().equals(memberId))
        .isPresent();
  }

  private boolean hasGroupRole(final Long groupId, final Set<MemberRole> allowedRoles) {

    if (currentUserService.isAdmin()) {
      return true;
    }

    return currentUserService
        .findCurrentUserId()
        .flatMap(userId -> memberRepository.findRoleByGroupIdAndUserId(groupId, userId))
        .filter(allowedRoles::contains)
        .isPresent();
  }
}
