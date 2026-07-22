package io.plan.mate.expense.tracker.backend.member.service.impl;

import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ConflictException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangeEnum;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.controller.payload.request.AddUserRequest;
import io.plan.mate.expense.tracker.backend.member.service.MemberService;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangeEnum;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final GroupRepository groupRepository;
  private final ModelMapper modelMapper;
  private final SettlementService settlementService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public MemberDto addUserToGroup(final Long groupId, final AddUserRequest addUserRequest) {

    final User user =
        userRepository
            .findByUsername(addUserRequest.username())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No user with name " + addUserRequest.username() + " exists"));

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("No group with id " + groupId + " exists"));

    if (memberRepository.findByGroupIdAndUserId(groupId, user.getId()).isPresent()) {
      throw new BadRequestException(
          "User with id " + user.getId() + " is already a member in group with id " + groupId);
    }

    Member member =
        Member.builder()
            .user(user)
            .group(group)
            .role(MemberRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build();

    member = memberRepository.save(member);

    settlementService.clearSettlementCache(groupId);

    final MemberDto memberDto = modelMapper.map(member, MemberDto.class);

    eventPublisher.publishEvent(
        new MemberChangedEvent(MemberChangeEnum.ADD_MEMBER, groupId, memberDto));
    eventPublisher.publishEvent(
        new SettlementsChangedEvent(SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, groupId));

    return memberDto;
  }

  @Override
  @Transactional
  public void removeUserFromGroup(final Long groupId, final Long memberId) {

    final Member member =
        memberRepository
            .findByGroupIdAndId(groupId, memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

    final boolean isRemovingLastOwner =
        member.getRole() == MemberRole.OWNER
            && memberRepository.countByGroupIdAndRole(groupId, MemberRole.OWNER) == 1L;

    if (isRemovingLastOwner) {
      throw new ConflictException(
          "Group with id " + groupId + " must keep at least one owner");
    }

    memberRepository.delete(member);

    settlementService.clearSettlementCache(groupId);

    eventPublisher.publishEvent(
        new MemberChangedEvent(
            MemberChangeEnum.REMOVE_MEMBER, groupId, MemberDto.builder().id(memberId).build()));
    eventPublisher.publishEvent(
        new SettlementsChangedEvent(SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, groupId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<MemberDto> getGroupMembers(final Long groupId) {

    final List<Member> groupMembers = memberRepository.findByGroupId(groupId);

    return groupMembers.stream().map(member -> modelMapper.map(member, MemberDto.class)).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<GroupDto> getUserGroups(final Long userId) {

    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }

    return memberRepository.findByUserId(userId).stream()
        .map(member -> modelMapper.map(member.getGroup(), GroupDto.class))
        .toList();
  }
}
