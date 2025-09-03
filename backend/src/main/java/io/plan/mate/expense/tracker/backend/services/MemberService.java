package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.db.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.db.dtos.MemberDto;
import io.plan.mate.expense.tracker.backend.payloads.request.AddUserRequest;

import java.util.List;

public interface MemberService {

  MemberDto addUserToGroup(Long groupId, AddUserRequest addUserRequest);

  void removeUserFromGroup(Long groupId, Long memberId);

  List<MemberDto> getGroupMembers(Long groupId);

  List<GroupDto> getUserGroups(Long userId);
}
