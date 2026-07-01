package io.plan.mate.expense.tracker.backend.services.impl;

import static io.plan.mate.expense.tracker.backend.services.impl.SettlementTestBuilders.newUser;
import static io.plan.mate.expense.tracker.backend.services.impl.SettlementTestBuilders.participant;
import static org.assertj.core.api.Assertions.assertThat;

import io.plan.mate.expense.tracker.backend.db.dtos.SettlementDto;
import io.plan.mate.expense.tracker.backend.db.entities.Expense;
import io.plan.mate.expense.tracker.backend.db.entities.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import io.plan.mate.expense.tracker.backend.db.repositories.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.SettlementRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.UserRepository;
import io.plan.mate.expense.tracker.backend.services.SettlementService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("SettlementServiceImpl integration tests")
class SettlementServiceImplIntegrationTest {

  @MockitoBean @SuppressWarnings("unused") private JwtDecoder jwtDecoder;

  @Autowired private SettlementService settlementService;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private ExpenseRepository expenseRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private CacheManager cacheManager;
  @Autowired private EntityManager entityManager;

  @BeforeEach
  void clearSettlementsCache() {
    Objects.requireNonNull(cacheManager.getCache("settlements")).clear();
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private User persistUser(String first, String last) {
    return userRepository.save(newUser(first, last));
  }

  private Group persistGroup(String name) {
    return groupRepository.save(Group.builder().name(name).build());
  }

  private void persistExpense(Group group, User paidBy, List<ExpenseParticipant> parts) {
    BigDecimal total =
        parts.stream()
            .map(ExpenseParticipant::getShareAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    Expense expense = Expense.builder().group(group).paidBy(paidBy).amount(total).build();
    parts.forEach(p -> p.setExpense(expense));
    expense.setParticipants(new ArrayList<>(parts));
    expenseRepository.save(expense);
  }

  @Test
  @DisplayName("single expense: correct debtor, creditor and amount are returned")
  void calculateSettlements_shouldReturnDebtorPaysCreditor_whenSingleExpense() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistExpense(group, alice, List.of(participant(bob, "30.00")));
    flushAndClear();

    List<SettlementDto> result = settlementService.calculateSettlements(group.getId());

    assertThat(result).hasSize(1);
    SettlementDto settlement = result.getFirst();
    assertThat(settlement.getFromUserFirstName()).isEqualTo("Bob");
    assertThat(settlement.getToUserFirstName()).isEqualTo("Alice");
    assertThat(settlement.getAmount()).isEqualByComparingTo("30.00");
  }

  @Test
  @DisplayName("three-person equal split: both debtors owe the creditor their share")
  void calculateSettlements_shouldReturnTwoSettlements_whenThreePersonEqualSplit() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    User charlie = persistUser("Charlie", "Brown");
    Group group = persistGroup("Trip");
    // Alice pays 90, split equally among Alice, Bob, Charlie
    // Net: Alice = +60, Bob = -30, Charlie = -30
    persistExpense(
        group,
        alice,
        List.of(participant(alice, "30.00"), participant(bob, "30.00"), participant(charlie, "30.00")));
    flushAndClear();

    List<SettlementDto> result = settlementService.calculateSettlements(group.getId());

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(settlement -> settlement.getToUserFirstName().equals("Alice"));
    assertThat(result)
        .allMatch(settlement -> settlement.getAmount().compareTo(new BigDecimal("30.00")) == 0);
    assertThat(result.stream().map(SettlementDto::getFromUserFirstName).toList())
        .containsExactlyInAnyOrder("Bob", "Charlie");
  }

  @Test
  @DisplayName("second call reuses persisted settlements (L2 cache) after Spring cache eviction")
  void calculateSettlements_shouldReusePersistedSettlements_whenSpringCacheEvicted() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistExpense(group, alice, List.of(participant(bob, "40.00")));
    flushAndClear();

    // First call: computes, persists to DB, and populates the Spring cache
    settlementService.calculateSettlements(group.getId());

    // Simulate Redis eviction by clearing the Spring cache while leaving DB rows intact
    Objects.requireNonNull(cacheManager.getCache("settlements")).clear();

    // Second call: cache miss → finds persisted rows → returns without recomputing
    List<SettlementDto> result = settlementService.calculateSettlements(group.getId());

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getAmount()).isEqualByComparingTo("40.00");
    // Persisted row count must still be 1 (not doubled by recalculation)
    assertThat(settlementRepository.findByGroupId(group.getId())).hasSize(1);
  }

  @Test
  @DisplayName("clearSettlementCache removes persisted settlements so next call recalculates")
  void clearSettlementCache_shouldTriggerRecalculation_whenNextCallInvoked() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistExpense(group, alice, List.of(participant(bob, "50.00")));
    flushAndClear();

    // Prime both cache layers
    settlementService.calculateSettlements(group.getId());

    // Evict both the Spring cache and the persisted DB rows
    settlementService.clearSettlementCache(group.getId());
    flushAndClear();

    assertThat(settlementRepository.findByGroupId(group.getId())).isEmpty();

    // Next call recalculates from expenses and re-persists
    List<SettlementDto> recalculated = settlementService.calculateSettlements(group.getId());

    assertThat(recalculated).hasSize(1);
    assertThat(recalculated.getFirst().getAmount()).isEqualByComparingTo("50.00");
  }

  @Test
  @DisplayName("expenses that net to zero produce no settlements")
  void calculateSettlements_shouldReturnNoSettlements_whenExpensesNetToZero() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistExpense(group, alice, List.of(participant(bob, "25.00")));
    persistExpense(group, bob, List.of(participant(alice, "25.00")));
    flushAndClear();

    List<SettlementDto> result = settlementService.calculateSettlements(group.getId());

    assertThat(result).isEmpty();
    assertThat(settlementRepository.findByGroupId(group.getId())).isEmpty();
  }

  @Test
  @DisplayName("group with no expenses returns empty settlement list without persisting anything")
  void calculateSettlements_shouldReturnEmptyAndPersistNothing_whenGroupHasNoExpenses() {
    Group group = persistGroup("Empty Group");

    List<SettlementDto> result = settlementService.calculateSettlements(group.getId());

    assertThat(result).isEmpty();
    assertThat(settlementRepository.findByGroupId(group.getId())).isEmpty();
  }
}
