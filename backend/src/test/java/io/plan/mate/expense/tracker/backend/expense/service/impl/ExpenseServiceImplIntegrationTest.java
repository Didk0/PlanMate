package io.plan.mate.expense.tracker.backend.expense.service.impl;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.newUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.expense.service.ExpenseService;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.settlement.jpa.repository.SettlementRepository;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@DisplayName("ExpenseServiceImpl integration tests")
class ExpenseServiceImplIntegrationTest {

  @MockitoBean @SuppressWarnings("unused") private JwtDecoder jwtDecoder;

  @Autowired private ExpenseService expenseService;
  @Autowired private SettlementService settlementService;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private ExpenseRepository expenseRepository;
  @Autowired private SettlementRepository settlementRepository;
  @Autowired private MemberRepository memberRepository;
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

  private void persistMember(User user, Group group) {
    memberRepository.save(
        Member.builder()
            .user(user)
            .group(group)
            .role(MemberRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build());
  }

  @Test
  @DisplayName("persists the expense with its participants when the request is valid")
  void createExpense_shouldPersistExpenseWithParticipants_whenRequestIsValid() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistMember(alice, group);
    persistMember(bob, group);
    flushAndClear();

    CreateExpenseRequest request =
        new CreateExpenseRequest(
            "Dinner",
            new BigDecimal("50.00"),
            alice.getUsername(),
            List.of(new CreateExpenseParticipant(bob.getUsername(), new BigDecimal("50.00"))));

    ExpenseDto result = expenseService.createExpense(group.getId(), request);
    flushAndClear();

    assertThat(result.getId()).isNotNull();
    assertThat(result.getAmount()).isEqualByComparingTo("50.00");
    assertThat(result.getParticipants()).hasSize(1);

    assertThat(expenseRepository.findByGroupId(group.getId())).hasSize(1);
  }

  @Test
  @DisplayName("throws BadRequestException and persists nothing when shares don't sum to the amount")
  void createExpense_shouldThrowBadRequestExceptionAndPersistNothing_whenShareSumMismatch() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistMember(alice, group);
    persistMember(bob, group);
    flushAndClear();

    CreateExpenseRequest request =
        new CreateExpenseRequest(
            "Dinner",
            new BigDecimal("50.00"),
            alice.getUsername(),
            List.of(new CreateExpenseParticipant(bob.getUsername(), new BigDecimal("10.00"))));

    assertThatThrownBy(() -> expenseService.createExpense(group.getId(), request))
        .isInstanceOf(BadRequestException.class);

    assertThat(expenseRepository.findByGroupId(group.getId())).isEmpty();
  }

  @Test
  @DisplayName("throws ResourceNotFoundException and persists nothing when a participant does not exist")
  void createExpense_shouldThrowResourceNotFoundExceptionAndPersistNothing_whenParticipantMissing() {
    User alice = persistUser("Alice", "Smith");
    Group group = persistGroup("Trip");
    persistMember(alice, group);
    flushAndClear();

    CreateExpenseRequest request =
        new CreateExpenseRequest(
            "Dinner",
            new BigDecimal("50.00"),
            alice.getUsername(),
            List.of(new CreateExpenseParticipant("ghost", new BigDecimal("50.00"))));

    assertThatThrownBy(() -> expenseService.createExpense(group.getId(), request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("ghost");

    assertThat(expenseRepository.findByGroupId(group.getId())).isEmpty();
  }

  @Test
  @DisplayName("evicts persisted settlements for the group after creating an expense")
  void createExpense_shouldEvictPersistedSettlements_whenExpenseIsCreated() {
    User alice = persistUser("Alice", "Smith");
    User bob = persistUser("Bob", "Jones");
    Group group = persistGroup("Trip");
    persistMember(alice, group);
    persistMember(bob, group);
    flushAndClear();

    CreateExpenseRequest firstExpense =
        new CreateExpenseRequest(
            "Lunch",
            new BigDecimal("20.00"),
            alice.getUsername(),
            List.of(new CreateExpenseParticipant(bob.getUsername(), new BigDecimal("20.00"))));
    expenseService.createExpense(group.getId(), firstExpense);
    flushAndClear();

    // Prime both cache layers (Spring cache + persisted settlement rows)
    settlementService.calculateSettlements(group.getId());
    assertThat(settlementRepository.findByGroupId(group.getId())).hasSize(1);

    CreateExpenseRequest secondExpense =
        new CreateExpenseRequest(
            "Dinner",
            new BigDecimal("30.00"),
            bob.getUsername(),
            List.of(new CreateExpenseParticipant(alice.getUsername(), new BigDecimal("30.00"))));
    expenseService.createExpense(group.getId(), secondExpense);
    flushAndClear();

    assertThat(settlementRepository.findByGroupId(group.getId())).isEmpty();
  }
}
