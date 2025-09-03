package io.plan.mate.expense.tracker.backend.configs.converters;

import io.plan.mate.expense.tracker.backend.db.dtos.MemberDto;
import io.plan.mate.expense.tracker.backend.db.entities.Member;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class MemberToMemberDtoConverter implements Converter<Member, MemberDto> {

  @Override
  public MemberDto convert(final MappingContext<Member, MemberDto> mappingContext) {

    final Member member = mappingContext.getSource();

    if (member == null) {
      return null;
    }

    return MemberDto.builder()
        .id(member.getId())
        .username(member.getUser().getUsername())
        .firstName(member.getUser().getFirstName())
        .lastName(member.getUser().getLastName())
        .groupName(member.getGroup().getName())
        .joinedAt(member.getJoinedAt())
        .build();
  }
}
