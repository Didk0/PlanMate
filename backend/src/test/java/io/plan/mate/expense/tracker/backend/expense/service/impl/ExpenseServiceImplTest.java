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
import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseChangedEvent;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseParticipant;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.CreateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.controller.payload.request.UpdateExpenseRequest;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.Expense;
import io.plan.mate.expense.tracker.backend.expense.jpa.entity.ExpenseParticipant;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    @DisplayName("publishes an ExpenseChangedEvent and a SettlementsChangedEvent after saving")
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

      verify(eventPublisher).publishEvent(any(ExpenseChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }

  @Nested
  @DisplayName("updateExpense")
  class UpdateExpense {

    private Expense existingExpense(final Group group) {
      Expense expense =
          Expense.builder()
              .id(10L)
              .description("Old description")
              .amount(new BigDecimal("20.00"))
              .group(group)
              .paidBy(user(1L, "alice"))
              .build();
      expense.setParticipants(new ArrayList<>(List.of(
          ExpenseParticipant.builder()
              .expense(expense)
              .participant(user(2L, "bob"))
              .shareAmount(new BigDecimal("20.00"))
              .build())));
      return expense;
    }

    @Test
    @DisplayName("throws ResourceNotFoundException when expense does not belong to the group")
    void updateExpense_shouldThrowResourceNotFoundException_whenExpenseNotInGroup() {
      Group otherGroup = Group.builder().id(2L).name("Other").build();
      Expense expense = existingExpense(otherGroup);
      when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));

      UpdateExpenseRequest request =
          new UpdateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("30.00"))));

      assertThatThrownBy(() -> expenseService.updateExpense(1L, 10L, request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("10");

      verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws BadRequestException when share sum does not equal the expense amount")
    void updateExpense_shouldThrowBadRequestException_whenShareSumDoesNotEqualAmount() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Expense expense = existingExpense(group);
      when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));

      UpdateExpenseRequest request =
          new UpdateExpenseRequest(
              "Dinner",
              new BigDecimal("30.00"),
              "alice",
              List.of(new CreateExpenseParticipant("bob", new BigDecimal("20.00"))));

      assertThatThrownBy(() -> expenseService.updateExpense(1L, 10L, request))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("30.00")
          .hasMessageContaining("20.00");

      verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("replaces participants, updates fields, and evicts the settlement cache")
    void updateExpense_shouldReplaceParticipantsAndEvictCache_whenValid() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Expense expense = existingExpense(group);
      User alice = user(1L, "alice");
      User carol = user(3L, "carol");
      when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));
      when(userRepository.findByUsernameIn(any())).thenReturn(List.of(alice, carol));
      when(memberRepository.findMemberUserIdsByGroupIdAndUserIdIn(eq(1L), any()))
          .thenReturn(Set.of(1L, 3L));
      when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(modelMapper.map(any(Expense.class), eq(ExpenseDto.class)))
          .thenReturn(ExpenseDto.builder().build());

      UpdateExpenseRequest request =
          new UpdateExpenseRequest(
              "Updated dinner",
              new BigDecimal("40.00"),
              "alice",
              List.of(new CreateExpenseParticipant("carol", new BigDecimal("40.00"))));

      expenseService.updateExpense(1L, 10L, request);

      ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
      verify(expenseRepository).save(captor.capture());
      Expense saved = captor.getValue();

      assertThat(saved.getDescription()).isEqualTo("Updated dinner");
      assertThat(saved.getAmount()).isEqualByComparingTo("40.00");
      assertThat(saved.getParticipants()).hasSize(1);
      assertThat(saved.getParticipants().getFirst().getParticipant().getUsername())
          .isEqualTo("carol");

      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(ExpenseChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }

  @Nested
  @DisplayName("deleteExpense")
  class DeleteExpense {

    @Test
    @DisplayName("throws ResourceNotFoundException when expense does not exist")
    void deleteExpense_shouldThrowResourceNotFoundException_whenExpenseDoesNotExist() {
      when(expenseRepository.findById(10L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> expenseService.deleteExpense(1L, 10L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("10");

      verify(expenseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deletes the expense, evicts the cache, and publishes events")
    void deleteExpense_shouldDeleteAndPublishEvents_whenExists() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Expense expense =
          Expense.builder()
              .id(10L)
              .description("Dinner")
              .amount(new BigDecimal("20.00"))
              .group(group)
              .paidBy(user(1L, "alice"))
              .build();
      when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));
      when(modelMapper.map(any(Expense.class), eq(ExpenseDto.class)))
          .thenReturn(ExpenseDto.builder().build());

      expenseService.deleteExpense(1L, 10L);

      verify(expenseRepository).delete(expense);
      verify(settlementService).clearSettlementCache(1L);
      verify(eventPublisher).publishEvent(any(ExpenseChangedEvent.class));
      verify(eventPublisher).publishEvent(any(SettlementsChangedEvent.class));
    }
  }

  @Nested
  @DisplayName("getGroupExpenses")
  class GetGroupExpenses {

    @Test
    @DisplayName("returns paged expenses in the order returned by the id page, using a requested sort plus an id tiebreaker")
    void getGroupExpenses_shouldReturnPagedExpensesInIdPageOrder_whenGroupHasExpenses() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Expense older =
          Expense.builder()
              .id(1L)
              .group(group)
              .amount(new BigDecimal("10.00"))
              .createdAt(LocalDateTime.now().minusDays(1))
              .build();
      Expense newer =
          Expense.builder()
              .id(2L)
              .group(group)
              .amount(new BigDecimal("20.00"))
              .createdAt(LocalDateTime.now())
              .build();
      Pageable requestedPageable =
          PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
      Pageable expectedPageable =
          PageRequest.of(
              0,
              10,
              Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id")));

      when(expenseRepository.findExpenseIdsByGroupId(1L, null, expectedPageable))
          .thenReturn(new PageImpl<>(List.of(2L, 1L), expectedPageable, 2));
      when(expenseRepository.findByIdIn(List.of(2L, 1L))).thenReturn(List.of(older, newer));
      when(modelMapper.map(older, ExpenseDto.class)).thenReturn(ExpenseDto.builder().id(1L).build());
      when(modelMapper.map(newer, ExpenseDto.class)).thenReturn(ExpenseDto.builder().id(2L).build());

      PagedResponse<ExpenseDto> result =
          expenseService.getGroupExpenses(1L, null, requestedPageable);

      assertThat(result.content()).extracting(ExpenseDto::getId).containsExactly(2L, 1L);
      assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("passes a trimmed search term to the repository when a search is provided")
    void getGroupExpenses_shouldSearchTrimmed_whenSearchProvided() {
      Group group = Group.builder().id(1L).name("Trip").build();
      Expense expense =
          Expense.builder()
              .id(1L)
              .group(group)
              .description("Dinner")
              .amount(new BigDecimal("10.00"))
              .createdAt(LocalDateTime.now())
              .build();
      Pageable requestedPageable = PageRequest.of(0, 10);
      Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

      when(expenseRepository.findExpenseIdsByGroupId(1L, "dinner", expectedPageable))
          .thenReturn(new PageImpl<>(List.of(1L), expectedPageable, 1));
      when(expenseRepository.findByIdIn(List.of(1L))).thenReturn(List.of(expense));
      when(modelMapper.map(expense, ExpenseDto.class))
          .thenReturn(ExpenseDto.builder().id(1L).build());

      PagedResponse<ExpenseDto> result =
          expenseService.getGroupExpenses(1L, "  dinner  ", requestedPageable);

      assertThat(result.content()).extracting(ExpenseDto::getId).containsExactly(1L);
      verify(expenseRepository).findExpenseIdsByGroupId(1L, "dinner", expectedPageable);
    }

    @Test
    @DisplayName("returns an empty page when group has no expenses")
    void getGroupExpenses_shouldReturnEmptyPage_whenGroupHasNoExpenses() {
      Pageable requestedPageable = PageRequest.of(0, 10);
      Pageable expectedPageable =
          PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
      when(expenseRepository.findExpenseIdsByGroupId(1L, null, expectedPageable))
          .thenReturn(new PageImpl<>(List.of(), expectedPageable, 0));
      when(expenseRepository.findByIdIn(List.of())).thenReturn(List.of());

      PagedResponse<ExpenseDto> result =
          expenseService.getGroupExpenses(1L, null, requestedPageable);

      assertThat(result.content()).isEmpty();
      assertThat(result.totalElements()).isZero();
    }
  }
}
