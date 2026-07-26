package io.plan.mate.expense.tracker.backend.settlement.controller;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.participant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.settlement.jpa.entity.Settlement;
import io.plan.mate.expense.tracker.backend.settlement.jpa.repository.SettlementRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("SettlementController integration tests")
class SettlementControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private SettlementRepository settlementRepository;

  private User alice;
  private User bob;
  private User charlie;
  private User outsider;
  private Group group;

  @BeforeEach
  void setUp() {
    alice = persistUser("Alice", "Smith");
    bob = persistUser("Bob", "Jones");
    charlie = persistUser("Charlie", "Brown");
    outsider = persistUser("Otto", "Outsider");
    group = persistGroup("Trip");
    persistMember(alice, group, MemberRole.OWNER);
    persistMember(bob, group, MemberRole.MEMBER);
    persistMember(charlie, group, MemberRole.MEMBER);
  }

  @Test
  @DisplayName("returns a single settlement for a two-person expense")
  void calculateSettlements_shouldReturnSingleSettlement_whenTwoPersonExpense() throws Exception {
    persistExpense(group, alice, List.of(participant(bob, "30.00")));
    flushAndClear();

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].fromUserFirstName").value("Bob"))
        .andExpect(jsonPath("$[0].toUserFirstName").value("Alice"))
        .andExpect(jsonPath("$[0].amount").value(30.00));
  }

  @Test
  @DisplayName("returns the minimal transfer set for a three-person chain")
  void calculateSettlements_shouldReturnMinimalTransfers_whenThreePersonChain() throws Exception {
    persistExpense(
        group,
        alice,
        List.of(participant(alice, "30.00"), participant(bob, "30.00"), participant(charlie, "30.00")));
    flushAndClear();

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].toUserFirstName").value("Alice"))
        .andExpect(jsonPath("$[1].toUserFirstName").value("Alice"));
  }

  @Test
  @DisplayName("returns an empty list when balances net to zero")
  void calculateSettlements_shouldReturnEmptyList_whenBalancesNetToZero() throws Exception {
    persistExpense(group, alice, List.of(participant(bob, "25.00")));
    persistExpense(group, bob, List.of(participant(alice, "25.00")));
    flushAndClear();

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("returns an empty list for a group with no expenses")
  void calculateSettlements_shouldReturnEmptyList_whenGroupHasNoExpenses() throws Exception {
    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("serves the second call from cache without recomputing")
  void calculateSettlements_shouldServeFromCache_onSecondCall() throws Exception {
    persistExpense(group, alice, List.of(participant(bob, "30.00")));
    flushAndClear();

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk());
    final var firstResultIds =
        settlementRepositoryIds(group.getId());

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk());
    final var secondResultIds = settlementRepositoryIds(group.getId());

    assertThat(secondResultIds).isEqualTo(firstResultIds);
  }

  private List<Long> settlementRepositoryIds(final Long groupId) {
    return settlementRepository.findByGroupId(groupId).stream()
        .map(Settlement::getId)
        .sorted()
        .toList();
  }

  @Test
  @DisplayName("a new expense invalidates the cache and changes the result")
  void calculateSettlements_shouldRecompute_whenNewExpenseCreatedAfterFirstCall() throws Exception {
    persistExpense(group, alice, List.of(participant(bob, "30.00")));
    flushAndClear();

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    mockMvc.perform(
        post("/api/groups/{groupId}/expenses", group.getId())
            .with(asUser(alice))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {
                  "description": "Lunch",
                  "amount": 30.00,
                  "paidByUsername": "%s",
                  "participants": [{"userName": "%s", "shareAmount": 30.00}]
                }
                """
                    .formatted(bob.getUsername(), alice.getUsername())));

    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(alice)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("returns 403 when the caller is not a member of the group")
  void calculateSettlements_shouldReturnForbidden_whenCallerIsNotAMember() throws Exception {
    mockMvc
        .perform(
            get("/api/groups/{groupId}/settlements/calculate", group.getId()).with(asUser(outsider)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("returns 404 for an unknown group id")
  void calculateSettlements_shouldReturnNotFound_whenGroupDoesNotExist() throws Exception {
    mockMvc
        .perform(get("/api/groups/{groupId}/settlements/calculate", 999_999L).with(asAdmin(alice)))
        .andExpect(status().isNotFound());
  }
}
