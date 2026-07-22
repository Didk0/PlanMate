package io.plan.mate.expense.tracker.backend.commons.config.converters;

import io.plan.mate.expense.tracker.backend.commons.config.application.properties.JwtConverterProperties;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private static final String ROLE_PREFIX = "ROLE_";
  private static final String REALM_ACCESS = "realm_access";
  private static final String RESOURCE_ACCESS = "resource_access";
  private static final String ROLES = "roles";

  private final JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
  private final JwtConverterProperties properties;

  @Override
  public AbstractAuthenticationToken convert(@NonNull final Jwt jwt) {

    final Set<GrantedAuthority> authorities =
        Stream.of(scopesConverter.convert(jwt).stream(), extractRealmRoles(jwt).stream(), extractResourceRoles(jwt).stream())
            .flatMap(Function.identity())
            .collect(Collectors.toSet());

    return new JwtAuthenticationToken(jwt, authorities, resolvePrincipalName(jwt));
  }

  private String resolvePrincipalName(final Jwt jwt) {

    final String claimName =
        StringUtils.hasText(properties.getPrincipleAttribute())
            ? properties.getPrincipleAttribute()
            : JwtClaimNames.SUB;

    final String principal = jwt.getClaimAsString(claimName);

    return StringUtils.hasText(principal) ? principal : jwt.getSubject();
  }

  private Collection<GrantedAuthority> extractRealmRoles(final Jwt jwt) {

    final Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);

    if (realmAccess == null) {
      return Set.of();
    }

    return toAuthorities(realmAccess.get(ROLES));
  }

  private Collection<GrantedAuthority> extractResourceRoles(final Jwt jwt) {

    final Map<String, Object> resourceAccess = jwt.getClaimAsMap(RESOURCE_ACCESS);

    final boolean hasResourceRoles =
        resourceAccess != null
            && StringUtils.hasText(properties.getResourceId())
            && resourceAccess.get(properties.getResourceId()) instanceof Map;

    if (!hasResourceRoles) {
      return Set.of();
    }

    @SuppressWarnings("unchecked")
    final Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(properties.getResourceId());

    return toAuthorities(resource.get(ROLES));
  }

  private Collection<GrantedAuthority> toAuthorities(final Object rawRoles) {

    if (!(rawRoles instanceof Collection<?> roles)) {
      return Set.of();
    }

    return roles.stream()
        .map(String::valueOf)
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
        .collect(Collectors.toSet());
  }
}
