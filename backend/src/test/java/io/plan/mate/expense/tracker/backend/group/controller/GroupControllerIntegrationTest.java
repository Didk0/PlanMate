package io.plan.mate.expense.tracker.backend.group.controller;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.participant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@DisplayName("GroupController integration tests")
class GroupControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired private SettlementService settlementService;
  @Autowired private SettlementRepository settlementRepository;

  private User owner;
  private User outsider;
  private User admin;

  @BeforeEach
  void setUp() {
    owner = persistUser("Olivia", "Owner");
    outsider = persistUser("Otto", "Outsider");
    admin = persistUser("Ada", "Admin");
  }

  private String createGroupPayload(final String name, final String description) {
    return """
        {
          "name": %s,
          "description": %s
        }
        """
        .formatted(
            name == null ? "null" : "\"" + name + "\"",
            description == null ? "null" : "\"" + description + "\"");
  }

  @Nested
  @DisplayName("createGroup")
  class CreateGroup {

    @Test
    @DisplayName("persists the group and makes the caller its OWNER when the request is valid")
    void createGroup_shouldPersistGroupAndMakeCallerOwner_whenRequestIsValid() throws Exception {
      mockMvc
          .perform(
              post("/api/groups")
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(createGroupPayload("Trip", "Summer trip")))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name").value("Trip"))
          .andExpect(jsonPath("$.description").value("Summer trip"))
          .andExpect(jsonPath("$.id").isNotEmpty());

      final var members = memberRepository.findByUserId(owner.getId());
      assertThat(members).anySatisfy(m -> assertThat(m.getRole()).isEqualTo(MemberRole.OWNER));
    }

    @Test
    @DisplayName("returns 400 when the name is blank")
    void createGroup_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/groups")
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(createGroupPayload("", "Summer trip")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error").value("Validation Error"))
          .andExpect(jsonPath("$.message").value(containsString("Group name must not be blank")));
    }

    @Test
    @DisplayName("returns 400 when the name exceeds 30 characters")
    void createGroup_shouldReturnBadRequest_whenNameTooLong() throws Exception {
      mockMvc
          .perform(
              post("/api/groups")
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(createGroupPayload("x".repeat(31), null)))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(containsString("Group name must not exceed 30 characters")));
    }

    @Test
    @DisplayName("returns 400 when the description exceeds 100 characters")
    void createGroup_shouldReturnBadRequest_whenDescriptionTooLong() throws Exception {
      mockMvc
          .perform(
              post("/api/groups")
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(createGroupPayload("Trip", "x".repeat(101))))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message")
                  .value(containsString("Group description must not exceed 100 characters")));
    }

    @Test
    @DisplayName("returns 404 when the caller is not provisioned in PlanMate")
    void createGroup_shouldReturnNotFound_whenCallerIsNotProvisioned() throws Exception {
      mockMvc
          .perform(
              post("/api/groups")
                  .with(asUnprovisioned())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(createGroupPayload("Trip", null)))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.message").value("Authenticated user is not provisioned in PlanMate"));
    }
  }

  @Nested
  @DisplayName("getGroupById")
  class GetGroupById {

    @Test
    @DisplayName("returns 200 with the group details when the caller is a member")
    void getGroupById_shouldReturnOk_whenCallerIsAMember() throws Exception {
      final Group group = persistGroup("Trip");
      persistMember(owner, group, MemberRole.OWNER);

      mockMvc
          .perform(get("/api/groups/{groupId}", group.getId()).with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(group.getId()))
          .andExpect(jsonPath("$.name").value("Trip"));
    }

    @Test
    @DisplayName("returns 404 for an unknown group id")
    void getGroupById_shouldReturnNotFound_whenGroupDoesNotExist() throws Exception {
      mockMvc
          .perform(get("/api/groups/{groupId}", 999_999L).with(asAdmin(admin)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(containsString("not found")));
    }
  }

  @Nested
  @DisplayName("getAllGroups")
  class GetAllGroups {

    @Test
    @DisplayName("a non-admin caller sees only their own groups")
    void getAllGroups_shouldReturnOnlyOwnGroups_whenCallerIsNotAdmin() throws Exception {
      final Group ownGroup = persistGroup("Mine");
      persistMember(owner, ownGroup, MemberRole.OWNER);
      final Group otherGroup = persistGroup("Someone Else's");
      persistMember(outsider, otherGroup, MemberRole.OWNER);

      mockMvc
          .perform(get("/api/groups").with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.content[0].name").value("Mine"));
    }

    @Test
    @DisplayName("an admin caller sees every group")
    void getAllGroups_shouldReturnAllGroups_whenCallerIsAdmin() throws Exception {
      final Group ownGroup = persistGroup("Mine");
      persistMember(owner, ownGroup, MemberRole.OWNER);
      final Group otherGroup = persistGroup("Someone Else's");
      persistMember(outsider, otherGroup, MemberRole.OWNER);

      mockMvc
          .perform(get("/api/groups").with(asAdmin(admin)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("search filters groups by name for the caller")
    void getAllGroups_shouldFilterBySearchTerm_whenSearchProvided() throws Exception {
      final Group tripGroup = persistGroup("Spain Trip");
      persistMember(owner, tripGroup, MemberRole.OWNER);
      final Group otherGroup = persistGroup("Book Club");
      persistMember(owner, otherGroup, MemberRole.OWNER);

      mockMvc
          .perform(get("/api/groups").param("search", "trip").with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.content[0].name").value("Spain Trip"));
    }

    @Test
    @DisplayName("a literal % in the search term is escaped rather than acting as a wildcard")
    void getAllGroups_shouldTreatPercentLiterally_whenSearchContainsPercent() throws Exception {
      final Group percentGroup = persistGroup("100% Trip");
      persistMember(owner, percentGroup, MemberRole.OWNER);
      final Group otherGroup = persistGroup("Book Club");
      persistMember(owner, otherGroup, MemberRole.OWNER);

      mockMvc
          .perform(get("/api/groups").param("search", "100%").with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.content[0].name").value("100% Trip"));
    }

    @Test
    @DisplayName("paging parameters are reflected in the response envelope")
    void getAllGroups_shouldRespectPageAndSize_whenPagingRequested() throws Exception {
      for (int i = 0; i < 3; i++) {
        final Group group = persistGroup("Group " + i);
        persistMember(owner, group, MemberRole.OWNER);
      }

      mockMvc
          .perform(get("/api/groups").param("page", "1").param("size", "2").with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page").value(1))
          .andExpect(jsonPath("$.size").value(2))
          .andExpect(jsonPath("$.totalElements").value(3))
          .andExpect(jsonPath("$.totalPages").value(2))
          .andExpect(jsonPath("$.first").value(false))
          .andExpect(jsonPath("$.last").value(true))
          .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("returns an empty page rather than an error for an unprovisioned caller")
    void getAllGroups_shouldReturnEmptyPage_whenCallerIsNotProvisioned() throws Exception {
      mockMvc
          .perform(get("/api/groups").with(asUnprovisioned()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(0));
    }
  }

  @Nested
  @DisplayName("deleteGroup")
  class DeleteGroup {

    @Test
    @DisplayName(
        "deletes the group and its persisted settlements when the caller is the owner")
    void deleteGroup_shouldDeleteGroupAndSettlements_whenCallerIsOwner() throws Exception {
      final User bob = persistUser("Bob", "Jones");
      final Group group = persistGroup("Trip");
      persistMember(owner, group, MemberRole.OWNER);
      persistMember(bob, group, MemberRole.MEMBER);
      persistExpense(group, owner, List.of(participant(bob, "10.00")));
      flushAndClear();
      settlementService.calculateSettlements(group.getId());
      flushAndClear();
      assertThat(settlementRepository.findByGroupId(group.getId())).isNotEmpty();
      // Detach the settlement rows just loaded above: deleteGroup's bulk
      // "DELETE FROM Settlement" (SettlementRepository.deleteByGroupId) does not
      // synchronize the persistence context, so leftover managed Settlement
      // entities here would otherwise confuse Group's cascade-remove on flush.
      flushAndClear();

      mockMvc
          .perform(delete("/api/groups/{groupId}", group.getId()).with(asUser(owner)))
          .andExpect(status().isNoContent());
      flushAndClear();

      assertThat(groupRepository.findById(group.getId())).isEmpty();
      assertThat(settlementRepository.findByGroupId(group.getId())).isEmpty();
    }

    @Test
    @DisplayName("returns 404 for an unknown group id")
    void deleteGroup_shouldReturnNotFound_whenGroupDoesNotExist() throws Exception {
      mockMvc
          .perform(delete("/api/groups/{groupId}", 999_999L).with(asAdmin(admin)))
          .andExpect(status().isNotFound());
    }
  }
}
