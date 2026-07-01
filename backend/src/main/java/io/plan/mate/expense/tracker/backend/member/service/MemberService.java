package io.plan.mate.expense.tracker.backend.member.service;

import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
import io.plan.mate.expense.tracker.backend.member.controller.payload.request.AddUserRequest;

import java.util.List;

public interface MemberService {

  MemberDto addUserToGroup(Long groupId, AddUserRequest addUserRequest);

  void removeUserFromGroup(Long groupId, Long memberId);

  List<MemberDto> getGroupMembers(Long groupId);

  List<GroupDto> getUserGroups(Long userId);
}
