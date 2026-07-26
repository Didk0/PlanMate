package io.plan.mate.expense.tracker.backend.commons.utils;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.newUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.ExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.user.service.keycloak.KeycloakService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

  @MockitoBean @SuppressWarnings("unused") protected JwtDecoder jwtDecoder;

  @MockitoBean protected KeycloakService keycloakService;

  @Autowired protected MockMvc mockMvc;
  @Autowired protected CacheManager cacheManager;
  @Autowired protected EntityManager entityManager;
  @Autowired protected UserRepository userRepository;
  @Autowired protected GroupRepository groupRepository;
  @Autowired protected MemberRepository memberRepository;
  @Autowired protected ExpenseRepository expenseRepository;

  @BeforeEach
  void clearSettlementsCache() {
    Objects.requireNonNull(cacheManager.getCache("settlements")).clear();
  }

  @BeforeEach
  void stubKeycloakUserLookup() {
    lenient().when(keycloakService.getUser(any())).thenAnswer(
            _ -> {
          final UserRepresentation representation = new UserRepresentation();
          representation.setUsername("synced-username");
          representation.setEmail("synced@test.com");
          representation.setFirstName("Synced");
          representation.setLastName("User");
          return representation;
        });
  }

  protected void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  protected User persistUser(final String first, final String last) {
    return userRepository.save(newUser(first, last));
  }

  protected Group persistGroup(final String name) {
    return groupRepository.save(Group.builder().name(name).build());
  }

  protected Member persistMember(final User user, final Group group, final MemberRole role) {
    return memberRepository.save(
        Member.builder().user(user).group(group).role(role).joinedAt(LocalDateTime.now()).build());
  }

  protected Member persistMember(final User user, final Group group) {
    return persistMember(user, group, MemberRole.MEMBER);
  }

  protected Expense persistExpense(
      final Group group, final User paidBy, final List<ExpenseParticipant> participants) {
    final BigDecimal total =
        participants.stream()
            .map(ExpenseParticipant::getShareAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    final Expense expense = Expense.builder().group(group).paidBy(paidBy).amount(total).build();
    participants.forEach(p -> p.setExpense(expense));
    expense.setParticipants(new ArrayList<>(participants));
    return expenseRepository.save(expense);
  }

  protected static RequestPostProcessor asUser(final User user) {
    return SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(builder -> builder.subject(user.getKeycloakId().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }

  protected static RequestPostProcessor asAdmin(final User user) {
    return SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(builder -> builder.subject(user.getKeycloakId().toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
  }

  protected static RequestPostProcessor asUnprovisioned() {
    final UUID unprovisionedKeycloakId = UUID.randomUUID();
    return SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(builder -> builder.subject(unprovisionedKeycloakId.toString()))
        .authorities(new SimpleGrantedAuthority("ROLE_USER"));
  }
}
