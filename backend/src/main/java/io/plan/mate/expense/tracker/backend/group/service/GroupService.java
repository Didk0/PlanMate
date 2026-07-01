package io.plan.mate.expense.tracker.backend.group.service;

import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;

import java.util.List;

public interface GroupService {

    GroupDto createGroup(CreateGroupRequest createGroupRequest);

    GroupDto getGroupById(Long id);

    List<GroupDto> getAllGroups();

    GroupDto deleteGroup(Long id);
}
