package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.entities.Group;
import io.plan.mate.expense.tracker.backend.entities.Member;
import io.plan.mate.expense.tracker.backend.entities.User;
import io.plan.mate.expense.tracker.backend.payloads.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.payloads.dtos.MemberDto;
import io.plan.mate.expense.tracker.backend.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.repositories.MemberRepository;
import io.plan.mate.expense.tracker.backend.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.services.MemberService;
import jakarta.transaction.Transactional;
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

  @Override
  public MemberDto addUserToGroup(final Long groupId, final Long userId) {

    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new IllegalArgumentException("No user with id " + userId + " exists"));

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new IllegalArgumentException("No group with id " + groupId + " exists"));

    if (memberRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
      throw new IllegalArgumentException(
          "User with id " + userId + " is already a member in group with id " + groupId);
    }

    Member member = Member.builder().user(user).group(group).joinedAt(LocalDateTime.now()).build();

    member = memberRepository.save(member);

    return modelMapper.map(member, MemberDto.class);
  }

  @Override
  @Transactional
  public MemberDto removeUserFromGroup(final Long groupId, final Long userId) {

    final Member member =
        memberRepository
            .findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Membership not found"));

    final MemberDto memberDto = modelMapper.map(member, MemberDto.class);

    memberRepository.delete(member);

    return memberDto;
  }

  @Override
  public List<MemberDto> getGroupMembers(final Long groupId) {

    final List<Member> groupMembers = memberRepository.findByGroupId(groupId);

    return groupMembers.stream().map(member -> modelMapper.map(member, MemberDto.class)).toList();
  }

  @Override
  public List<GroupDto> getUserGroups(final Long userId) {

    final List<Member> userMemberships = memberRepository.findByUserId(userId);

    return userMemberships.stream()
        .map(
            member ->
                GroupDto.builder()
                    .id(member.getGroup().getId())
                    .name(member.getGroup().getName())
                    .createdAt(member.getGroup().getCreatedAt())
                    .build())
        .toList();
  }
}
