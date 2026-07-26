package io.plan.mate.expense.tracker.backend.user.controller;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.participant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

@DisplayName("UserController integration tests")
class UserControllerIntegrationTest extends AbstractIntegrationTest {

  private User admin;

  @BeforeEach
  void setUp() {
    admin = persistUser("Ada", "Admin");
  }

  private JwtRequestPostProcessor asNewUser(
      final UUID keycloakId, final String preferredUsername) {
    return jwt()
        .jwt(
            builder ->
                builder
                    .subject(keycloakId.toString())
                    .claim("preferred_username", preferredUsername)
                    .claim("email", preferredUsername + "@test.com")
                    .claim("given_name", "New")
                    .claim("family_name", "User"))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Nested
  @DisplayName("createUser")
  class CreateUser {

    @Test
    @DisplayName("creates a new PlanMate user from the JWT claims on first call")
    void createUser_shouldCreateUser_whenFirstCall() throws Exception {
      final UUID keycloakId = UUID.randomUUID();

      mockMvc
          .perform(post("/api/users").with(asNewUser(keycloakId, "newbie")))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.username").value("newbie"))
          .andExpect(jsonPath("$.email").value("newbie@test.com"))
          .andExpect(jsonPath("$.firstName").value("New"))
          .andExpect(jsonPath("$.lastName").value("User"));

      assertThat(userRepository.findByKeycloakId(keycloakId)).isPresent();
    }

    @Test
    @DisplayName("is idempotent and re-syncs the profile from Keycloak on a second call")
    void createUser_shouldResyncProfile_whenCalledAgain() throws Exception {
      final UUID keycloakId = UUID.randomUUID();
      mockMvc.perform(post("/api/users").with(asNewUser(keycloakId, "newbie")));
      flushAndClear();

      final UserRepresentation representation = new UserRepresentation();
      representation.setUsername("resynced");
      representation.setEmail("resynced@test.com");
      representation.setFirstName("Resynced");
      representation.setLastName("Name");
      when(keycloakService.getUser(any())).thenReturn(representation);

      mockMvc
          .perform(post("/api/users").with(asNewUser(keycloakId, "newbie")))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.username").value("resynced"))
          .andExpect(jsonPath("$.firstName").value("Resynced"));

      assertThat(userRepository.findAll()).filteredOn(u -> keycloakId.equals(u.getKeycloakId()))
          .hasSize(1);
    }

    @Test
    @DisplayName("keeps the stale profile when the Keycloak admin API call fails")
    void createUser_shouldKeepStaleProfile_whenKeycloakCallFails() throws Exception {
      final UUID keycloakId = UUID.randomUUID();
      mockMvc.perform(post("/api/users").with(asNewUser(keycloakId, "newbie")));
      flushAndClear();

      when(keycloakService.getUser(any()))
          .thenThrow(new WebApplicationException("service account lacks view-users"));

      mockMvc
          .perform(post("/api/users").with(asNewUser(keycloakId, "newbie")))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.username").value("newbie"));
    }

    @Test
    @DisplayName("returns 400 when the JWT has no subject claim")
    void createUser_shouldReturnBadRequest_whenJwtHasNoSubject() throws Exception {
      mockMvc
          .perform(
              post("/api/users")
                  .with(
                      jwt()
                          .jwt(builder -> builder.subject("anonymous").claim("sub", ""))
                          .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
          .andExpect(status().isBadRequest())
          .andExpect(
              jsonPath("$.message").value(containsString("missing a subject claim")));
    }
  }

  @Nested
  @DisplayName("getUserById")
  class GetUserById {

    @Test
    @DisplayName("returns 200 when the caller requests their own id")
    void getUserById_shouldReturnOk_whenCallerRequestsOwnId() throws Exception {
      mockMvc
          .perform(get("/api/users/{userId}", admin.getId()).with(asUser(admin)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(admin.getId()));
    }

    @Test
    @DisplayName("returns 404 for an unknown user id")
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      mockMvc
          .perform(get("/api/users/{userId}", 999_999L).with(asAdmin(admin)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(containsString("No user with id")));
    }
  }

  @Nested
  @DisplayName("getAllUsers")
  class GetAllUsers {

    @Test
    @DisplayName("returns a page of users sorted by username for an admin caller")
    void getAllUsers_shouldReturnPagedUsers_whenCallerIsAdmin() throws Exception {
      persistUser("Bob", "Jones");
      persistUser("Charlie", "Brown");

      mockMvc
          .perform(get("/api/users").with(asAdmin(admin)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size").value(10))
          .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("filters by username via search")
    void getAllUsers_shouldFilterByUsername_whenSearchProvided() throws Exception {
      final User bob = persistUser("Bob", "Jones");

      mockMvc
          .perform(get("/api/users").param("search", bob.getUsername()).with(asAdmin(admin)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(1))
          .andExpect(jsonPath("$.content[0].username").value(bob.getUsername()));
    }

    @Test
    @DisplayName("returns 403 for a non-admin caller")
    void getAllUsers_shouldReturnForbidden_whenCallerIsNotAdmin() throws Exception {
      mockMvc.perform(get("/api/users").with(asUser(admin))).andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("deleteUser")
  class DeleteUser {

    @Test
    @DisplayName("deletes a user with no memberships or expense history")
    void deleteUser_shouldDeleteUser_whenNoMembershipsOrExpenseHistory() throws Exception {
      final User clean = persistUser("Clean", "User");

      mockMvc
          .perform(delete("/api/users/{userId}", clean.getId()).with(asAdmin(admin)))
          .andExpect(status().isNoContent());

      assertThat(userRepository.findById(clean.getId())).isEmpty();
    }

    @Test
    @DisplayName("returns 409 when the admin tries to delete their own account")
    void deleteUser_shouldReturnConflict_whenDeletingOwnAccount() throws Exception {
      mockMvc
          .perform(delete("/api/users/{userId}", admin.getId()).with(asAdmin(admin)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(containsString("cannot delete their own account")));
    }

    @Test
    @DisplayName("returns 409 when the user has a group membership")
    void deleteUser_shouldReturnConflict_whenUserHasMembership() throws Exception {
      final User member = persistUser("Group", "Member");
      final Group group = persistGroup("Trip");
      persistMember(member, group, MemberRole.MEMBER);

      mockMvc
          .perform(delete("/api/users/{userId}", member.getId()).with(asAdmin(admin)))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.message")
                  .value(containsString("group memberships or expense history")));
    }

    @Test
    @DisplayName("returns 409 when the user has paid an expense")
    void deleteUser_shouldReturnConflict_whenUserPaidAnExpense() throws Exception {
      final User payer = persistUser("Payer", "User");
      final User participantUser = persistUser("Participant", "User");
      final Group group = persistGroup("Trip");
      persistMember(payer, group, MemberRole.OWNER);
      persistMember(participantUser, group, MemberRole.MEMBER);
      persistExpense(
          group,
          payer,
          List.of(participant(participantUser, "10.00")));
      flushAndClear();

      mockMvc
          .perform(delete("/api/users/{userId}", payer.getId()).with(asAdmin(admin)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("returns 409 when the user is only a participant in an expense")
    void deleteUser_shouldReturnConflict_whenUserIsOnlyAParticipant() throws Exception {
      final User payer = persistUser("Payer", "User");
      final User participantUser = persistUser("Participant", "User");
      final Group group = persistGroup("Trip");
      persistMember(payer, group, MemberRole.OWNER);
      persistMember(participantUser, group, MemberRole.MEMBER);
      persistExpense(
          group,
          payer,
          List.of(participant(participantUser, "10.00")));
      flushAndClear();
      // remove the participant's membership so only expense-participation remains
      memberRepository.findByGroupIdAndUserId(group.getId(), participantUser.getId())
          .ifPresent(memberRepository::delete);
      flushAndClear();

      mockMvc
          .perform(delete("/api/users/{userId}", participantUser.getId()).with(asAdmin(admin)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("returns 404 for an unknown user id")
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
      mockMvc
          .perform(delete("/api/users/{userId}", 999_999L).with(asAdmin(admin)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 403 for a non-admin caller")
    void deleteUser_shouldReturnForbidden_whenCallerIsNotAdmin() throws Exception {
      final User clean = persistUser("Clean", "User");

      mockMvc
          .perform(delete("/api/users/{userId}", clean.getId()).with(asUser(admin)))
          .andExpect(status().isForbidden());
    }
  }
}
