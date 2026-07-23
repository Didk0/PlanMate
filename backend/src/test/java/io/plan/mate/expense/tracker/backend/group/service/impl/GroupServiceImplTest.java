package io.plan.mate.expense.tracker.backend.group.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.commons.security.CurrentUserService;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupServiceImpl")
class GroupServiceImplTest {

  @Mock private GroupRepository groupRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private UserRepository userRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private SettlementService settlementService;
  @Mock private CurrentUserService currentUserService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private GroupServiceImpl groupService;

  @Nested
  @DisplayName("deleteGroup")
  class DeleteGroup {

    @Test
    @DisplayName("throws ResourceNotFoundException when group does not exist")
    void deleteGroup_shouldThrowResourceNotFoundException_whenGroupDoesNotExist() {
      when(groupRepository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.deleteGroup(1L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("1");

      verify(settlementService, never()).clearSettlementCache(any());
      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("clears the settlement cache and publishes a SettlementsChangedEvent when group is deleted")
    void deleteGroup_shouldClearSettlementCacheAndPublishEvent_whenGroupDeleted() {
      Group group = Group.builder().id(1L).name("Trip").build();
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

      groupService.deleteGroup(1L);

      verify(settlementService).clearSettlementCache(1L);
      verify(groupRepository).delete(group);
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }

  @Nested
  @DisplayName("createGroup")
  class CreateGroup {

    @Test
    @DisplayName("persists the creator as an OWNER member")
    void createGroup_shouldPersistCreatorAsOwnerMember_whenGroupIsCreated() {
      User creator = User.builder().id(7L).username("alice").build();
      Group group = Group.builder().id(1L).name("Trip").build();
      Member savedOwner = Member.builder().id(1L).user(creator).group(group).role(MemberRole.OWNER).build();

      when(currentUserService.requireCurrentUserId()).thenReturn(7L);
      when(userRepository.getReferenceById(7L)).thenReturn(creator);
      when(groupRepository.save(any())).thenReturn(group);
      when(memberRepository.save(any())).thenReturn(savedOwner);
      when(modelMapper.map(group, GroupDto.class)).thenReturn(GroupDto.builder().id(1L).build());
      when(modelMapper.map(savedOwner, MemberDto.class)).thenReturn(MemberDto.builder().id(1L).build());

      groupService.createGroup(new CreateGroupRequest("Trip", "desc"));

      ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
      verify(memberRepository).save(memberCaptor.capture());

      assertThat(memberCaptor.getValue().getRole()).isEqualTo(MemberRole.OWNER);
      assertThat(memberCaptor.getValue().getUser()).isEqualTo(creator);
    }

    @Test
    @DisplayName("clears the settlement cache and publishes member + settlements events")
    void createGroup_shouldClearSettlementCacheAndPublishMemberAndSettlementEvents_whenGroupIsCreated() {
      User creator = User.builder().id(7L).username("alice").build();
      Group group = Group.builder().id(1L).name("Trip").build();
      Member savedOwner = Member.builder().id(1L).user(creator).group(group).role(MemberRole.OWNER).build();

      when(currentUserService.requireCurrentUserId()).thenReturn(7L);
      when(userRepository.getReferenceById(7L)).thenReturn(creator);
      when(groupRepository.save(any())).thenReturn(group);
      when(memberRepository.save(any())).thenReturn(savedOwner);
      when(modelMapper.map(group, GroupDto.class)).thenReturn(GroupDto.builder().id(1L).build());
      when(modelMapper.map(savedOwner, MemberDto.class)).thenReturn(MemberDto.builder().id(1L).build());

      groupService.createGroup(new CreateGroupRequest("Trip", "desc"));

      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(MemberChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }

    @Test
    @DisplayName("throws ResourceNotFoundException when the caller is not provisioned")
    void createGroup_shouldThrowResourceNotFoundException_whenCallerIsNotProvisioned() {
      when(currentUserService.requireCurrentUserId())
          .thenThrow(new ResourceNotFoundException("Authenticated user is not provisioned in PlanMate"));

      assertThatThrownBy(() -> groupService.createGroup(new CreateGroupRequest("Trip", "desc")))
          .isInstanceOf(ResourceNotFoundException.class);

      verify(groupRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getAllGroups")
  class GetAllGroups {

    @Test
    @DisplayName("returns every group when the caller is an ADMIN")
    void getAllGroups_shouldReturnEveryGroup_whenCallerIsAdmin() {
      Group group = Group.builder().id(1L).name("Trip").build();
      when(currentUserService.isAdmin()).thenReturn(true);
      when(groupRepository.findAll()).thenReturn(List.of(group));
      when(modelMapper.map(group, GroupDto.class)).thenReturn(GroupDto.builder().id(1L).build());

      List<GroupDto> groups = groupService.getAllGroups();

      assertThat(groups).hasSize(1);
      verify(currentUserService, never()).findCurrentUserId();
    }

    @Test
    @DisplayName("returns only the caller's groups when the caller is not an ADMIN")
    void getAllGroups_shouldReturnCallerGroups_whenCallerIsNotAdmin() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Member member = Member.builder().id(1L).group(group).build();

      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.of(7L));
      when(memberRepository.findByUserId(7L)).thenReturn(List.of(member));
      when(modelMapper.map(group, GroupDto.class)).thenReturn(GroupDto.builder().id(1L).build());

      List<GroupDto> groups = groupService.getAllGroups();

      assertThat(groups).hasSize(1);
      verify(groupRepository, never()).findAll();
    }

    @Test
    @DisplayName("returns an empty list when the caller is not provisioned")
    void getAllGroups_shouldReturnEmptyList_whenCallerIsNotProvisioned() {
      when(currentUserService.isAdmin()).thenReturn(false);
      when(currentUserService.findCurrentUserId()).thenReturn(Optional.empty());

      List<GroupDto> groups = groupService.getAllGroups();

      assertThat(groups).isEmpty();
      verify(memberRepository, never()).findByUserId(any());
    }
  }
}
