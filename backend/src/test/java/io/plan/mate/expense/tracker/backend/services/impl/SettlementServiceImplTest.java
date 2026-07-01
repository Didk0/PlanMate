package io.plan.mate.expense.tracker.backend.services.impl;

import static io.plan.mate.expense.tracker.backend.services.impl.SettlementTestBuilders.expense;
import static io.plan.mate.expense.tracker.backend.services.impl.SettlementTestBuilders.groupWithExpenses;
import static io.plan.mate.expense.tracker.backend.services.impl.SettlementTestBuilders.participant;
import static io.plan.mate.expense.tracker.backend.services.impl.SettlementTestBuilders.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.db.dtos.SettlementDto;
import io.plan.mate.expense.tracker.backend.db.entities.Expense;
import io.plan.mate.expense.tracker.backend.db.entities.Group;
import io.plan.mate.expense.tracker.backend.db.entities.Settlement;
import io.plan.mate.expense.tracker.backend.db.entities.User;
import io.plan.mate.expense.tracker.backend.db.repositories.GroupRepository;
import io.plan.mate.expense.tracker.backend.db.repositories.SettlementRepository;
import io.plan.mate.expense.tracker.backend.exception.handling.exceptions.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementServiceImpl")
class SettlementServiceImplTest {

  @Mock private SettlementRepository settlementRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private ModelMapper modelMapper;

  @InjectMocks private SettlementServiceImpl settlementService;

  @SuppressWarnings("unchecked")
  private ArgumentCaptor<List<Settlement>> settlementListCaptor() {
    return ArgumentCaptor.forClass(List.class);
  }

  @Nested
  @DisplayName("calculateSettlements")
  class CalculateSettlements {

    @Test
    @DisplayName("throws ResourceNotFoundException when group does not exist")
    void calculateSettlements_shouldThrowResourceNotFoundException_whenGroupDoesNotExist() {
      when(settlementRepository.findByGroupId(99L)).thenReturn(List.of());
      when(groupRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> settlementService.calculateSettlements(99L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("99");
    }

    @Test
    @DisplayName("returns empty list when group has no expenses")
    void calculateSettlements_shouldReturnEmptyList_whenGroupHasNoExpenses() {
      Group group = groupWithExpenses(1L);
      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

      List<SettlementDto> result = settlementService.calculateSettlements(1L);

      assertThat(result).isEmpty();
      verify(settlementRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("returns persisted settlements without recalculating when L2 cache is warm")
    void calculateSettlements_shouldReturnPersistedSettlements_whenL2CacheIsWarm() {
      User alice = user(1L, "Alice", "Smith");
      User bob = user(2L, "Bob", "Jones");
      Group group = Group.builder().id(1L).name("Trip").build();
      Settlement settlement =
          Settlement.builder()
              .id(10L)
              .group(group)
              .fromUser(bob)
              .toUser(alice)
              .amount(new BigDecimal("30.00"))
              .build();
      SettlementDto settlementDto =
          SettlementDto.builder()
              .fromUserFirstName("Bob")
              .toUserFirstName("Alice")
              .amount(new BigDecimal("30.00"))
              .build();

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of(settlement));
      when(modelMapper.map(settlement, SettlementDto.class)).thenReturn(settlementDto);

      List<SettlementDto> result = settlementService.calculateSettlements(1L);

      assertThat(result).containsExactly(settlementDto);
      verify(groupRepository, never()).findById(any());
    }

    @Test
    @DisplayName("two-person expense: debtor pays creditor the full shared amount")
    void calculateSettlements_shouldReturnDebtorPaysCreditor_whenTwoPersonExpense() {
      User alice = user(1L, "Alice", "Smith");
      User bob = user(2L, "Bob", "Jones");
      Group group = Group.builder().id(1L).name("Trip").build();
      // Alice paid 30; Bob owes 30 (Alice is not a participant herself)
      Expense expense = expense(1L, group, alice, participant(bob, "30.00"));
      Group groupWithExpense = groupWithExpenses(1L, expense);

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(groupWithExpense));
      when(settlementRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Settlement.class), eq(SettlementDto.class)))
          .thenReturn(SettlementDto.builder().build());

      settlementService.calculateSettlements(1L);

      ArgumentCaptor<List<Settlement>> captor = settlementListCaptor();
      verify(settlementRepository).saveAll(captor.capture());
      List<Settlement> saved = captor.getValue();

      assertThat(saved).hasSize(1);
      assertThat(saved.getFirst().getFromUser().getId()).isEqualTo(bob.getId());
      assertThat(saved.getFirst().getToUser().getId()).isEqualTo(alice.getId());
      assertThat(saved.getFirst().getAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("payer who is also a participant: their share reduces their net credit")
    void calculateSettlements_shouldReduceNetCredit_whenPayerIsAlsoParticipant() {
      User alice = user(1L, "Alice", "Smith");
      User bob = user(2L, "Bob", "Jones");
      User charlie = user(3L, "Charlie", "Brown");
      Group group = Group.builder().id(1L).name("Trip").build();
      // Alice pays 90, split equally Alice(30)+Bob(30)+Charlie(30)
      // Net: Alice = +90 - 30 = +60, Bob = -30, Charlie = -30
      Expense expense =
          expense(
              1L,
              group,
              alice,
              participant(alice, "30.00"),
              participant(bob, "30.00"),
              participant(charlie, "30.00"));
      Group groupWithExpense = groupWithExpenses(1L, expense);

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(groupWithExpense));
      when(settlementRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Settlement.class), eq(SettlementDto.class)))
          .thenReturn(SettlementDto.builder().build());

      settlementService.calculateSettlements(1L);

      ArgumentCaptor<List<Settlement>> captor = settlementListCaptor();
      verify(settlementRepository).saveAll(captor.capture());
      List<Settlement> saved = captor.getValue();

      assertThat(saved)
          .hasSize(2)
          .allMatch(settlement -> settlement.getToUser().getId().equals(alice.getId()))
          .allMatch(settlement -> settlement.getAmount().compareTo(new BigDecimal("30.00")) == 0);
      assertThat(saved.stream().map(settlement -> settlement.getFromUser().getId()).toList())
          .containsExactlyInAnyOrder(bob.getId(), charlie.getId());
    }

    @Test
    @DisplayName("expenses whose net balances cancel out produce no settlements")
    void calculateSettlements_shouldReturnNoSettlements_whenNetBalancesCancelOut() {
      User alice = user(1L, "Alice", "Smith");
      User bob = user(2L, "Bob", "Jones");
      Group group = Group.builder().id(1L).name("Trip").build();
      // Alice pays 30 for Bob; Bob pays 30 for Alice → net zero for both
      Expense expense1 = expense(1L, group, alice, participant(bob, "30.00"));
      Expense expense2 = expense(2L, group, bob, participant(alice, "30.00"));
      Group groupWithBothExpenses = groupWithExpenses(1L, expense1, expense2);

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(groupWithBothExpenses));
      when(settlementRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

      List<SettlementDto> result = settlementService.calculateSettlements(1L);

      assertThat(result).isEmpty();
      ArgumentCaptor<List<Settlement>> captor = settlementListCaptor();
      verify(settlementRepository).saveAll(captor.capture());
      assertThat(captor.getValue()).isEmpty();
    }

    @Test
    @DisplayName("payer is the sole participant: net balance is zero, no settlements")
    void calculateSettlements_shouldReturnNoSettlements_whenPayerIsSoleParticipant() {
      User alice = user(1L, "Alice", "Smith");
      Group group = Group.builder().id(1L).name("Solo").build();
      // Alice pays 50 and is the only participant → +50 - 50 = 0
      Expense expense = expense(1L, group, alice, participant(alice, "50.00"));
      Group groupWithExpense = groupWithExpenses(1L, expense);

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(groupWithExpense));
      when(settlementRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

      List<SettlementDto> result = settlementService.calculateSettlements(1L);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("unequal shares: each debtor's settlement amount matches their individual share")
    void calculateSettlements_shouldMatchIndividualShares_whenSharesAreUnequal() {
      User alice = user(1L, "Alice", "Smith");
      User bob = user(2L, "Bob", "Jones");
      User charlie = user(3L, "Charlie", "Brown");
      Group group = Group.builder().id(1L).name("Trip").build();
      // Alice pays 90: Bob owes 60, Charlie owes 30
      // Creditor: Alice(90). Debtors: Bob(60), Charlie(30).
      // Match Alice(90) vs Bob(60): Bob pays Alice 60, Alice remaining = 30.
      // Match Alice(30) vs Charlie(30): Charlie pays Alice 30.
      Expense expense =
          expense(1L, group, alice, participant(bob, "60.00"), participant(charlie, "30.00"));
      Group groupWithExpense = groupWithExpenses(1L, expense);

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(groupWithExpense));
      when(settlementRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Settlement.class), eq(SettlementDto.class)))
          .thenReturn(SettlementDto.builder().build());

      settlementService.calculateSettlements(1L);

      ArgumentCaptor<List<Settlement>> captor = settlementListCaptor();
      verify(settlementRepository).saveAll(captor.capture());
      List<Settlement> saved = captor.getValue();

      assertThat(saved).hasSize(2);
      assertThat(saved).allMatch(settlement -> settlement.getToUser().getId().equals(alice.getId()));

      Map<Long, BigDecimal> amountByDebtor = new HashMap<>();
      saved.forEach(settlement -> amountByDebtor.put(settlement.getFromUser().getId(), settlement.getAmount()));
      assertThat(amountByDebtor.get(bob.getId())).isEqualByComparingTo("60.00");
      assertThat(amountByDebtor.get(charlie.getId())).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("complex multi-user scenario: settlements are minimal and amounts are correct")
    void calculateSettlements_shouldMinimizeTransactions_whenComplexMultiUserScenario() {
      User alice = user(1L, "Alice", "Smith");
      User bob = user(2L, "Bob", "Jones");
      User charlie = user(3L, "Charlie", "Brown");
      Group group = Group.builder().id(1L).name("Trip").build();
      // Expense 1: Alice pays 60, split Alice(30)+Bob(30) → Alice net +30, Bob -30
      // Expense 2: Bob pays 30, split Bob(15)+Charlie(15)
      //   → Bob net = -30 + 30 - 15 = -15, Charlie = -15, Alice = +30 (unchanged)
      // Final: Alice=+30, Bob=-15, Charlie=-15 → Bob pays Alice 15, Charlie pays Alice 15
      Expense expense1 =
          expense(1L, group, alice, participant(alice, "30.00"), participant(bob, "30.00"));
      Expense expense2 =
          expense(2L, group, bob, participant(bob, "15.00"), participant(charlie, "15.00"));
      Group groupWithBothExpenses = groupWithExpenses(1L, expense1, expense2);

      when(settlementRepository.findByGroupId(1L)).thenReturn(List.of());
      when(groupRepository.findById(1L)).thenReturn(Optional.of(groupWithBothExpenses));
      when(settlementRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Settlement.class), eq(SettlementDto.class)))
          .thenReturn(SettlementDto.builder().build());

      settlementService.calculateSettlements(1L);

      ArgumentCaptor<List<Settlement>> captor = settlementListCaptor();
      verify(settlementRepository).saveAll(captor.capture());
      List<Settlement> saved = captor.getValue();

      assertThat(saved).hasSize(2);
      assertThat(saved).allMatch(settlement -> settlement.getToUser().getId().equals(alice.getId()));
      assertThat(saved.stream().map(settlement -> settlement.getFromUser().getId()).toList())
          .containsExactlyInAnyOrder(bob.getId(), charlie.getId());
      assertThat(saved).allMatch(settlement -> settlement.getAmount().compareTo(new BigDecimal("15.00")) == 0);
    }
  }

  @Nested
  @DisplayName("clearSettlementCache")
  class ClearSettlementCache {

    @Test
    @DisplayName("deletes all persisted settlements for the group from the repository")
    void clearSettlementCache_shouldDeleteSettlementsForGroup_whenCalled() {
      settlementService.clearSettlementCache(1L);

      verify(settlementRepository).deleteByGroupId(1L);
    }
  }
}
