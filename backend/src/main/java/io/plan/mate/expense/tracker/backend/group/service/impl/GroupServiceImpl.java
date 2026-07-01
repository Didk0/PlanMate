package io.plan.mate.expense.tracker.backend.group.service.impl;

import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.group.service.GroupService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;
  private final ModelMapper modelMapper;

  @Override
  @Transactional
  public GroupDto createGroup(final CreateGroupRequest createGroupRequest) {

    final Group group =
        Group.builder()
            .name(createGroupRequest.name())
            .description(createGroupRequest.description())
            .build();

    final Group createdGroup = groupRepository.save(group);

    return modelMapper.map(createdGroup, GroupDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public GroupDto getGroupById(final Long groupId) {

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id=" + groupId + " not found"));

    return modelMapper.map(group, GroupDto.class);
  }

  @Override
  @Transactional(readOnly = true)
  public List<GroupDto> getAllGroups() {

    return groupRepository.findAll().stream()
        .map(group -> modelMapper.map(group, GroupDto.class))
        .toList();
  }

  @Override
  @Transactional
  public GroupDto deleteGroup(final Long groupId) {

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id=" + groupId + " not found"));

    final GroupDto groupDtoToReturn = modelMapper.map(group, GroupDto.class);

    groupRepository.delete(group);

    return groupDtoToReturn;
  }
}
