package io.plan.mate.expense.tracker.backend.group.service.impl;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.commons.security.CurrentUserService;
import io.plan.mate.expense.tracker.backend.commons.util.LikePatternEscaper;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.group.service.GroupService;
import io.plan.mate.expense.tracker.backend.group.service.mapper.GroupMapper;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangeEnum;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.member.service.mapper.MemberMapper;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangeEnum;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;
  private final MemberRepository memberRepository;
  private final UserRepository userRepository;
  private final GroupMapper groupMapper;
  private final MemberMapper memberMapper;
  private final SettlementService settlementService;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public GroupDto createGroup(final CreateGroupRequest createGroupRequest) {

    final Long creatorId = currentUserService.requireCurrentUserId();

    final Group group = groupMapper.toEntity(createGroupRequest);

    final Group createdGroup = groupRepository.save(group);

    final Member owner =
        memberMapper.toEntity(
            userRepository.getReferenceById(creatorId), createdGroup, MemberRole.OWNER);

    final Member savedOwner = memberRepository.save(owner);

    settlementService.clearSettlementCache(createdGroup.getId());

    final GroupDto groupDto = groupMapper.toDto(createdGroup);

    eventPublisher.publishEvent(
        new MemberChangedEvent(
            MemberChangeEnum.ADD_MEMBER, createdGroup.getId(), memberMapper.toDto(savedOwner)));
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

    return groupMapper.toDto(group);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<GroupDto> getAllGroups(final String search, final Pageable pageable) {

    final String normalizedSearch =
        StringUtils.hasText(search) ? LikePatternEscaper.escape(search.trim()) : null;

    if (currentUserService.isAdmin()) {
      final Page<GroupDto> groupPage =
          groupRepository.search(normalizedSearch, pageable).map(groupMapper::toDto);
      return PagedResponse.from(groupPage);
    }

    final Page<GroupDto> groupPage =
        currentUserService
            .findCurrentUserId()
            .map(
                userId ->
                    groupRepository
                        .searchForMember(userId, normalizedSearch, pageable)
                        .map(groupMapper::toDto))
            .orElseGet(() -> Page.empty(pageable));

    return PagedResponse.from(groupPage);
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
