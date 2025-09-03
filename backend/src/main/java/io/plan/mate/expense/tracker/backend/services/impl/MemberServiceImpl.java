package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.db.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.db.dtos.MemberDto;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.entities.Member;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import io.plan.mate.expense.tracker.backend.db.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.MemberRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.BadRequestException;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.payloads.request.AddUserRequest;
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

    Member member = Member.builder().user(user).group(group).joinedAt(LocalDateTime.now()).build();

    member = memberRepository.save(member);

    return modelMapper.map(member, MemberDto.class);
  }

  @Override
  @Transactional
  public void removeUserFromGroup(final Long groupId, final Long memberId) {

    final Member member =
        memberRepository
            .findByGroupIdAndId(groupId, memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));

    memberRepository.delete(member);
  }

  @Override
  @Transactional
  public List<MemberDto> getGroupMembers(final Long groupId) {

    final List<Member> groupMembers = memberRepository.findByGroupId(groupId);

    return groupMembers.stream().map(member -> modelMapper.map(member, MemberDto.class)).toList();
  }

  @Override
  @Transactional
  public List<GroupDto> getUserGroups(final Long userId) {

    final List<Member> userMemberships =
        userRepository
            .findById(userId)
            .map(User::getMemberships)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found"));

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
