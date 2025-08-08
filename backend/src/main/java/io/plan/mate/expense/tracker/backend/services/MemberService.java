package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.payloads.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.payloads.dtos.MemberDto;
import java.util.List;

public interface MemberService {

  MemberDto addUserToGroup(Long groupId, Long userId);

  MemberDto removeUserFromGroup(Long groupId, Long userId);

  List<MemberDto> getGroupMembers(Long groupId);

  List<GroupDto> getUserGroups(Long userId);
}
