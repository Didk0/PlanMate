package io.plan.mate.expense.tracker.backend.services.impl;

import io.plan.mate.expense.tracker.backend.db.entities.Expense;
import io.plan.mate.expense.tracker.backend.db.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class SettlementTestBuilders {

  private SettlementTestBuilders() {}

  static User user(long id, String first, String last) {
    return User.builder()
        .id(id)
        .keycloakId(UUID.randomUUID())
        .username(first.toLowerCase())
        .email(first.toLowerCase() + "@test.com")
        .firstName(first)
        .lastName(last)
        .build();
  }

  static User newUser(String first, String last) {
    return User.builder()
        .keycloakId(UUID.randomUUID())
        .username(first.toLowerCase() + UUID.randomUUID().toString().substring(0, 6))
        .email(UUID.randomUUID() + "@test.com")
        .firstName(first)
        .lastName(last)
        .build();
  }

  static ExpenseParticipant participant(User user, String amount) {
    return ExpenseParticipant.builder()
        .participant(user)
        .shareAmount(new BigDecimal(amount))
        .build();
  }

  static Expense expense(long id, Group group, User paidBy, ExpenseParticipant... parts) {
    BigDecimal total =
        Arrays.stream(parts)
            .map(ExpenseParticipant::getShareAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return Expense.builder()
        .id(id)
        .group(group)
        .paidBy(paidBy)
        .amount(total)
        .participants(new ArrayList<>(List.of(parts)))
        .build();
  }

  static Group groupWithExpenses(long id, Expense... expenses) {
    return Group.builder()
        .id(id)
        .name("Test Group")
        .expenses(new ArrayList<>(List.of(expenses)))
        .build();
  }
}
