package io.plan.mate.expense.tracker.backend.commons.security;

import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.BadRequestException;
import io.plan.mate.expense.tracker.backend.commons.exception.handling.exception.ResourceNotFoundException;
import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** Resolves the authenticated caller's Keycloak identity and the app {@code User} row it maps to. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

  private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

  private final UserRepository userRepository;

  public UUID getKeycloakId() {
    return UUID.fromString(requireCurrentJwt().getSubject());
  }

  public Jwt requireCurrentJwt() {
    return currentJwt()
        .orElseThrow(() -> new BadRequestException("No authenticated user in the security context"));
  }

  public Optional<Long> findCurrentUserId() {
    return currentJwt()
        .flatMap(jwt -> userRepository.findByKeycloakId(UUID.fromString(jwt.getSubject())))
        .map(User::getId);
  }

  public Long requireCurrentUserId() {
    return findCurrentUserId()
        .orElseThrow(
            () -> new ResourceNotFoundException("Authenticated user is not provisioned in PlanMate"));
  }

  public boolean isAdmin() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.getAuthorities().stream()
            .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
  }

  private Optional<Jwt> currentJwt() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final boolean isJwtAuthentication =
        authentication != null && authentication.getPrincipal() instanceof Jwt;
    return isJwtAuthentication ? Optional.of((Jwt) authentication.getPrincipal()) : Optional.empty();
  }
}
