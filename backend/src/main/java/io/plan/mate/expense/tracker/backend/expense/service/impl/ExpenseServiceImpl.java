package io.plan.mate.expense.tracker.backend.expense.service.impl;

import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.service.ExpenseService;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    checkValidShareTotal(createExpenseRequest);

    final Set<String> usernames = new LinkedHashSet<>();
    usernames.add(createExpenseRequest.paidByUsername());
    createExpenseRequest.participants().forEach(p -> usernames.add(p.userName()));

    final Map<String, User> usersByUsername =
        userRepository.findByUsernameIn(usernames).stream()
            .collect(Collectors.toMap(User::getUsername, Function.identity()));

    final Set<String> missingUsernames = new LinkedHashSet<>(usernames); //order: payer first, then participants
    missingUsernames.removeAll(usersByUsername.keySet());
    if (!missingUsernames.isEmpty()) {
      throw new ResourceNotFoundException("Users not found: " + missingUsernames);
    }

    final Expense expense =
        Expense.builder()
            .description(createExpenseRequest.description())
            .amount(createExpenseRequest.amount())
            .createdAt(LocalDateTime.now())
            .group(group)
            .paidBy(usersByUsername.get(createExpenseRequest.paidByUsername()))
            .build();

    final List<ExpenseParticipant> participants =
        createExpenseRequest.participants().stream()
            .map(
                p ->
                    ExpenseParticipant.builder()
                        .expense(expense)
                        .participant(usersByUsername.get(p.userName()))
                        .shareAmount(p.shareAmount())
                        .build())
            .toList();

    expense.setParticipants(participants);

    final Expense savedExpense = expenseRepository.save(expense);

    final ExpenseDto expenseDto = modelMapper.map(savedExpense, ExpenseDto.class);

    settlementService.clearSettlementCache(group.getId());

    return expenseDto;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ExpenseDto> getGroupExpenses(final Long groupId) {

    final List<Expense> expenses = expenseRepository.findByGroupId(groupId);

    return expenses.stream().map(expense -> modelMapper.map(expense, ExpenseDto.class)).toList();
  }

  private void checkValidShareTotal(final CreateExpenseRequest createExpenseRequest) {
    final BigDecimal shareTotal =
        createExpenseRequest.participants().stream()
            .map(CreateExpenseParticipant::shareAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (shareTotal.compareTo(createExpenseRequest.amount()) != 0) {
      throw new BadRequestException(
          "Sum of participant shares ("
              + shareTotal
              + ") must equal the expense amount ("
              + createExpenseRequest.amount()
              + ")");
    }
  }
}
