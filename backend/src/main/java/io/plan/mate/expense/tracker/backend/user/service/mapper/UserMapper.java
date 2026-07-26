package io.plan.mate.expense.tracker.backend.user.service.mapper;

import io.plan.mate.expense.tracker.backend.commons.service.mapper.PlanMateMapperConfig;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = PlanMateMapperConfig.class)
public interface UserMapper {

  UserDto toDto(User user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "keycloakId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "memberships", ignore = true)
  void updateFromKeycloak(
      @MappingTarget User user, UserRepresentation userRepresentation);
}
