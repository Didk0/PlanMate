package io.plan.mate.expense.tracker.backend.commons.exception.handling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.commons.utils.AbstractIntegrationTest;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.service.GroupService;
import io.plan.mate.expense.tracker.backend.member.jpa.entity.MemberRole;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("GlobalExceptionHandler integration tests")
class GlobalExceptionHandlerIntegrationTest extends AbstractIntegrationTest {

  @MockitoBean private GroupService groupService;

  private User owner;
  private Group group;

  @BeforeEach
  void setUp() {
    owner = persistUser("Olivia", "Owner");
    group = persistGroup("Trip");
    persistMember(owner, group, MemberRole.OWNER);
  }

  @Test
  @DisplayName("an unhandled RuntimeException maps to 500 with a fixed, non-leaking message")
  void unhandledException_shouldReturnInternalServerError_withFixedMessage() throws Exception {
    when(groupService.getGroupById(group.getId()))
        .thenThrow(new RuntimeException("some sensitive internal detail"));

    mockMvc
        .perform(get("/api/groups/{groupId}", group.getId()).with(asUser(owner)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
            org.hamcrest.Matchers.containsString("sensitive internal detail"))));
  }

  @Test
  @DisplayName("a DataIntegrityViolationException maps to 409 with a fixed message")
  void dataIntegrityViolation_shouldReturnConflict_withFixedMessage() throws Exception {
    doThrow(new DataIntegrityViolationException("duplicate key"))
        .when(groupService)
        .deleteGroup(group.getId());

    mockMvc
        .perform(delete("/api/groups/{groupId}", group.getId()).with(asUser(owner)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("Request conflicts with existing data"));
  }

  @Test
  @DisplayName("the ApiError body carries timestamp, status, error, message and path")
  void apiError_shouldIncludeAllFields_forAResourceNotFoundException() throws Exception {
    when(groupService.getGroupById(any()))
        .thenThrow(new ResourceNotFoundException("Group with id=999 not found"));

    mockMvc
        .perform(get("/api/groups/{groupId}", group.getId()).with(asUser(owner)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").isNotEmpty())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not found"))
        .andExpect(jsonPath("$.message").value("Group with id=999 not found"))
        .andExpect(jsonPath("$.path").value("/api/groups/" + group.getId()));
  }

  @Test
  @DisplayName("a non-numeric path variable falls through to the generic 500 handler")
  void nonNumericPathVariable_shouldReturnInternalServerError() throws Exception {
    mockMvc
        .perform(get("/api/groups/{groupId}", "abc").with(asAdmin(owner)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value("Internal Server Error"));
  }
}
