package io.plan.mate.expense.tracker.backend.commons.utils;

import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SettlementTestBuilders {

  private SettlementTestBuilders() {}

  public static User user(long id, String first, String last) {
    return User.builder()
        .id(id)
        .keycloakId(UUID.randomUUID())
        .username(first.toLowerCase())
        .email(first.toLowerCase() + "@test.com")
        .firstName(first)
        .lastName(last)
        .build();
  }

  public static User newUser(String first, String last) {
    return User.builder()
        .keycloakId(UUID.randomUUID())
        .username(first.toLowerCase() + UUID.randomUUID().toString().substring(0, 6))
        .email(UUID.randomUUID() + "@test.com")
        .firstName(first)
        .lastName(last)
        .build();
  }

  public static ExpenseParticipant participant(User user, String amount) {
    return ExpenseParticipant.builder()
        .participant(user)
        .shareAmount(new BigDecimal(amount))
        .build();
  }

  public static Expense expense(long id, Group group, User paidBy, ExpenseParticipant... parts) {
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

  public static Group groupWithExpenses(long id, Expense... expenses) {
    return Group.builder()
        .id(id)
        .name("Test Group")
        .expenses(new ArrayList<>(List.of(expenses)))
        .build();
  }
}
