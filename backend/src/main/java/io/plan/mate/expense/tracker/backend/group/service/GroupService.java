package io.plan.mate.expense.tracker.backend.group.service;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;

import org.springframework.data.domain.Pageable;

public interface GroupService {

    GroupDto createGroup(CreateGroupRequest createGroupRequest);

    GroupDto getGroupById(Long id);

    PagedResponse<GroupDto> getAllGroups(Pageable pageable);

    void deleteGroup(Long id);
}
