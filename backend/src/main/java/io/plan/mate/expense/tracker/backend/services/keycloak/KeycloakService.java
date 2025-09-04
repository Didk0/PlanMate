package io.plan.mate.expense.tracker.backend.services.keycloak;

import io.plan.mate.expense.tracker.backend.configs.ApplicationProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakService {

  private final Keycloak keycloak;
  private final ApplicationProperties applicationProperties;

  public UserRepresentation getUser(UUID keycloakId) {

    return keycloak
        .realm(applicationProperties.getKeycloakRealm())
        .users()
        .get(String.valueOf(keycloakId))
        .toRepresentation();
  }
}
