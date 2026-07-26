package io.plan.mate.expense.tracker.backend.commons.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("Endpoint authorization")
class AuthorizationIntegrationTest extends AbstractIntegrationTest {

  private User owner;
  private User member;
  private User outsider;
  private Group group;

  @BeforeEach
  void setUp() {
    owner = persistUser("Olivia", "Owner");
    member = persistUser("Mia", "Member");
    outsider = persistUser("Otto", "Outsider");

    group = persistGroup("Trip");

    persistMember(owner, group, MemberRole.OWNER);
    persistMember(member, group, MemberRole.MEMBER);
  }

  @Test
  @DisplayName("getGroupById returns 403 when the caller is not a member of the group")
  void getGroupById_shouldReturnForbidden_whenCallerIsNotAMember() throws Exception {
    mockMvc
        .perform(get("/api/groups/{groupId}", group.getId()).with(asUser(outsider)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getGroupById returns 200 when the caller is a member of the group")
  void getGroupById_shouldReturnOk_whenCallerIsAMember() throws Exception {
    mockMvc
        .perform(get("/api/groups/{groupId}", group.getId()).with(asUser(member)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("deleteGroup returns 403 when the caller is a plain member, not the owner")
  void deleteGroup_shouldReturnForbidden_whenCallerIsAPlainMember() throws Exception {
    mockMvc
        .perform(delete("/api/groups/{groupId}", group.getId()).with(asUser(member)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("deleteGroup returns 204 when the caller is the owner")
  void deleteGroup_shouldReturnNoContent_whenCallerIsTheOwner() throws Exception {
    mockMvc
        .perform(delete("/api/groups/{groupId}", group.getId()).with(asUser(owner)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("getAllUsers returns 403 when the caller lacks the ADMIN role")
  void getAllUsers_shouldReturnForbidden_whenCallerLacksAdminRole() throws Exception {
    mockMvc.perform(get("/api/users").with(asUser(outsider))).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getAllUsers returns 200 when the caller has the ADMIN role")
  void getAllUsers_shouldReturnOk_whenCallerHasAdminRole() throws Exception {
    mockMvc.perform(get("/api/users").with(asAdmin(outsider))).andExpect(status().isOk());
  }

  @Test
  @DisplayName("getUserGroups returns 403 when userId is not the caller's own id")
  void getUserGroups_shouldReturnForbidden_whenUserIdIsNotTheCaller() throws Exception {
    mockMvc
        .perform(get("/api/users/{userId}/groups", owner.getId()).with(asUser(outsider)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("getUserGroups returns 200 when userId is the caller's own id")
  void getUserGroups_shouldReturnOk_whenUserIdIsTheCaller() throws Exception {
    mockMvc
        .perform(get("/api/users/{userId}/groups", owner.getId()).with(asUser(owner)))
        .andExpect(status().isOk());
  }

  private String validExpensePayload() {
    return """
        {
          "description": "Dinner",
          "amount": 10.00,
          "paidByUsername": "%s",
          "participants": [{"userName": "%s", "shareAmount": 10.00}]
        }
        """
        .formatted(member.getUsername(), owner.getUsername());
  }

  @Test
  @DisplayName("createExpense returns 403 when the caller is not a member of the group")
  void createExpense_shouldReturnForbidden_whenCallerIsNotAMember() throws Exception {
    mockMvc
        .perform(
            post("/api/groups/{groupId}/expenses", group.getId())
                .with(asUser(outsider))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validExpensePayload()))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("createExpense returns 201 when the caller is a member of the group")
  void createExpense_shouldReturnCreated_whenCallerIsAMember() throws Exception {
    mockMvc
        .perform(
            post("/api/groups/{groupId}/expenses", group.getId())
                .with(asUser(member))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validExpensePayload()))
        .andExpect(status().isCreated());
  }
}
