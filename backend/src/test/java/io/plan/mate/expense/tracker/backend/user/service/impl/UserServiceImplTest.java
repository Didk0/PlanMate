package io.plan.mate.expense.tracker.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ConflictException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.user.service.keycloak.KeycloakService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private ExpenseRepository expenseRepository;
  @Mock private ModelMapper modelMapper;
  @Mock private KeycloakService keycloakService;

  @InjectMocks private UserServiceImpl userService;

  @Nested
  @DisplayName("deleteUser")
  class DeleteUser {

    @Test
    @DisplayName("throws ResourceNotFoundException when user does not exist")
    void deleteUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
      when(userRepository.findById(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userService.deleteUser(1L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("1");

      verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws ConflictException when user has group memberships")
    void deleteUser_shouldThrowConflictException_whenUserHasMemberships() {
      User user = User.builder().id(1L).build();
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(memberRepository.existsByUserId(1L)).thenReturn(true);

      assertThatThrownBy(() -> userService.deleteUser(1L)).isInstanceOf(ConflictException.class);

      verify(userRepository, never()).delete(user);
    }

    @Test
    @DisplayName("throws ConflictException when user has expense history")
    void deleteUser_shouldThrowConflictException_whenUserHasExpenseHistory() {
      User user = User.builder().id(1L).build();
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(memberRepository.existsByUserId(1L)).thenReturn(false);
      when(expenseRepository.existsByPaidById(1L)).thenReturn(true);

      assertThatThrownBy(() -> userService.deleteUser(1L)).isInstanceOf(ConflictException.class);

      verify(userRepository, never()).delete(user);
    }

    @Test
    @DisplayName("deletes the user when no memberships or expense history exist")
    void deleteUser_shouldDeleteUser_whenNoMembershipsOrExpensesExist() {
      User user = User.builder().id(1L).build();
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(memberRepository.existsByUserId(1L)).thenReturn(false);
      when(expenseRepository.existsByPaidById(1L)).thenReturn(false);
      when(expenseRepository.existsByParticipants_ParticipantId(1L)).thenReturn(false);

      userService.deleteUser(1L);

      verify(userRepository).delete(user);
    }
  }
}
