package io.plan.mate.expense.tracker.backend.configs.converters;

import io.plan.mate.expense.tracker.backend.db.dtos.ExpenseParticipantDto;
import io.plan.mate.expense.tracker.backend.db.entities.ExpenseParticipant;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class ExpenseParticipantToDtoConverter
    implements Converter<ExpenseParticipant, ExpenseParticipantDto> {

  @Override
  public ExpenseParticipantDto convert(
      final MappingContext<ExpenseParticipant, ExpenseParticipantDto> context) {

    final ExpenseParticipant expenseParticipant = context.getSource();
    if (expenseParticipant == null) {
      return null;
    }

    return ExpenseParticipantDto.builder()
        .id(expenseParticipant.getId())
        .firstName(expenseParticipant.getParticipant().getFirstName())
        .lastName(expenseParticipant.getParticipant().getLastName())
        .shareAmount(expenseParticipant.getShareAmount())
        .build();
  }
}
