package io.plan.mate.expense.tracker.backend.expense.service.impl;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.util.LikePatternEscaper;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseChangeEnum;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseChangedEvent;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.UpdateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.service.ExpenseService;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangeEnum;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
  private final MemberRepository memberRepository;
  private final ModelMapper modelMapper;
  private final SettlementService settlementService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public ExpenseDto createExpense(
      final Long groupId, final CreateExpenseRequest createExpenseRequest) {

    final Group group = findGroupOrThrow(groupId);

    checkValidShareTotal(
        createExpenseRequest.participants(), createExpenseRequest.amount());

    final Map<String, User> usersByUsername =
        resolveMembersByUsername(
            groupId,
            collectUsernames(
                createExpenseRequest.paidByUsername(), createExpenseRequest.participants()));

    final Expense expense =
        Expense.builder()
            .description(createExpenseRequest.description())
            .amount(createExpenseRequest.amount())
            .createdAt(LocalDateTime.now())
            .group(group)
            .paidBy(usersByUsername.get(createExpenseRequest.paidByUsername()))
            .build();

    expense.setParticipants(
        buildParticipants(expense, createExpenseRequest.participants(), usersByUsername));

    final Expense savedExpense = expenseRepository.save(expense);
    final ExpenseDto expenseDto = modelMapper.map(savedExpense, ExpenseDto.class);

    settlementService.clearSettlementCache(group.getId());
    eventPublisher.publishEvent(
        new ExpenseChangedEvent(ExpenseChangeEnum.ADD_EXPENSE, group.getId(), expenseDto));
    eventPublisher.publishEvent(
        new SettlementsChangedEvent(
            SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, group.getId()));

    return expenseDto;
  }

  @Override
  @Transactional
  public ExpenseDto updateExpense(
      final Long groupId, final Long expenseId, final UpdateExpenseRequest updateExpenseRequest) {

    final Expense expense = findExpenseInGroupOrThrow(groupId, expenseId);

    checkValidShareTotal(
        updateExpenseRequest.participants(), updateExpenseRequest.amount());

    final Map<String, User> usersByUsername =
        resolveMembersByUsername(
            groupId,
            collectUsernames(
                updateExpenseRequest.paidByUsername(), updateExpenseRequest.participants()));

    expense.setDescription(updateExpenseRequest.description());
    expense.setAmount(updateExpenseRequest.amount());
    expense.setPaidBy(usersByUsername.get(updateExpenseRequest.paidByUsername()));

    expense.getParticipants().clear();

    expenseRepository.flush();

    expense
        .getParticipants()
        .addAll(buildParticipants(expense, updateExpenseRequest.participants(), usersByUsername));

    final Expense savedExpense = expenseRepository.save(expense);
    final ExpenseDto expenseDto = modelMapper.map(savedExpense, ExpenseDto.class);

    settlementService.clearSettlementCache(groupId);
    eventPublisher.publishEvent(
        new ExpenseChangedEvent(ExpenseChangeEnum.EDIT_EXPENSE, groupId, expenseDto));
    eventPublisher.publishEvent(
        new SettlementsChangedEvent(SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, groupId));

    return expenseDto;
  }

  @Override
  @Transactional
  public void deleteExpense(final Long groupId, final Long expenseId) {

    final Expense expense = findExpenseInGroupOrThrow(groupId, expenseId);
    final ExpenseDto expenseDto = modelMapper.map(expense, ExpenseDto.class);

    expenseRepository.delete(expense);

    settlementService.clearSettlementCache(groupId);
    eventPublisher.publishEvent(
        new ExpenseChangedEvent(ExpenseChangeEnum.DELETE_EXPENSE, groupId, expenseDto));
    eventPublisher.publishEvent(
        new SettlementsChangedEvent(SettlementsChangeEnum.SETTLEMENTS_INVALIDATED, groupId));
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<ExpenseDto> getGroupExpenses(
      final Long groupId, final String search, final Pageable pageable) {

    final String normalizedSearch =
        StringUtils.hasText(search) ? LikePatternEscaper.escape(search.trim()) : null;

    final Sort sortById = pageable.getSort().and(Sort.by(Sort.Direction.ASC, "id"));
    final Pageable pageableSortedById =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortById);

    final Page<Long> expenseIdPage =
        expenseRepository.findExpenseIdsByGroupId(
            groupId, normalizedSearch, pageableSortedById);

    final Map<Long, Expense> expensesById =
        expenseRepository.findByIdIn(expenseIdPage.getContent()).stream()
            .collect(Collectors.toMap(Expense::getId, Function.identity()));

    final List<ExpenseDto> expenseDtos =
        expenseIdPage.getContent().stream()
            .map(expensesById::get)
            .map(expense -> modelMapper.map(expense, ExpenseDto.class))
            .toList();

    return new PagedResponse<>(
        expenseDtos,
        expenseIdPage.getNumber(),
        expenseIdPage.getSize(),
        expenseIdPage.getTotalElements(),
        expenseIdPage.getTotalPages(),
        expenseIdPage.isFirst(),
        expenseIdPage.isLast());
  }

  private Group findGroupOrThrow(final Long groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Group with id " + groupId + " not found"));
  }

  private Expense findExpenseInGroupOrThrow(final Long groupId, final Long expenseId) {

    final Expense expense =
        expenseRepository
            .findById(expenseId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Expense with id " + expenseId + " not found"));

    final boolean belongsToGroup = expense.getGroup().getId().equals(groupId);
    if (!belongsToGroup) {
      throw new ResourceNotFoundException(
          "Expense with id " + expenseId + " not found in group " + groupId);
    }

    return expense;
  }

  private Set<String> collectUsernames(
      final String paidByUsername, final List<CreateExpenseParticipant> participants) {

    final Set<String> usernames = new LinkedHashSet<>(); // order: payer first, then participants
    usernames.add(paidByUsername);
    participants.forEach(p -> usernames.add(p.userName()));
    return usernames;
  }

  private Map<String, User> resolveMembersByUsername(
      final Long groupId, final Set<String> usernames) {

    final Map<String, User> usersByUsername =
        userRepository.findByUsernameIn(usernames).stream()
            .collect(Collectors.toMap(User::getUsername, Function.identity()));

    final Set<String> missingUsernames = new LinkedHashSet<>(usernames);
    missingUsernames.removeAll(usersByUsername.keySet());
    if (!missingUsernames.isEmpty()) {
      throw new ResourceNotFoundException("Users not found: " + missingUsernames);
    }

    final Set<Long> userIds =
        usersByUsername.values().stream().map(User::getId).collect(Collectors.toSet());
    final Set<Long> memberUserIds =
        memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(groupId, userIds);

    final Set<String> nonMemberUsernames =
        usersByUsername.entrySet().stream()
            .filter(entry -> !memberUserIds.contains(entry.getValue().getId()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    if (!nonMemberUsernames.isEmpty()) {
      throw new BadRequestException(
          "Users are not members of group with id " + groupId + ": " + nonMemberUsernames);
    }

    return usersByUsername;
  }

  private List<ExpenseParticipant> buildParticipants(
      final Expense expense,
      final List<CreateExpenseParticipant> participants,
      final Map<String, User> usersByUsername) {

    return participants.stream()
        .map(
            p ->
                ExpenseParticipant.builder()
                    .expense(expense)
                    .participant(usersByUsername.get(p.userName()))
                    .shareAmount(p.shareAmount())
                    .build())
        .toList();
  }

  private void checkValidShareTotal(
      final List<CreateExpenseParticipant> participants, final BigDecimal amount) {

    final BigDecimal shareTotal =
        participants.stream()
            .map(CreateExpenseParticipant::shareAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (shareTotal.compareTo(amount) != 0) {
      throw new BadRequestException(
          "Sum of participant shares (" + shareTotal + ") must equal the expense amount (" + amount + ")");
    }
  }
}
