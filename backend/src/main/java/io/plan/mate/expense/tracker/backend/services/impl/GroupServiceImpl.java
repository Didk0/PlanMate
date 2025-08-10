package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.db.dtos.GroupDto;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.services.GroupService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

  private final GroupRepository groupRepository;
  private final ModelMapper modelMapper;

  @Override
  public GroupDto createGroup(final CreateGroupRequest createGroupRequest) {

    final Group group = Group.builder().name(createGroupRequest.name()).build();

    final Group createdGroup = groupRepository.save(group);

    return modelMapper.map(createdGroup, GroupDto.class);
  }

  @Override
  public GroupDto getGroupById(final Long groupId) {

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id=" + groupId + " not found"));

    return modelMapper.map(group, GroupDto.class);
  }

  @Override
  public List<GroupDto> getAllGroups() {

    return groupRepository.findAll().stream()
        .map(group -> modelMapper.map(group, GroupDto.class))
        .toList();
  }

  @Override
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
