package io.plan.mate.expense.tracker.backend.services.keycloak;

import io.plan.mate.expense.tracker.backend.configs.application.properties.KeycloakProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakService {

  private final Keycloak keycloak;
  private final KeycloakProperties keycloakProperties;

  public UserRepresentation getUser(UUID keycloakId) {

    return keycloak
        .realm(keycloakProperties.getRealm())
        .users()
        .get(String.valueOf(keycloakId))
        .toRepresentation();
  }
}
