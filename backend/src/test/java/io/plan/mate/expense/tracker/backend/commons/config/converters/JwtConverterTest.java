package io.plan.mate.expense.tracker.backend.commons.config.converters;

import static org.assertj.core.api.Assertions.assertThat;

import io.plan.mate.expense.tracker.backend.commons.config.application.properties.JwtConverterProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

@DisplayName("JwtConverter")
class JwtConverterTest {

  private static final String SUBJECT = "11111111-1111-1111-1111-111111111111";

  private JwtConverterProperties propertiesFor(final String resourceId) {
    return propertiesFor(resourceId, "sub");
  }

  private JwtConverterProperties propertiesFor(final String resourceId, final String principleAttribute) {
    final JwtConverterProperties properties = new JwtConverterProperties();
    properties.setResourceId(resourceId);
    properties.setPrincipleAttribute(principleAttribute);
    return properties;
  }

  private Jwt.Builder jwtBuilder() {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject(SUBJECT)
        .issuedAt(Instant.EPOCH)
        .expiresAt(Instant.EPOCH.plusSeconds(300));
  }

  @Test
  @DisplayName("grants ROLE_ADMIN when realm_access contains ADMIN")
  void convert_shouldGrantRoleAdmin_whenRealmAccessContainsAdmin() {
    final Jwt jwt =
        jwtBuilder().claim("realm_access", Map.of("roles", List.of("ADMIN"))).build();

    final AbstractAuthenticationToken token =
        new JwtConverter(propertiesFor("planmate-backend")).convert(jwt);

    assertThat(token.getAuthorities())
        .extracting(Object::toString)
        .contains("ROLE_ADMIN");
  }

  @Test
  @DisplayName("grants a role for the configured resource id from resource_access")
  void convert_shouldGrantRoleUser_whenResourceAccessContainsUserForConfiguredResourceId() {
    final Jwt jwt =
        jwtBuilder()
            .claim(
                "resource_access",
                Map.of("planmate-backend", Map.of("roles", List.of("USER"))))
            .build();

    final AbstractAuthenticationToken token =
        new JwtConverter(propertiesFor("planmate-backend")).convert(jwt);

    assertThat(token.getAuthorities())
        .extracting(Object::toString)
        .contains("ROLE_USER");
  }

  @Test
  @DisplayName("returns no role authorities when realm_access and resource_access are absent")
  void convert_shouldReturnNoRoleAuthorities_whenRealmAccessAndResourceAccessAreAbsent() {
    final Jwt jwt = jwtBuilder().build();

    final AbstractAuthenticationToken token =
        new JwtConverter(propertiesFor("planmate-backend")).convert(jwt);

    assertThat(token.getAuthorities()).isEmpty();
  }

  @Test
  @DisplayName("does not throw when a resource_access entry has no roles key")
  void convert_shouldNotThrow_whenResourceAccessEntryHasNoRolesKey() {
    final Jwt jwt =
        jwtBuilder()
            .claim("resource_access", Map.of("planmate-backend", Map.of()))
            .build();

    final AbstractAuthenticationToken token =
        new JwtConverter(propertiesFor("planmate-backend")).convert(jwt);

    assertThat(token.getAuthorities()).isEmpty();
  }

  @Test
  @DisplayName("uses the subject as the principal name when the configured claim is missing")
  void convert_shouldUseSubjectAsPrincipalName_whenPrincipleAttributeClaimIsMissing() {
    final Jwt jwt = jwtBuilder().build();

    final AbstractAuthenticationToken token =
        new JwtConverter(propertiesFor("planmate-backend", "principal_username")).convert(jwt);

    assertThat(token.getName()).isEqualTo(SUBJECT);
  }
}
