package io.plan.mate.expense.tracker.backend.user.service.impl;

import io.plan.mate.expense.tracker.backend.commons.service.dto.PagedResponse;
import io.plan.mate.expense.tracker.backend.user.service.dto.UserDto;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ConflictException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.commons.security.CurrentUserService;
import io.plan.mate.expense.tracker.backend.commons.util.LikePatternEscaper;
import io.plan.mate.expense.tracker.backend.expense.jpa.repository.ExpenseRepository;
import io.plan.mate.expense.tracker.backend.member.jpa.repository.MemberRepository;
import io.plan.mate.expense.tracker.backend.user.service.UserService;
import io.plan.mate.expense.tracker.backend.user.service.keycloak.KeycloakService;
import io.plan.mate.expense.tracker.backend.user.service.mapper.UserMapper;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final ExpenseRepository expenseRepository;
  private final UserMapper userMapper;
  private final KeycloakService keycloakService;
  private final CurrentUserService currentUserService;

  @Override
  @Transactional
  public UserDto provisionCurrentUser() {

    final Jwt jwt = currentUserService.requireCurrentJwt();
    final UUID keycloakId = currentUserService.getKeycloakId();

    final User existingUser = userRepository.findByKeycloakId(keycloakId).orElse(null);

    if (existingUser != null) {

      try {
        final UserRepresentation userRepresentation = keycloakService.getUser(keycloakId);

        userMapper.updateFromKeycloak(existingUser, userRepresentation);

        userRepository.save(existingUser);
      } catch (final WebApplicationException ex) {
        log.warn("Skipping Keycloak profile sync for user {}: {}", keycloakId, ex.getMessage());
      }

      return userMapper.toDto(existingUser);
    }

    final User user =
        User.builder()
            .username(jwt.getClaimAsString("preferred_username"))
            .email(jwt.getClaimAsString("email"))
            .firstName(jwt.getClaimAsString("given_name"))
            .lastName(jwt.getClaimAsString("family_name"))
            .createdAt(LocalDateTime.now())
            .keycloakId(keycloakId)
            .build();

    final User createdUser = userRepository.save(user);

    return userMapper.toDto(createdUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUserById(final Long userId) {

    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        String.format("No user with id %s found", userId)));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResponse<UserDto> getAllUsers(final String search, final Pageable pageable) {

    final String normalizedSearch =
        StringUtils.hasText(search) ? LikePatternEscaper.escape(search.trim()) : null;

    return PagedResponse.from(
        userRepository.search(normalizedSearch, pageable).map(userMapper::toDto));
  }

  @Override
  @Transactional
  public void deleteUser(final Long userId) {

    final User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        String.format("No user with id %s found", userId)));

    if (userId.equals(currentUserService.requireCurrentUserId())) {
      throw new ConflictException(
          String.format("User with id %s cannot delete their own account", userId));
    }

    final boolean hasMembershipsOrExpenseHistory =
        memberRepository.existsByUserId(userId)
            || expenseRepository.existsByPaidById(userId)
            || expenseRepository.existsByParticipants_ParticipantId(userId);

    if (hasMembershipsOrExpenseHistory) {
      throw new ConflictException(
          String.format(
              "User with id %s cannot be deleted while they have group memberships or expense history",
              userId));
    }

    userRepository.delete(user);
  }
}
