package io.plan.mate.expense.tracker.backend.commons.websocket;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.newUser;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseChangeEnum;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseChangedEvent;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangeEnum;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangeEnum;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.user.service.keycloak.KeycloakService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Exercises the mutation -&gt; domain event -&gt; {@link WebSocketEventListener} -&gt; {@link
 * WebSocketEventPublisher} chain at the Spring-context level (a mocked {@link
 * SimpMessagingTemplate}, no real STOMP client). Deliberately NOT {@code @Transactional}: {@code
 * @TransactionalEventListener(AFTER_COMMIT)} only fires on a real commit, so each test cleans up
 * its own data in {@link #tearDown()} instead of relying on rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WebSocket event publishing integration tests")
class WebSocketEventIntegrationTest {

  @MockitoBean @SuppressWarnings("unused") private JwtDecoder jwtDecoder;
  @MockitoBean @SuppressWarnings("unused") private KeycloakService keycloakService;
  @MockitoBean private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ExpenseRepository expenseRepository;

  private User owner;
  private User member;
  private Group group;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(newUser("Olivia", "Owner"));
    member = userRepository.save(newUser("Mia", "Member"));
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

  @AfterEach
  void tearDown() {
    groupRepository.deleteById(group.getId());
    userRepository.delete(owner);
    userRepository.delete(member);
  }

  private static RequestPostProcessor asUser(final User user) {
    return jwt()
        .jwt(builder -> builder.subject(user.getKeycloakId().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  private String expensePayload(final String amount, final String payer, final String participant) {
    return """
        {
          "description": "Dinner",
          "amount": %s,
          "paidByUsername": "%s",
          "participants": [{"userName": "%s", "shareAmount": %s}]
        }
        """
        .formatted(amount, payer, participant, amount);
  }

  @Test
  @DisplayName("creating an expense publishes ADD_EXPENSE and SETTLEMENTS_INVALIDATED after commit")
  void createExpense_shouldPublishExpenseAndSettlementsEvents_afterCommit() throws Exception {
    mockMvc.perform(
        post("/api/groups/{groupId}/expenses", group.getId())
            .with(asUser(owner))
            .contentType(MediaType.APPLICATION_JSON)
            .content(expensePayload("20.00", owner.getUsername(), member.getUsername())));

    verify(simpMessagingTemplate)
        .convertAndSend(
            eq("/topic/groups/" + group.getId() + "/expenses"),
            org.mockito.ArgumentMatchers.argThat(
                (ExpenseChangedEvent event) -> event.changeType() == ExpenseChangeEnum.ADD_EXPENSE));
    verify(simpMessagingTemplate)
        .convertAndSend(
            eq("/topic/groups/" + group.getId() + "/settlements"),
            org.mockito.ArgumentMatchers.argThat(
                (SettlementsChangedEvent event) ->
                    event.changeType() == SettlementsChangeEnum.SETTLEMENTS_INVALIDATED));
  }

  @Test
  @DisplayName("updating an expense publishes EDIT_EXPENSE after commit")
  void updateExpense_shouldPublishEditExpenseEvent_afterCommit() throws Exception {
    final Expense expense =
        expenseRepository.save(
            Expense.builder()
                .group(group)
                .paidBy(owner)
                .amount(new java.math.BigDecimal("20.00"))
                .build());

    mockMvc.perform(
        put("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
            .with(asUser(owner))
            .contentType(MediaType.APPLICATION_JSON)
            .content(expensePayload("30.00", owner.getUsername(), member.getUsername())));

    verify(simpMessagingTemplate)
        .convertAndSend(
            eq("/topic/groups/" + group.getId() + "/expenses"),
            org.mockito.ArgumentMatchers.argThat(
                (ExpenseChangedEvent event) ->
                    event.changeType() == ExpenseChangeEnum.EDIT_EXPENSE));
  }

  @Test
  @DisplayName("deleting an expense publishes DELETE_EXPENSE with a fully-populated DTO after commit")
  void deleteExpense_shouldPublishDeleteExpenseEvent_afterCommit() throws Exception {
    final Expense expense =
        expenseRepository.save(
            Expense.builder()
                .group(group)
                .paidBy(owner)
                .amount(new java.math.BigDecimal("20.00"))
                .build());

    mockMvc.perform(
        delete("/api/groups/{groupId}/expenses/{expenseId}", group.getId(), expense.getId())
            .with(asUser(owner)));

    verify(simpMessagingTemplate)
        .convertAndSend(
            eq("/topic/groups/" + group.getId() + "/expenses"),
            org.mockito.ArgumentMatchers.argThat(
                (ExpenseChangedEvent event) ->
                    event.changeType() == ExpenseChangeEnum.DELETE_EXPENSE
                        && event.expense().getId().equals(expense.getId())));
  }

  @Test
  @DisplayName("adding a member publishes ADD_MEMBER after commit")
  void addMember_shouldPublishAddMemberEvent_afterCommit() throws Exception {
    final User outsider = userRepository.save(newUser("Otto", "Outsider"));
    try {
      mockMvc.perform(
          post("/api/groups/{groupId}/users", group.getId())
              .with(asUser(owner))
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"username\": \"%s\"}".formatted(outsider.getUsername())));

      verify(simpMessagingTemplate)
          .convertAndSend(
              eq("/topic/groups/" + group.getId() + "/users"),
              org.mockito.ArgumentMatchers.argThat(
                  (MemberChangedEvent event) -> event.changeType() == MemberChangeEnum.ADD_MEMBER));
    } finally {
      memberRepository
          .findByGroupIdAndUserId(group.getId(), outsider.getId())
          .ifPresent(memberRepository::delete);
      userRepository.delete(outsider);
    }
  }

  @Test
  @DisplayName("removing a member publishes REMOVE_MEMBER after commit")
  void removeMember_shouldPublishRemoveMemberEvent_afterCommit() throws Exception {
    final Member membership =
        memberRepository.findByGroupIdAndUserId(group.getId(), member.getId()).orElseThrow();

    mockMvc.perform(
        delete("/api/groups/{groupId}/users/{memberId}", group.getId(), membership.getId())
            .with(asUser(owner)));

    verify(simpMessagingTemplate)
        .convertAndSend(
            eq("/topic/groups/" + group.getId() + "/users"),
            org.mockito.ArgumentMatchers.argThat(
                (MemberChangedEvent event) ->
                    event.changeType() == MemberChangeEnum.REMOVE_MEMBER));
  }

  @Test
  @DisplayName("a service-level failure rolls back the transaction and publishes nothing")
  void createExpense_shouldPublishNothing_whenServiceThrowsAndTransactionRollsBack()
      throws Exception {
    // Share total (5.00) does not equal the expense amount (20.00): BadRequestException thrown
    // inside the @Transactional service method, so Spring marks the transaction rollback-only.
    mockMvc.perform(
        post("/api/groups/{groupId}/expenses", group.getId())
            .with(asUser(owner))
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {
                  "description": "Dinner",
                  "amount": 20.00,
                  "paidByUsername": "%s",
                  "participants": [{"userName": "%s", "shareAmount": 5.00}]
                }
                """
                    .formatted(owner.getUsername(), member.getUsername())));

    verifyNoInteractions(simpMessagingTemplate);
  }
}
