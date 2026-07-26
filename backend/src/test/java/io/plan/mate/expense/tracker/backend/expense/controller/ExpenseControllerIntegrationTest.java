package io.plan.mate.expense.tracker.backend.expense.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.participant;

import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.settlement.jpa.repository.SettlementRepository;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@DisplayName("ExpenseController integration tests")
class ExpenseControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private SettlementService settlementService;
  @Autowired private SettlementRepository settlementRepository;

  private User alice;
  private User bob;
  private User outsiderUser;
  private Group group;

  @BeforeEach
  void setUp() {
    alice = persistUser("Alice", "Smith");
    bob = persistUser("Bob", "Jones");
    outsiderUser = persistUser("Otto", "Outsider");
    group = persistGroup("Trip");
    persistMember(alice, group, MemberRole.OWNER);
    persistMember(bob, group, MemberRole.MEMBER);
  }

  private String expensePayload(
      final String description, final String amount, final String payerUsername,
      final String... participantUsernameAndShare) {
    final StringBuilder participants = new StringBuilder();
    for (int i = 0; i < participantUsernameAndShare.length; i += 2) {
      if (!participants.isEmpty()) {
        participants.append(",");
      }
      participants.append(
          "{\"userName\": \"%s\", \"shareAmount\": %s}"
              .formatted(participantUsernameAndShare[i], participantUsernameAndShare[i + 1]));
    }
    return """
        {
          "description": %s,
          "amount": %s,
          "paidByUsername": "%s",
          "participants": [%s]
        }
        """
        .formatted(
            description == null ? "null" : "\"" + description + "\"",
            amount,
            payerUsername,
            participants);
  }

  @Nested
  @DisplayName("createExpense")
  class CreateExpense {

    @Test
    @DisplayName("persists the expense and its participants when the request is valid")
    void createExpense_shouldPersistExpense_whenRequestIsValid() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      expensePayload(
                          "Dinner", "50.00", alice.getUsername(), bob.getUsername(), "50.00")))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.description").value("Dinner"))
          .andExpect(jsonPath("$.amount").value(50.00))
          .andExpect(jsonPath("$.paidByUsername").value(alice.getUsername()))
          .andExpect(jsonPath("$.participants.length()").value(1));

      assertThat(expenseRepository.findByGroupId(group.getId())).hasSize(1);
    }

    @Test
    @DisplayName("allows the payer to also be a participant")
    void createExpense_shouldSucceed_whenPayerIsAlsoParticipant() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      expensePayload(
                          "Groceries",
                          "40.00",
                          alice.getUsername(),
                          alice.getUsername(), "20.00",
                          bob.getUsername(), "20.00")))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.participants.length()").value(2));
    }

    @Test
    @DisplayName("returns 400 when participant shares do not sum to the expense amount")
    void createExpense_shouldReturnBadRequest_whenShareSumMismatch() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      expensePayload(
                          "Dinner", "50.00", alice.getUsername(), bob.getUsername(), "10.00")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(containsString("must equal the expense amount")));

      assertThat(expenseRepository.findByGroupId(group.getId())).isEmpty();
    }

    @Test
    @DisplayName("accepts shares that sum equal in value but differ in scale")
    void createExpense_shouldSucceed_whenSharesEqualButDifferentScale() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "100", alice.getUsername(), bob.getUsername(), "100.00")))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("returns 404 when a participant username does not exist")
    void createExpense_shouldReturnNotFound_whenParticipantMissing() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "50.00", alice.getUsername(), "ghost", "50.00")))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(containsString("ghost")));
    }

    @Test
    @DisplayName("returns 400 when a participant exists but is not a member of the group")
    void createExpense_shouldReturnBadRequest_whenParticipantNotAMember() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asAdmin(outsiderUser))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      expensePayload(
                          "Dinner",
                          "50.00",
                          alice.getUsername(),
                          outsiderUser.getUsername(), "50.00")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("are not members of group")));
    }

    @Test
    @DisplayName("returns 404 for an unknown group id")
    void createExpense_shouldReturnNotFound_whenGroupDoesNotExist() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", 999_999L)
                  .with(asAdmin(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "50.00", alice.getUsername(), bob.getUsername(), "50.00")))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when amount is null")
    void createExpense_shouldReturnBadRequest_whenAmountIsNull() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "description": "Dinner",
                        "amount": null,
                        "paidByUsername": "%s",
                        "participants": [{"userName": "%s", "shareAmount": 10.00}]
                      }
                      """
                          .formatted(alice.getUsername(), bob.getUsername())))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("Amount is required")));
    }

    @Test
    @DisplayName("returns 400 when amount is zero or negative")
    void createExpense_shouldReturnBadRequest_whenAmountNotPositive() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      expensePayload("Dinner", "-5.00", alice.getUsername(), bob.getUsername(), "-5.00")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("Amount must be positive")));
    }

    @Test
    @DisplayName("returns 400 when paidByUsername is blank")
    void createExpense_shouldReturnBadRequest_whenPaidByUsernameBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "50.00", "", bob.getUsername(), "50.00")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("Paid by Username is required")));
    }

    @Test
    @DisplayName("returns 400 when participants list is empty")
    void createExpense_shouldReturnBadRequest_whenParticipantsEmpty() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {
                        "description": "Dinner",
                        "amount": 50.00,
                        "paidByUsername": "%s",
                        "participants": []
                      }
                      """
                          .formatted(alice.getUsername())))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(containsString("At least one participant is required")));
    }

    @Test
    @DisplayName("returns 400 when a participant's username is blank")
    void createExpense_shouldReturnBadRequest_whenParticipantUsernameBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "50.00", alice.getUsername(), "", "50.00")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("Username is required")));
    }

    @Test
    @DisplayName("returns 400 when a participant's share amount is zero")
    void createExpense_shouldReturnBadRequest_whenParticipantShareNotPositive() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "50.00", alice.getUsername(), bob.getUsername(), "0.00")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("Share must be positive")));
    }

    @Test
    @DisplayName("returns 400 when description exceeds 255 characters")
    void createExpense_shouldReturnBadRequest_whenDescriptionTooLong() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      expensePayload(
                          "x".repeat(256), "50.00", alice.getUsername(), bob.getUsername(), "50.00")))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(containsString("Description must be at most 255 characters")));
    }

    @Test
    @DisplayName("evicts the settlement cache after creating an expense")
    void createExpense_shouldEvictSettlementCache_whenExpenseIsCreated() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/expenses", group.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Dinner", "20.00", alice.getUsername(), bob.getUsername(), "20.00")));
      flushAndClear();
      settlementService.calculateSettlements(group.getId());
      assertThat(settlementRepository.findByGroupId(group.getId())).isNotEmpty();
      flushAndClear();

      mockMvc.perform(
          post("/api/groups/{groupId}/expenses", group.getId())
              .with(asUser(alice))
              .contentType(MediaType.APPLICATION_JSON)
              .content(expensePayload("Lunch", "10.00", bob.getUsername(), alice.getUsername(), "10.00")));
      flushAndClear();

      assertThat(settlementRepository.findByGroupId(group.getId())).isEmpty();
    }
  }

  @Nested
  @DisplayName("getGroupExpenses")
  class GetGroupExpenses {

    @Test
    @DisplayName("returns expenses ordered newest first by default")
    void getGroupExpenses_shouldReturnNewestFirst_byDefault() throws Exception {
      persistExpense(group, alice, List.of(participant(bob, "10.00")));
      persistExpense(group, bob, List.of(participant(alice, "20.00")));
      flushAndClear();

      mockMvc
          .perform(get("/api/groups/{groupId}/expenses", group.getId()).with(asUser(alice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("filters by description via search")
    void getGroupExpenses_shouldFilterByDescription_whenSearchProvided() throws Exception {
      persistExpense(group, alice, List.of(participant(bob, "10.00")))
          .setDescription("Dinner out");
      persistExpense(group, bob, List.of(participant(alice, "20.00")))
          .setDescription("Groceries");
      flushAndClear();

      mockMvc
          .perform(
              get("/api/groups/{groupId}/expenses", group.getId())
                  .param("search", "dinner")
                  .with(asUser(alice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("returns an empty page for a group with no expenses")
    void getGroupExpenses_shouldReturnEmptyPage_whenGroupHasNoExpenses() throws Exception {
      mockMvc
          .perform(get("/api/groups/{groupId}/expenses", group.getId()).with(asUser(alice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("eagerly includes participants without a lazy-init error")
    void getGroupExpenses_shouldIncludeParticipants_withoutLazyInitError() throws Exception {
      persistExpense(group, alice, List.of(participant(bob, "10.00")));
      flushAndClear();

      mockMvc
          .perform(get("/api/groups/{groupId}/expenses", group.getId()).with(asUser(alice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].participants.length()").value(1))
          .andExpect(jsonPath("$.content[0].participants[0].shareAmount").value(10.00));
    }

    @Test
    @DisplayName("respects explicit page and size overrides")
    void getGroupExpenses_shouldRespectPageAndSize_whenProvided() throws Exception {
      for (int i = 0; i < 3; i++) {
        persistExpense(group, alice, List.of(participant(bob, "1.00")));
      }
      flushAndClear();

      mockMvc
          .perform(
              get("/api/groups/{groupId}/expenses", group.getId())
                  .param("page", "1")
                  .param("size", "2")
                  .with(asUser(alice)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page").value(1))
          .andExpect(jsonPath("$.size").value(2))
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.content.length()").value(1));
    }
  }

  @Nested
  @DisplayName("updateExpense")
  class UpdateExpense {

    @Test
    @DisplayName("replaces participants and updates fields when the request is valid")
    void updateExpense_shouldReplaceParticipants_whenRequestIsValid() throws Exception {
      final var expense = persistExpense(group, alice, List.of(participant(bob, "10.00")));
      flushAndClear();

      mockMvc
          .perform(
              put("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Updated", "30.00", bob.getUsername(), alice.getUsername(), "30.00")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.description").value("Updated"))
          .andExpect(jsonPath("$.amount").value(30.00))
          .andExpect(jsonPath("$.participants.length()").value(1))
          .andExpect(jsonPath("$.participants[0].username").value(alice.getUsername()));
    }

    @Test
    @DisplayName("returns 404 for an unknown expense id")
    void updateExpense_shouldReturnNotFound_whenExpenseDoesNotExist() throws Exception {
      mockMvc
          .perform(
              put("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), 999_999L)
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Updated", "30.00", alice.getUsername(), bob.getUsername(), "30.00")))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 when the expense belongs to a different group")
    void updateExpense_shouldReturnNotFound_whenExpenseInDifferentGroup() throws Exception {
      final Group otherGroup = persistGroup("Other");
      persistMember(alice, otherGroup, MemberRole.OWNER);
      final var expense =
          persistExpense(otherGroup, alice, List.of(participant(bob, "10.00")));
      flushAndClear();

      mockMvc
          .perform(
              put("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Updated", "30.00", alice.getUsername(), bob.getUsername(), "30.00")))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(containsString("not found in group")));
    }

    @Test
    @DisplayName("returns 400 when updated shares do not sum to the amount")
    void updateExpense_shouldReturnBadRequest_whenShareSumMismatch() throws Exception {
      final var expense = persistExpense(group, alice, List.of(participant(bob, "10.00")));
      flushAndClear();

      mockMvc
          .perform(
              put("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
                  .with(asUser(alice))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(expensePayload("Updated", "30.00", alice.getUsername(), bob.getUsername(), "5.00")))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("deleteExpense")
  class DeleteExpense {

    @Test
    @DisplayName("deletes the expense and its participants")
    void deleteExpense_shouldDeleteExpense_whenItExists() throws Exception {
      final var expense = persistExpense(group, alice, List.of(participant(bob, "10.00")));
      flushAndClear();

      mockMvc
          .perform(
              delete("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
                  .with(asUser(alice)))
          .andExpect(status().isNoContent());
      flushAndClear();

      assertThat(expenseRepository.findById(expense.getId())).isEmpty();
    }

    @Test
    @DisplayName("returns 404 for an unknown expense id")
    void deleteExpense_shouldReturnNotFound_whenExpenseDoesNotExist() throws Exception {
      mockMvc
          .perform(
              delete("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), 999_999L)
                  .with(asUser(alice)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 when the expense belongs to a different group")
    void deleteExpense_shouldReturnNotFound_whenExpenseInDifferentGroup() throws Exception {
      final Group otherGroup = persistGroup("Other");
      persistMember(alice, otherGroup, MemberRole.OWNER);
      final var expense =
          persistExpense(otherGroup, alice, List.of(participant(bob, "10.00")));
      flushAndClear();

      mockMvc
          .perform(
              delete("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
                  .with(asUser(alice)))
          .andExpect(status().isNotFound());
    }
  }
}
