package io.plan.mate.expense.tracker.backend.configs.application.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Validated
@Getter
@Setter
public class KeycloakProperties {

  @NotBlank private String authServerUrl;

  @NotBlank private String realm;

  @NotBlank private String clientId;

  @NotBlank private String clientSecret;

  private String scope;
}
