package io.plan.mate.expense.tracker.backend.commons.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.Duration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
@EnableAspectJAutoProxy
@EnableCaching
public class AppConfig {

  @Bean
  public OpenAPI planMateOpenApi() {

    return new OpenAPI()
        .info(
            new Info()
                .title("PlanMate API")
                .description("API documentation for the PlanMate group expense tracker")
                .version("1.0.0")
                .contact(new Contact().name("PlanMate Dev Team").email("support@planmate.com"))
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .externalDocs(
            new ExternalDocumentation()
                .description("PlanMate Github Repository")
                .url("https://github.com/Didk0/PlanMate"));
  }

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {

    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofDays(1))
        .disableCachingNullValues()
        .serializeValuesWith(SerializationPair.fromSerializer(redisValueSerializer()));
  }

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {

    return builder ->
        builder.withCacheConfiguration(
            "settlements",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(redisValueSerializer())));
  }

  private GenericJacksonJsonRedisSerializer redisValueSerializer() {

    final PolymorphicTypeValidator typeValidator =
        BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("io.plan.mate.expense.tracker.backend.")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.time.")
            .allowIfSubType("java.math.")
            .build();

    return GenericJacksonJsonRedisSerializer.builder()
        .enableSpringCacheNullValueSupport()
        .enableDefaultTyping(typeValidator)
        .build();
  }
}
