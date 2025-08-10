package io.plan.mate.expense.tracker.backend.services;

import io.plan.mate.expense.tracker.backend.db.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateGroupRequest;

import java.util.List;

public interface GroupService {

    GroupDto createGroup(CreateGroupRequest createGroupRequest);

    GroupDto getGroupById(Long id);

    List<GroupDto> getAllGroups();

    GroupDto deleteGroup(Long id);
}
