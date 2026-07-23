package io.plan.mate.expense.tracker.backend.group.service.impl;

import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.commons.security.CurrentUserService;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.group.service.GroupService;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangeEnum;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangeEnum;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;
  private final MemberRepository memberRepository;
  private final UserRepository userRepository;
  private final ModelMapper modelMapper;
  private final SettlementService settlementService;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public GroupDto createGroup(final CreateGroupRequest createGroupRequest) {

    final Long creatorId = currentUserService.requireCurrentUserId();

    final Group group =
        Group.builder()
            .name(createGroupRequest.name())
            .description(createGroupRequest.description())
            .build();

    final Group createdGroup = groupRepository.save(group);

    final Member owner =
        Member.builder()
            .user(userRepository.getReferenceById(creatorId))
            .group(createdGroup)
            .role(MemberRole.OWNER)
            .joinedAt(LocalDateTime.now())
            .build();

    final Member savedOwner = memberRepository.save(owner);

    settlementService.clearSettlementCache(createdGroup.getId());

    final GroupDto groupDto = modelMapper.map(createdGroup, GroupDto.class);

    eventPublisher.publishEvent(
        new MemberChangedEvent(
            MemberChangeEnum.ADD_MEMBER,
            createdGroup.getId(),
            modelMapper.map(savedOwner, MemberDto.class)));
    eventPublisher.publishEvent(
        new SettlementsChangedEvent(
            SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, createdGroup.getId()));

    return groupDto;
  }

  @Override
  @Transactional(readOnly = true)
  public GroupDto getGroupById(final Long groupId) {

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id=" + groupId + " not found"));

    return modelMapper.map(group, GroupDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GroupDto> getAllGroups() {

    if (currentUserService.isAdmin()) {
      return groupRepository.findAll().stream()
          .map(group -> modelMapper.map(group, GroupDto.class))
          .toList();
    }

    return currentUserService
        .findCurrentUserId()
        .map(
            userId ->
                memberRepository.findByUserId(userId).stream()
                    .map(member -> modelMapper.map(member.getGroup(), GroupDto.class))
                    .toList())
        .orElseGet(List::of);
  }

  @Override
  @Transactional
  public void deleteGroup(final Long groupId) {

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id=" + groupId + " not found"));

    settlementService.clearSettlementCache(groupId);

    groupRepository.delete(group);

    eventPublisher.publishEvent(
        new SettlementsChangedEvent(SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, groupId));
  }
}
