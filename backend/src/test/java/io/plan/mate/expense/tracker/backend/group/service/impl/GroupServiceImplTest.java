package io.plan.mate.expense.tracker.backend.group.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.group.service.dto.GroupDto;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupServiceImpl")
class GroupServiceImplTest {

  @Mock private GroupRepository groupRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private SettlementService settlementService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private GroupServiceImpl groupService;

  @Nested
  @DisplayName("deleteGroup")
  class DeleteGroup {

    @Test
    @DisplayName("throws ResourceNotFoundException when group does not exist")
    void deleteGroup_shouldThrowResourceNotFoundException_whenGroupDoesNotExist() {
      when(groupRepository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.deleteGroup(1L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("1");

      verify(settlementService, never()).clearSettlementCache(any());
      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("clears the settlement cache and publishes a SettlementsChangedEvent when group is deleted")
    void deleteGroup_shouldClearSettlementCacheAndPublishEvent_whenGroupDeleted() {
      Group group = Group.builder().id(1L).name("Trip").build();
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(modelMapper.map(group, GroupDto.class)).thenReturn(GroupDto.builder().id(1L).build());

      groupService.deleteGroup(1L);

      verify(settlementService).clearSettlementCache(1L);
      verify(groupRepository).delete(group);
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }
}
