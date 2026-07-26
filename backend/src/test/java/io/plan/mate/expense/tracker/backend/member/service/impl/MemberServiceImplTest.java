package io.plan.mate.expense.tracker.backend.member.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ConflictException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.group.service.mapper.GroupMapperImpl;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.controller.payload.request.AddUserRequest;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
import io.plan.mate.expense.tracker.backend.member.service.mapper.MemberMapperImpl;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl")
class MemberServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private GroupRepository groupRepository;
  @Spy private MemberMapperImpl memberMapper;
  @Spy private GroupMapperImpl groupMapper;
  @Mock private SettlementService settlementService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private MemberServiceImpl memberService;

  @Nested
  @DisplayName("addUserToGroup")
  class AddUserToGroup {

    @Test
    @DisplayName("clears the settlement cache and publishes member + settlements events")
    void addUserToGroup_shouldClearCacheAndPublishEvents_whenMemberAdded() {
      User user = User.builder().id(1L).username("alice").build();
      Group group = Group.builder().id(1L).name("Trip").build();
      Member member = Member.builder().id(1L).user(user).group(group).build();

      when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(memberRepository.findByGroupIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
      when(memberRepository.save(any())).thenReturn(member);

      memberService.addUserToGroup(1L, new AddUserRequest("alice"));

      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(MemberChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }

    @Test
    @DisplayName("persists the new member with the MEMBER role")
    void addUserToGroup_shouldPersistMemberWithMemberRole_whenUserIsAdded() {
      User user = User.builder().id(1L).username("alice").build();
      Group group = Group.builder().id(1L).name("Trip").build();
      Member savedMember = Member.builder().id(1L).user(user).group(group).build();

      when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(memberRepository.findByGroupIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
      when(memberRepository.save(any())).thenReturn(savedMember);

      memberService.addUserToGroup(1L, new AddUserRequest("alice"));

      ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
      verify(memberRepository).save(memberCaptor.capture());

      assertThat(memberCaptor.getValue().getRole()).isEqualTo(MemberRole.MEMBER);
    }
  }

  @Nested
  @DisplayName("removeUserFromGroup")
  class RemoveUserFromGroup {

    @Test
    @DisplayName("clears the settlement cache and publishes member + settlements events")
    void removeUserFromGroup_shouldClearCacheAndPublishEvents_whenMemberRemoved() {
      Member member = Member.builder().id(1L).role(MemberRole.MEMBER).build();
      when(memberRepository.findByGroupIdAndId(1L, 1L)).thenReturn(Optional.of(member));

      memberService.removeUserFromGroup(1L, 1L);

      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(MemberChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }

    @Test
    @DisplayName("throws ConflictException when removing the last owner")
    void removeUserFromGroup_shouldThrowConflictException_whenRemovingTheLastOwner() {
      Member member = Member.builder().id(1L).role(MemberRole.OWNER).build();
      when(memberRepository.findByGroupIdAndId(1L, 1L)).thenReturn(Optional.of(member));
      when(memberRepository.countByGroupIdAndRole(1L, MemberRole.OWNER)).thenReturn(1L);

      assertThatThrownBy(() -> memberService.removeUserFromGroup(1L, 1L))
          .isInstanceOf(ConflictException.class);

      verify(memberRepository, never()).delete(any());
      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("removes an owner when another owner remains in the group")
    void removeUserFromGroup_shouldRemoveOwner_whenAnotherOwnerRemains() {
      Member member = Member.builder().id(1L).role(MemberRole.OWNER).build();
      when(memberRepository.findByGroupIdAndId(1L, 1L)).thenReturn(Optional.of(member));
      when(memberRepository.countByGroupIdAndRole(1L, MemberRole.OWNER)).thenReturn(2L);

      memberService.removeUserFromGroup(1L, 1L);

      verify(memberRepository).delete(member);
    }
  }

  @Nested
  @DisplayName("getUserGroups")
  class GetUserGroups {

    @Test
    @DisplayName("throws ResourceNotFoundException when user does not exist")
    void getUserGroups_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
      when(userRepository.existsById(1L)).thenReturn(false);

      assertThatThrownBy(() -> memberService.getUserGroups(1L))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("returns the groups the user is a member of without an N+1 lookup")
    void getUserGroups_shouldReturnGroups_whenUserIsMember() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Member member = Member.builder().id(1L).group(group).build();

      when(userRepository.existsById(1L)).thenReturn(true);
      when(memberRepository.findByUserId(1L)).thenReturn(List.of(member));

      List<GroupDto> groups = memberService.getUserGroups(1L);

      assertThat(groups).hasSize(1);
      assertThat(groups.get(0).getId()).isEqualTo(1L);
    }
  }
}
