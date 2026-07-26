package io.plan.mate.expense.tracker.backend.group.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DisplayName("GroupRepository integration tests")
class GroupRepositoryIntegrationTest extends AbstractIntegrationTest {

  @Test
  @DisplayName(
      "searchForMember does not throw when sorted by a Group-only field such as \"name\", "
          + "the GroupController's default sort")
  void searchForMember_shouldNotThrow_whenSortedByGroupOnlyField() {
    User alice = persistUser("Alice", "Smith");
    Group group = persistGroup("Trip");
    persistMember(alice, group);
    flushAndClear();

    Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));

    assertThat(groupRepository.searchForMember(alice.getId(), null, pageable).getContent())
        .extracting(Group::getName)
        .containsExactly("Trip");
  }
}
