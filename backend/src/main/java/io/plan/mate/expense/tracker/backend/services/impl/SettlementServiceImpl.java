package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.db.dtos.SettlementDto;
import io.plan.mate.expense.tracker.backend.db.entities.Expense;
import io.plan.mate.expense.tracker.backend.db.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.entities.Settlement;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import io.plan.mate.expense.tracker.backend.db.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.SettlementRepository;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.services.SettlementService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

  private final SettlementRepository settlementRepository;
  private final GroupRepository groupRepository;
  private final ModelMapper modelMapper;

  @Override
  @CacheEvict(value = "settlements", key = "#groupId")
  public void clearSettlementCache(final Long groupId) {
    // This method invalidates the settlements cache when called
  }

  @Override
  @Cacheable(value = "settlements", key = "#groupId")
  @Transactional
  public List<SettlementDto> calculateSettlements(final Long groupId) {

    final List<Expense> expenses =
        groupRepository
            .findById(groupId)
            .map(Group::getExpenses)
            .orElseThrow(
                () -> new ResourceNotFoundException("Group with id " + groupId + " not found"));

    if (expenses.isEmpty()) {
      return Collections.emptyList();
    }

    // Calculate net balance per user (net balance = amount paid - amount owed)
    final Map<Long, BigDecimal> userIdToNetBalanceMap = new HashMap<>();

    // Get the User entities
    final Map<Long, User> userCache = new HashMap<>();

    for (final Expense expense : expenses) {

      final Long payerId = expense.getPaidBy().getId();

      userCache.putIfAbsent(payerId, expense.getPaidBy());

      BigDecimal payerNetBalance = BigDecimal.ZERO;

      for (final ExpenseParticipant expenseParticipant : expense.getParticipants()) {

        final Long userId = expenseParticipant.getParticipant().getId();

        userIdToNetBalanceMap.put(
            userId,
            userIdToNetBalanceMap
                .getOrDefault(userId, BigDecimal.ZERO)
                .subtract(expenseParticipant.getShareAmount()));

        payerNetBalance = payerNetBalance.add(expenseParticipant.getShareAmount());

        userCache.putIfAbsent(userId, expenseParticipant.getParticipant());
      }

      userIdToNetBalanceMap.put(
          payerId,
          userIdToNetBalanceMap.getOrDefault(payerId, BigDecimal.ZERO).add(payerNetBalance));
    }

    final PriorityQueue<UserBalance> creditors =
        new PriorityQueue<>(
            Comparator.comparing((UserBalance userBalance) -> userBalance.amount)
                .reversed()); // they are owed money
    final PriorityQueue<UserBalance> debtors =
        new PriorityQueue<>(
            Comparator.comparing((UserBalance userBalance) -> userBalance.amount)
                .reversed()); // they owe money

    populateCreditorsAndDebtors(userIdToNetBalanceMap, creditors, userCache, debtors);

    // delete all old settlements
    settlementRepository.deleteByGroupId(groupId);

    final Group group = expenses.get(0).getGroup();

    // match creditors and debtors
    final List<Settlement> settlements = calculateSettlements(creditors, debtors, group);

    settlementRepository.saveAll(settlements);

    return settlements.stream()
        .map(settlement -> modelMapper.map(settlement, SettlementDto.class))
        .toList();
  }

  private void populateCreditorsAndDebtors(
      final Map<Long, BigDecimal> userIdToNetBalanceMap,
      final PriorityQueue<UserBalance> creditors,
      final Map<Long, User> userCache,
      final PriorityQueue<UserBalance> debtors) {

    for (final Map.Entry<Long, BigDecimal> userIdToNetBalance : userIdToNetBalanceMap.entrySet()) {

      final BigDecimal balance = userIdToNetBalance.getValue();
      final Long userId = userIdToNetBalance.getKey();

      if (balance.compareTo(BigDecimal.ZERO) > 0) {
        creditors.add(new UserBalance(userCache.get(userId), balance));
      } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
        debtors.add(new UserBalance(userCache.get(userId), balance.abs()));
      }
    }
  }

  private List<Settlement> calculateSettlements(
      final PriorityQueue<UserBalance> creditors,
      final PriorityQueue<UserBalance> debtors,
      final Group group) {

    final List<Settlement> settlements = new ArrayList<>();

    while (!creditors.isEmpty() && !debtors.isEmpty()) {
      final UserBalance creditor = creditors.poll();
      final UserBalance debtor = debtors.poll();

      final BigDecimal minAmount = creditor.amount.min(debtor.amount);

      settlements.add(
          Settlement.builder()
              .group(group)
              .fromUser(debtor.user)
              .toUser(creditor.user)
              .amount(minAmount)
              .settledAt(LocalDateTime.now())
              .build());

      final BigDecimal creditorRemaining = creditor.amount.subtract(minAmount);
      final BigDecimal debtorRemaining = debtor.amount.subtract(minAmount);

      if (creditorRemaining.compareTo(BigDecimal.ZERO) > 0) {
        creditors.add(new UserBalance(creditor.user, creditorRemaining));
      }
      if (debtorRemaining.compareTo(BigDecimal.ZERO) > 0) {
        debtors.add(new UserBalance(debtor.user, debtorRemaining));
      }
    }
    return settlements;
  }

  @AllArgsConstructor
  private static class UserBalance {
    User user;
    BigDecimal amount;
  }
}
