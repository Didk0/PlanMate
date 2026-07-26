package io.plan.mate.expense.tracker.backend.expense.service.mapper;

import io.plan.mate.expense.tracker.backend.commons.service.mapper.PlanMateMapperConfig;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.UpdateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseParticipantDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = PlanMateMapperConfig.class)
public interface ExpenseMapper {

  @Mapping(target = "paidByFirstName", source = "paidBy.firstName")
  @Mapping(target = "paidByLastName", source = "paidBy.lastName")
  @Mapping(target = "paidByUsername", source = "paidBy.username")
  ExpenseDto toDto(Expense expense);

  @Mapping(target = "firstName", source = "participant.firstName")
  @Mapping(target = "lastName", source = "participant.lastName")
  @Mapping(target = "username", source = "participant.username")
  ExpenseParticipantDto toDto(ExpenseParticipant expenseParticipant);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "participants", ignore = true)
  @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "description", source = "request.description")
  @Mapping(target = "amount", source = "request.amount")
  @Mapping(target = "group", source = "group")
  @Mapping(target = "paidBy", source = "paidBy")
  Expense toEntity(CreateExpenseRequest request, Group group, User paidBy);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "expense", source = "expense")
  @Mapping(target = "participant", source = "user")
  @Mapping(target = "shareAmount", source = "createExpenseParticipant.shareAmount")
  ExpenseParticipant toParticipantEntity(
      CreateExpenseParticipant createExpenseParticipant, Expense expense, User user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "group", ignore = true)
  @Mapping(target = "participants", ignore = true)
  @Mapping(target = "description", source = "request.description")
  @Mapping(target = "amount", source = "request.amount")
  @Mapping(target = "paidBy", source = "paidBy")
  void updateEntity(@MappingTarget Expense expense, UpdateExpenseRequest request, User paidBy);
}
