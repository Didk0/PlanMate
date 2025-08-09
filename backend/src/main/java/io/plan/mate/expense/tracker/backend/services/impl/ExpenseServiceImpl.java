package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.entities.Expense;
import io.plan.mate.expense.tracker.backend.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.entities.Group;
import io.plan.mate.expense.tracker.backend.entities.User;
import io.plan.mate.expense.tracker.backend.payloads.dtos.ExpenseDto;
import io.plan.mate.expense.tracker.backend.payloads.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.repositories.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.services.ExpenseService;
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

  @Override
  public ExpenseDto createExpense(final CreateExpenseRequest createExpenseRequest) {

    final Group group =
        groupRepository
            .findById(createExpenseRequest.groupId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Group with id " + createExpenseRequest.groupId() + " does not exist"));

    final User paidBy =
        userRepository
            .findById(createExpenseRequest.paidByUserId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "User with id " + createExpenseRequest.paidByUserId() + " does not exist"));

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
            .map(p -> modelMapper.map(p, ExpenseParticipant.class))
            .map(
                p -> {
                  final User userParticipant =
                      userRepository
                          .findById(p.getParticipant().getId())
                          .orElseThrow(
                              () ->
                                  new IllegalArgumentException(
                                      "No user with id " + p.getParticipant().getId() + " exists"));
                  return ExpenseParticipant.builder()
                      .expense(expense)
                      .participant(userParticipant)
                      .shareAmount(p.getShareAmount())
                      .build();
                })
            .toList();

    expense.setParticipants(participants);

    expenseRepository.save(expense);

    return modelMapper.map(expense, ExpenseDto.class);
  }

  @Override
  @Transactional
  public List<ExpenseDto> getGroupExpenses(final Long groupId) {

    final List<Expense> expenses =
        expenseRepository
            .findByGroupId(groupId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Expense for group with id " + groupId + " not found"));

    return expenses.stream().map(expense -> modelMapper.map(expense, ExpenseDto.class)).toList();
  }
}
