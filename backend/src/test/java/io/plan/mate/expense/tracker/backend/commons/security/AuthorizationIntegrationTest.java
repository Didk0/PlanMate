package io.plan.mate.expense.tracker.backend.commons.security;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.newUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Endpoint authorization")
class AuthorizationIntegrationTest {

  @MockitoBean @SuppressWarnings("unused") private JwtDecoder jwtDecoder;

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private MemberRepository memberRepository;

  private User owner;
  private User member;
  private User outsider;
  private Group group;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(newUser("Olivia", "Owner"));
    member = userRepository.save(newUser("Mia", "Member"));
    outsider = userRepository.save(newUser("Otto", "Outsider"));

    group = groupRepository.save(Group.builder().name("Trip").build());

    memberRepository.save(
        Member.builder()
            .user(owner)
            .group(group)
            .role(MemberRole.OWNER)
            .joinedAt(LocalDateTime.now())
            .build());
    memberRepository.save(
        Member.builder()
            .user(member)
            .group(group)
            .role(MemberRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build());
  }

  private static RequestPostProcessor asUser(final User user) {
    return jwt()
        .jwt(builder -> builder.subject(user.getKeycloakId().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  private static RequestPostProcessor asAdmin(final User user) {
    return jwt()
        .jwt(builder -> builder.subject(user.getKeycloakId().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
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
}
