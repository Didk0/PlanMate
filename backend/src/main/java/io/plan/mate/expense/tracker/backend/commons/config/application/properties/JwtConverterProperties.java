package io.plan.mate.expense.tracker.backend.commons.config.application.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt.auth.converter")
@Getter
@Setter
public class JwtConverterProperties {

  private String resourceId;
  private String principleAttribute;
}
