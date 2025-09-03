package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.db.dtos.ExpenseDto;
import io.plan.mate.expense.tracker.backend.db.entities.Expense;
import io.plan.mate.expense.tracker.backend.db.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import io.plan.mate.expense.tracker.backend.db.repositories.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.services.ExpenseService;
import io.plan.mate.expense.tracker.backend.services.SettlementService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final ModelMapper modelMapper;
  private final SettlementService settlementService;

  @Override
  @Transactional
  public ExpenseDto createExpense(
      final Long groupId, final CreateExpenseRequest createExpenseRequest) {

    final Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id " + groupId + " not found"));

    final User paidBy =
        userRepository
            .findByUsername(createExpenseRequest.paidByUserName())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User with name " + createExpenseRequest.paidByUserName() + " not found"));

    final Expense expense =
        Expense.builder()
            .description(createExpenseRequest.description())
            .amount(createExpenseRequest.amount())
            .createdAt(LocalDateTime.now())
            .group(group)
            .paidBy(paidBy)
            .build();

    final List<ExpenseParticipant> participants =
        createExpenseRequest.participants().stream()
            .map(
                p -> {
                  final User userParticipant =
                      userRepository
                          .findByUsername(p.userName())
                          .orElseThrow(
                              () ->
                                  new ResourceNotFoundException(
                                      "User with username " + p.userName() + " not found"));

                  return ExpenseParticipant.builder()
                      .expense(expense)
                      .participant(userParticipant)
                      .shareAmount(p.shareAmount())
                      .build();
                })
            .toList();

    expense.setParticipants(participants);

    final Expense savedExpense = expenseRepository.save(expense);

    final ExpenseDto expenseDto = modelMapper.map(savedExpense, ExpenseDto.class);

    settlementService.clearSettlementCache(group.getId());

    return expenseDto;
  }

  @Override
  @Transactional
  public List<ExpenseDto> getGroupExpenses(final Long groupId) {

    final List<Expense> expenses =
        expenseRepository
            .findByGroupId(groupId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Expense for group with id " + groupId + " not found"));

    return expenses.stream().map(expense -> modelMapper.map(expense, ExpenseDto.class)).toList();
  }
}
