package io.plan.mate.expense.tracker.backend.configs.converters;

import io.plan.mate.expense.tracker.backend.db.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.db.dtos.ExpenseParticipantDto;
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

    final ExpenseParticipantDto expenseParticipantDto =
        ExpenseParticipantDto.builder()
            .id(expenseParticipant.getId())
            .shareAmount(expenseParticipant.getShareAmount())
            .build();

    if (expenseParticipant.getParticipant() != null) {
      expenseParticipantDto.setUserId(expenseParticipant.getParticipant().getId());
      expenseParticipantDto.setUserName(expenseParticipant.getParticipant().getName());
    }

    return expenseParticipantDto;
  }
}
