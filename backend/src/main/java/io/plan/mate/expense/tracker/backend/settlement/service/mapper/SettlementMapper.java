package io.plan.mate.expense.tracker.backend.settlement.service.mapper;

import io.plan.mate.expense.tracker.backend.commons.service.mapper.PlanMateMapperConfig;
import io.plan.mate.expense.tracker.backend.settlement.jpa.entity.Settlement;
import io.plan.mate.expense.tracker.backend.settlement.service.dto.SettlementDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlanMateMapperConfig.class)
public interface SettlementMapper {

  @Mapping(target = "fromUserFirstName", source = "fromUser.firstName")
  @Mapping(target = "fromUserLastName", source = "fromUser.lastName")
  @Mapping(target = "toUserFirstName", source = "toUser.firstName")
  @Mapping(target = "toUserLastName", source = "toUser.lastName")
  SettlementDto toDto(Settlement settlement);

  List<SettlementDto> toDtoList(List<Settlement> settlements);
}
