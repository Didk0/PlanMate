package io.plan.mate.expense.tracker.backend.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ConflictException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.commons.security.CurrentUserService;
import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import io.plan.mate.expense.tracker.backend.user.service.keycloak.KeycloakService;
import io.plan.mate.expense.tracker.backend.user.service.mapper.UserMapperImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private MemberRepository memberRepository;
  @Mock private ExpenseRepository expenseRepository;
  @Spy private UserMapperImpl userMapper;
  @Mock private KeycloakService keycloakService;
  @Mock private CurrentUserService currentUserService;

  @InjectMocks private UserServiceImpl userService;

  private static Jwt jwtFor(final UUID subject) {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject(subject.toString())
        .claim("preferred_username", "alice")
        .claim("email", "alice@example.com")
        .claim("given_name", "Alice")
        .claim("family_name", "Doe")
        .build();
  }

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
    @DisplayName("throws ConflictException when deleting own account")
    void deleteUser_shouldThrowConflictException_whenDeletingOwnAccount() {
      User user = User.builder().id(1L).build();
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(currentUserService.requireCurrentUserId()).thenReturn(1L);

      assertThatThrownBy(() -> userService.deleteUser(1L)).isInstanceOf(ConflictException.class);

      verify(userRepository, never()).delete(user);
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

  @Nested
  @DisplayName("provisionCurrentUser")
  class ProvisionCurrentUser {

    @Test
    @DisplayName("uses the keycloak id from the JWT, not a client-supplied value")
    void provisionCurrentUser_shouldUseKeycloakIdFromTheJwt_whenRequestBodyIsIgnored() {
      UUID keycloakId = UUID.randomUUID();
      Jwt jwt = jwtFor(keycloakId);

      when(currentUserService.requireCurrentJwt()).thenReturn(jwt);
      when(currentUserService.getKeycloakId()).thenReturn(keycloakId);
      when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
      when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

      userService.provisionCurrentUser();

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());

      assertThat(userCaptor.getValue().getKeycloakId()).isEqualTo(keycloakId);
      assertThat(userCaptor.getValue().getUsername()).isEqualTo("alice");
      assertThat(userCaptor.getValue().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("returns the existing user without creating a duplicate when already provisioned")
    void provisionCurrentUser_shouldReturnExistingUser_whenAlreadyProvisioned() {
      UUID keycloakId = UUID.randomUUID();
      Jwt jwt = jwtFor(keycloakId);
      User existingUser = User.builder().id(1L).keycloakId(keycloakId).username("alice").build();

      when(currentUserService.requireCurrentJwt()).thenReturn(jwt);
      when(currentUserService.getKeycloakId()).thenReturn(keycloakId);
      when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existingUser));
      when(keycloakService.getUser(keycloakId))
          .thenThrow(new jakarta.ws.rs.WebApplicationException("service account lacks view-users"));

      userService.provisionCurrentUser();

      verify(userRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("getAllUsers")
  class GetAllUsers {

    @Test
    @DisplayName("returns a paged response mapped from the requested page")
    void getAllUsers_shouldReturnPagedUsers_whenUsersExist() {
      User alice = User.builder().id(1L).username("alice").build();
      Pageable pageable = PageRequest.of(0, 10);
      when(userRepository.search(null, pageable))
          .thenReturn(new PageImpl<>(List.of(alice), pageable, 1));

      PagedResponse<UserDto> result = userService.getAllUsers(null, pageable);

      assertThat(result.content()).hasSize(1);
      assertThat(result.totalElements()).isEqualTo(1);
      assertThat(result.page()).isZero();
    }

    @Test
    @DisplayName("passes a trimmed search term to the repository when a search is provided")
    void getAllUsers_shouldSearchTrimmed_whenSearchProvided() {
      User alice = User.builder().id(1L).username("alice").build();
      Pageable pageable = PageRequest.of(0, 10);
      when(userRepository.search("ali", pageable))
          .thenReturn(new PageImpl<>(List.of(alice), pageable, 1));

      PagedResponse<UserDto> result = userService.getAllUsers("  ali  ", pageable);

      assertThat(result.content()).hasSize(1);
      verify(userRepository).search("ali", pageable);
    }
  }
}
