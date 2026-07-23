package io.plan.mate.expense.tracker.backend.commons.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupAccessEvaluator")
class GroupAccessEvaluatorTest {

  @Mock private CurrentUserService currentUserService;
  @Mock private MemberRepository memberRepository;

  @InjectMocks private GroupAccessEvaluator groupAccessEvaluator;

  @Nested
  @DisplayName("isMember")
  class IsMember {

    @Test
    @DisplayName("returns true when the caller is a MEMBER of the group")
    void isMember_shouldReturnTrue_whenCallerIsMember() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findRoleByGroupIdAndUserId(1L, 7L))
          .thenReturn(Optional.of(MemberRole.MEMBER));

      assertThat(groupAccessEvaluator.isMember(1L)).isTrue();
    }

    @Test
    @DisplayName("returns true when the caller is the OWNER of the group")
    void isMember_shouldReturnTrue_whenCallerIsOwner() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findRoleByGroupIdAndUserId(1L, 7L))
          .thenReturn(Optional.of(MemberRole.OWNER));

      assertThat(groupAccessEvaluator.isMember(1L)).isTrue();
    }

    @Test
    @DisplayName("returns false when the caller has no membership in the group")
    void isMember_shouldReturnFalse_whenCallerHasNoMembership() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findRoleByGroupIdAndUserId(1L, 7L)).thenReturn(Optional.empty());

      assertThat(groupAccessEvaluator.isMember(1L)).isFalse();
    }

    @Test
    @DisplayName("returns true and short-circuits the membership lookup when the caller is ADMIN")
    void isMember_shouldReturnTrue_whenCallerIsAdmin() {
      when(currentUserService.isAdmin()).thenReturn(true);

      assertThat(groupAccessEvaluator.isMember(1L)).isTrue();

      verifyNoInteractions(memberRepository);
    }
  }

  @Nested
  @DisplayName("isOwner")
  class IsOwner {

    @Test
    @DisplayName("returns false when the caller is a plain MEMBER")
    void isOwner_shouldReturnFalse_whenCallerIsPlainMember() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findRoleByGroupIdAndUserId(1L, 7L))
          .thenReturn(Optional.of(MemberRole.MEMBER));

      assertThat(groupAccessEvaluator.isOwner(1L)).isFalse();
    }

    @Test
    @DisplayName("returns true when the caller is the OWNER")
    void isOwner_shouldReturnTrue_whenCallerIsOwner() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findRoleByGroupIdAndUserId(1L, 7L))
          .thenReturn(Optional.of(MemberRole.OWNER));

      assertThat(groupAccessEvaluator.isOwner(1L)).isTrue();
    }
  }

  @Nested
  @DisplayName("isSelf")
  class IsSelf {

    @Test
    @DisplayName("returns true when userId is the caller's own id")
    void isSelf_shouldReturnTrue_whenUserIdIsCallersOwn() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));

      assertThat(groupAccessEvaluator.isSelf(7L)).isTrue();
    }

    @Test
    @DisplayName("returns false when userId belongs to someone else")
    void isSelf_shouldReturnFalse_whenUserIdBelongsToAnotherUser() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));

      assertThat(groupAccessEvaluator.isSelf(9L)).isFalse();
    }

    @Test
    @DisplayName("returns true when the caller is ADMIN")
    void isSelf_shouldReturnTrue_whenCallerIsAdmin() {
      when(currentUserService.isAdmin()).thenReturn(true);

      assertThat(groupAccessEvaluator.isSelf(9L)).isTrue();
    }
  }

  @Nested
  @DisplayName("isOwnMembership")
  class IsOwnMembership {

    @Test
    @DisplayName("returns false when memberId belongs to another user's membership")
    void isOwnMembership_shouldReturnFalse_whenMemberIdBelongsToAnotherUser() {
      Member otherMembership = Member.builder().id(99L).build();

      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findByGroupIdAndUserId(1L, 7L)).thenReturn(Optional.of(otherMembership));

      assertThat(groupAccessEvaluator.isOwnMembership(1L, 5L)).isFalse();
    }

    @Test
    @DisplayName("returns true when memberId is the caller's own membership")
    void isOwnMembership_shouldReturnTrue_whenMemberIdIsCallersOwn() {
      Member ownMembership = Member.builder().id(5L).build();

      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findByGroupIdAndUserId(1L, 7L)).thenReturn(Optional.of(ownMembership));

      assertThat(groupAccessEvaluator.isOwnMembership(1L, 5L)).isTrue();
    }
  }
}
