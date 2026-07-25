package io.plan.mate.expense.tracker.backend.group.jpa.repository;

import static io.plan.mate.expense.tracker.backend.commons.utils.SettlementTestBuilders.newUser;
import static org.assertj.core.api.Assertions.assertThat;

import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.Member;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("GroupRepository integration tests")
class GroupRepositoryIntegrationTest {

  @MockitoBean @SuppressWarnings("unused") private JwtDecoder jwtDecoder;

  @Autowired private GroupRepository groupRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EntityManager entityManager;

  @BeforeEach
  void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName(
      "searchForMember does not throw when sorted by a Group-only field such as \"name\", "
          + "the GroupController's default sort")
  void searchForMember_shouldNotThrow_whenSortedByGroupOnlyField() {
    User alice = userRepository.save(newUser("Alice", "Smith"));
    Group group = groupRepository.save(Group.builder().name("Trip").build());
    memberRepository.save(
        Member.builder()
            .user(alice)
            .group(group)
            .role(MemberRole.MEMBER)
            .joinedAt(LocalDateTime.now())
            .build());
    entityManager.flush();
    entityManager.clear();

    Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));

    assertThat(groupRepository.searchForMember(alice.getId(), null, pageable).getContent())
        .extracting(Group::getName)
        .containsExactly("Trip");
  }
}
