package io.plan.mate.expense.tracker.backend.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("MemberController integration tests")
class MemberControllerIntegrationTest extends AbstractIntegrationTest {

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

  private String addUserPayload(final String username) {
    return "{\"username\": %s}"
        .formatted(username == null ? "null" : "\"" + username + "\"");
  }

  @Nested
  @DisplayName("addUserToGroup")
  class AddUserToGroup {

    @Test
    @DisplayName("adds the user as a MEMBER when the request is valid")
    void addUserToGroup_shouldAddUserAsMember_whenRequestIsValid() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/users", group.getId())
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(addUserPayload(outsider.getUsername())))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.username").value(outsider.getUsername()))
          .andExpect(jsonPath("$.firstName").value("Otto"))
          .andExpect(jsonPath("$.groupName").value("Trip"))
          .andExpect(jsonPath("$.role").value("MEMBER"));

      assertThat(memberRepository.findByGroupIdAndUserId(group.getId(), outsider.getId()))
          .isPresent();
    }

    @Test
    @DisplayName("returns 404 for an unknown username")
    void addUserToGroup_shouldReturnNotFound_whenUsernameDoesNotExist() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/users", group.getId())
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(addUserPayload("ghost")))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(containsString("ghost")));
    }

    @Test
    @DisplayName("returns 404 for an unknown group id")
    void addUserToGroup_shouldReturnNotFound_whenGroupDoesNotExist() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/users", 999_999L)
                  .with(asAdmin(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(addUserPayload(outsider.getUsername())))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 400 when the user is already a member")
    void addUserToGroup_shouldReturnBadRequest_whenAlreadyAMember() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/users", group.getId())
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(addUserPayload(member.getUsername())))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("already a member")));
    }

    @Test
    @DisplayName("returns 400 when the username is blank")
    void addUserToGroup_shouldReturnBadRequest_whenUsernameBlank() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/users", group.getId())
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(addUserPayload("")))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(containsString("Username must not be blank")));
    }

    @Test
    @DisplayName("returns 400 when the username exceeds 30 characters")
    void addUserToGroup_shouldReturnBadRequest_whenUsernameTooLong() throws Exception {
      mockMvc
          .perform(
              post("/api/groups/{groupId}/users", group.getId())
                  .with(asUser(owner))
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(addUserPayload("x".repeat(31))))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(containsString("Username must not exceed 30 characters")));
    }
  }

  @Nested
  @DisplayName("removeUserFromGroup")
  class RemoveUserFromGroup {

    @Test
    @DisplayName("removes the member when the caller is the owner")
    void removeUserFromGroup_shouldRemoveMember_whenCallerIsOwner() throws Exception {
      final Member membership =
          memberRepository.findByGroupIdAndUserId(group.getId(), member.getId()).orElseThrow();

      mockMvc
          .perform(
              delete("/api/groups/{groupId}/users/{memberId}", group.getId(), membership.getId())
                  .with(asUser(owner)))
          .andExpect(status().isNoContent());

      assertThat(memberRepository.findById(membership.getId())).isEmpty();
    }

    @Test
    @DisplayName("allows a plain member to remove themselves")
    void removeUserFromGroup_shouldAllowSelfRemoval_whenCallerIsThatMember() throws Exception {
      final Member membership =
          memberRepository.findByGroupIdAndUserId(group.getId(), member.getId()).orElseThrow();

      mockMvc
          .perform(
              delete("/api/groups/{groupId}/users/{memberId}", group.getId(), membership.getId())
                  .with(asUser(member)))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("returns 403 when a plain member tries to remove someone else")
    void removeUserFromGroup_shouldReturnForbidden_whenMemberRemovesSomeoneElse() throws Exception {
      final Member ownerMembership =
          memberRepository.findByGroupIdAndUserId(group.getId(), owner.getId()).orElseThrow();

      mockMvc
          .perform(
              delete(
                      "/api/groups/{groupId}/users/{memberId}",
                      group.getId(),
                      ownerMembership.getId())
                  .with(asUser(member)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.error").value("Forbidden"))
          .andExpect(
              jsonPath("$.message").value("You do not have permission to perform this action"));
    }

    @Test
    @DisplayName("returns 409 when removing the last remaining owner")
    void removeUserFromGroup_shouldReturnConflict_whenRemovingLastOwner() throws Exception {
      final Member ownerMembership =
          memberRepository.findByGroupIdAndUserId(group.getId(), owner.getId()).orElseThrow();

      mockMvc
          .perform(
              delete(
                      "/api/groups/{groupId}/users/{memberId}",
                      group.getId(),
                      ownerMembership.getId())
                  .with(asUser(owner)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(containsString("must keep at least one owner")));
    }

    @Test
    @DisplayName("allows removing an owner when another owner remains")
    void removeUserFromGroup_shouldSucceed_whenAnotherOwnerRemains() throws Exception {
      persistMember(outsider, group, MemberRole.OWNER);
      final Member ownerMembership =
          memberRepository.findByGroupIdAndUserId(group.getId(), owner.getId()).orElseThrow();

      mockMvc
          .perform(
              delete(
                      "/api/groups/{groupId}/users/{memberId}",
                      group.getId(),
                      ownerMembership.getId())
                  .with(asUser(owner)))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("returns 404 for an unknown membership id")
    void removeUserFromGroup_shouldReturnNotFound_whenMembershipDoesNotExist() throws Exception {
      mockMvc
          .perform(
              delete("/api/groups/{groupId}/users/{memberId}", group.getId(), 999_999L)
                  .with(asUser(owner)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Membership not found"));
    }
  }

  @Nested
  @DisplayName("getGroupMembers")
  class GetGroupMembers {

    @Test
    @DisplayName("returns all members of the group")
    void getGroupMembers_shouldReturnAllMembers() throws Exception {
      mockMvc
          .perform(get("/api/groups/{groupId}/users", group.getId()).with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("returns an empty list for an unknown group id rather than an error")
    void getGroupMembers_shouldReturnEmptyList_whenGroupDoesNotExist() throws Exception {
      mockMvc
          .perform(get("/api/groups/{groupId}/users", 999_999L).with(asAdmin(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(0));
    }
  }

  @Nested
  @DisplayName("getUserGroups")
  class GetUserGroups {

    @Test
    @DisplayName("returns the caller's own groups")
    void getUserGroups_shouldReturnOwnGroups_whenCallerRequestsSelf() throws Exception {
      mockMvc
          .perform(get("/api/users/{userId}/groups", owner.getId()).with(asUser(owner)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].name").value("Trip"));
    }

    @Test
    @DisplayName("returns 404 for an unknown user id")
    void getUserGroups_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      mockMvc
          .perform(get("/api/users/{userId}/groups", 999_999L).with(asAdmin(owner)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(containsString("not found")));
    }
  }
}
