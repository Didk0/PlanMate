package io.plan.mate.expense.tracker.backend.group.service.mapper;

import io.plan.mate.expense.tracker.backend.commons.service.mapper.PlanMateMapperConfig;
import io.plan.mate.expense.tracker.backend.group.controller.payload.request.CreateGroupRequest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlanMateMapperConfig.class)
public interface GroupMapper {

  GroupDto toDto(Group group);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "members", ignore = true)
  @Mapping(target = "expenses", ignore = true)
  @Mapping(target = "settlements", ignore = true)
  Group toEntity(CreateGroupRequest createGroupRequest);
}
