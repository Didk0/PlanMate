package io.plan.mate.expense.tracker.backend.expense.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseCreatedEvent;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.expense.service.dto.ExpenseDto;
import io.plan.mate.expense.tracker.backend.group.jpa.entity.Group;
import io.plan.mate.expense.tracker.backend.group.jpa.repository.GroupRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.settlement.service.SettlementService;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseServiceImpl")
class ExpenseServiceImplTest {

  @Mock private ExpenseRepository expenseRepository;
  @Mock private UserRepository userRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private SettlementService settlementService;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ExpenseServiceImpl expenseService;

  private User user(final Long id, final String username) {
    return User.builder().id(id).username(username).build();
  }

  @Nested
  @DisplayName("createExpense")
  class CreateExpense {

    @Test
    @DisplayName("throws ResourceNotFoundException when group does not exist")
    void createExpense_shouldThrowResourceNotFoundException_whenGroupDoesNotExist() {
      when(groupRepository.findById(1L)).thenReturn(Optional.empty());

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      assertThatThrownBy(() -> expenseService.createExpense(1L, request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("1");

      verify(userRepository, never()).findByUsernameIn(any());
    }

    @Test
    @DisplayName("throws BadRequestException when share sum does not equal the expense amount")
    void createExpense_shouldThrowBadRequestException_whenShareSumDoesNotEqualAmount() {
      Group group = Group.builder().id(1L).name("Trip").build();
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("20.00"))));

      assertThatThrownBy(() -> expenseService.createExpense(1L, request))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("30.00")
          .hasMessageContaining("20.00");

      verify(userRepository, never()).findByUsernameIn(any());
      verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws ResourceNotFoundException listing missing usernames")
    void createExpense_shouldThrowResourceNotFoundException_whenAUserIsMissing() {
      Group group = Group.builder().id(1L).name("Trip").build();
      User alice = user(1L, "alice");
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice));

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      assertThatThrownBy(() -> expenseService.createExpense(1L, request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("bob");

      verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws BadRequestException naming the payer when they are not a group member")
    void createExpense_shouldThrowBadRequestException_whenPayerIsNotAGroupMember() {
      Group group = Group.builder().id(1L).name("Trip").build();
      User alice = user(1L, "alice");
      User bob = user(2L, "bob");
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice, bob));
      when(memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(eq(1L), any()))
          .thenReturn(Set.of(2L));

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      assertThatThrownBy(() -> expenseService.createExpense(1L, request))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("alice");

      verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "throws BadRequestException naming the participant when they are not a group member")
    void createExpense_shouldThrowBadRequestException_whenParticipantIsNotAGroupMember() {
      Group group = Group.builder().id(1L).name("Trip").build();
      User alice = user(1L, "alice");
      User bob = user(2L, "bob");
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice, bob));
      when(memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(eq(1L), any()))
          .thenReturn(Set.of(1L));

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      assertThatThrownBy(() -> expenseService.createExpense(1L, request))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("bob");

      verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("looks up all participants and the payer in a single query")
    void createExpense_shouldLookUpUsersInASingleQuery_whenCreatingExpense() {
      Group group = Group.builder().id(1L).name("Trip").build();
      User alice = user(1L, "alice");
      User bob = user(2L, "bob");
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice, bob));
      when(memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(eq(1L), any()))
          .thenReturn(Set.of(1L, 2L));
      when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Expense.class), eq(ExpenseDto.class)))
          .thenReturn(ExpenseDto.builder().build());

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      expenseService.createExpense(1L, request);

      verify(userRepository).findByUsernameIn(any());

      ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
      verify(expenseRepository).save(captor.capture());
      Expense saved = captor.getValue();

      assertThat(saved.getPaidBy().getUsername()).isEqualTo("alice");
      assertThat(saved.getParticipants()).hasSize(1);
      assertThat(saved.getParticipants().getFirst().getParticipant().getUsername())
          .isEqualTo("bob");
    }

    @Test
    @DisplayName("evicts the settlement cache for the group after saving")
    void createExpense_shouldEvictSettlementCache_whenExpenseIsSaved() {
      Group group = Group.builder().id(1L).name("Trip").build();
      User alice = user(1L, "alice");
      User bob = user(2L, "bob");
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice, bob));
      when(memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(eq(1L), any()))
          .thenReturn(Set.of(1L, 2L));
      when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Expense.class), eq(ExpenseDto.class)))
          .thenReturn(ExpenseDto.builder().build());

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      expenseService.createExpense(1L, request);

      verify(settlementService).clearSettlementCache(1L);
    }

    @Test
    @DisplayName("publishes an ExpenseCreatedEvent and a SettlementsChangedEvent after saving")
    void createExpense_shouldPublishExpenseAndSettlementsEvents_whenExpenseIsSaved() {
      Group group = Group.builder().id(1L).name("Trip").build();
      User alice = user(1L, "alice");
      User bob = user(2L, "bob");
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice, bob));
      when(memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(eq(1L), any()))
          .thenReturn(Set.of(1L, 2L));
      when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Expense.class), eq(ExpenseDto.class)))
          .thenReturn(ExpenseDto.builder().build());

      CreateExpenseRequest request =
          new CreateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      expenseService.createExpense(1L, request);

      verify(eventPublisher).publishEvent(any(ExpenseCreatedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }
}
