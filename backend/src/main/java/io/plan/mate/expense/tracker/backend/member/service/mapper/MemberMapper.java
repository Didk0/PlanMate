package io.plan.mate.expense.tracker.backend.member.service.mapper;

import io.plan.mate.expense.tracker.backend.commons.service.mapper.PlanMateMapperConfig;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.service.dto.MemberDto;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlanMateMapperConfig.class)
public interface MemberMapper {

  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "firstName", source = "user.firstName")
  @Mapping(target = "lastName", source = "user.lastName")
  @Mapping(target = "groupName", source = "group.name")
  MemberDto toDto(Member member);

  List<MemberDto> toDtoList(List<Member> members);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "joinedAt", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "group", source = "group")
  @Mapping(target = "role", source = "role")
  Member toEntity(User user, Group group, MemberRole role);
}
