package io.plan.mate.expense.tracker.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.plan.mate.expense.tracker.backend.configs.converters.ExpenseParticipantToDtoConverter;
import io.plan.mate.expense.tracker.backend.configs.converters.MemberToMemberDtoConverter;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.Duration;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

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
  public ModelMapper modelMapper() {

    final ModelMapper modelMapper = new ModelMapper();

    modelMapper.addConverter(new MemberToMemberDtoConverter());
    modelMapper.addConverter(new ExpenseParticipantToDtoConverter());

    return modelMapper;
  }

  @Bean
  public ObjectMapper objectMapper() {

    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Bean
  public RedisCacheConfiguration cacheConfiguration() {

    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofDays(1))
        .disableCachingNullValues()
        .serializeValuesWith(
            SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
  }

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
      final ObjectMapper objectMapper) {

    return builder ->
        builder.withCacheConfiguration(
            "settlements",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .disableCachingNullValues()
                .serializeValuesWith(
                    SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper))));
  }
}
