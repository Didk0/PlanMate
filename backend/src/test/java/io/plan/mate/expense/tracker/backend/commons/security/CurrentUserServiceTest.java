package io.plan.mate.expense.tracker.backend.commons.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.user.jpa.entity.User;
import io.plan.mate.expense.tracker.backend.user.jpa.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrentUserService")
class CurrentUserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CurrentUserService currentUserService;

  private static Jwt jwtFor(final UUID subject) {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject(subject.toString())
        .issuedAt(Instant.EPOCH)
        .expiresAt(Instant.EPOCH.plusSeconds(300))
        .build();
  }

  private void authenticateAs(final UUID subject, final String... authorities) {
    final Jwt jwt = jwtFor(subject);
    final List<SimpleGrantedAuthority> grantedAuthorities =
        List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, grantedAuthorities, subject.toString()));
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("resolves the app user id from the JWT subject")
  void findCurrentUserId_shouldResolveId_whenUserIsProvisioned() {
    UUID keycloakId = UUID.randomUUID();
    authenticateAs(keycloakId, "ROLE_USER");
    when(userRepository.findByKeycloakId(keycloakId))
        .thenReturn(Optional.of(User.builder().id(42L).keycloakId(keycloakId).build()));

    assertThat(currentUserService.findCurrentUserId()).contains(42L);
  }

  @Test
  @DisplayName("returns empty when the caller is authenticated but not yet provisioned")
  void findCurrentUserId_shouldReturnEmpty_whenUserIsNotProvisioned() {
    UUID keycloakId = UUID.randomUUID();
    authenticateAs(keycloakId, "ROLE_USER");
    when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

    assertThat(currentUserService.findCurrentUserId()).isEmpty();
  }

  @Test
  @DisplayName("returns empty when there is no authentication in the context")
  void findCurrentUserId_shouldReturnEmpty_whenNotAuthenticated() {
    assertThat(currentUserService.findCurrentUserId()).isEmpty();
  }

  @Test
  @DisplayName("returns true for isAdmin when the caller has ROLE_ADMIN")
  void isAdmin_shouldReturnTrue_whenCallerHasAdminAuthority() {
    authenticateAs(UUID.randomUUID(), "ROLE_ADMIN");

    assertThat(currentUserService.isAdmin()).isTrue();
  }

  @Test
  @DisplayName("returns false for isAdmin when the caller lacks ROLE_ADMIN")
  void isAdmin_shouldReturnFalse_whenCallerLacksAdminAuthority() {
    authenticateAs(UUID.randomUUID(), "ROLE_USER");

    assertThat(currentUserService.isAdmin()).isFalse();
  }

  @Test
  @DisplayName("returns false for isAdmin when there is no authentication in the context")
  void isAdmin_shouldReturnFalse_whenNotAuthenticated() {
    assertThat(currentUserService.isAdmin()).isFalse();
  }
}
