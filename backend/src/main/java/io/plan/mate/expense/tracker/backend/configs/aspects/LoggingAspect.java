package io.plan.mate.expense.tracker.backend.configs.aspects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingAspect {

  private final ObjectMapper objectMapper;

  @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
  public void restControllerMethods() {}

  @Around("restControllerMethods()")
  public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {

    final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    final String className = methodSignature.getDeclaringType().getSimpleName();
    final String methodName = methodSignature.getName();
    final Object[] args = joinPoint.getArgs();

    log.info(
        "Received request: {}.{}() with arguments = {}",
        className,
        methodName,
        safeSerialize(args));

    final Instant startTime = Instant.now();

    try {
      final Object result = joinPoint.proceed();

      final Instant endTime = Instant.now();
      final long durationMillis = Duration.between(startTime, endTime).toMillis();

      log.info(
          "Response from {}.{}() = {} (Execution time: {} ms)",
          className,
          methodName,
          safeSerialize(result),
          durationMillis);

      return result;

    } catch (final Exception exception) {

      final Instant endTime = Instant.now();
      final long durationMillis = Duration.between(startTime, endTime).toMillis();

      log.error(
          "Exception in {}.{}() after {} ms with message = {} and cause = {}",
          className,
          methodName,
          durationMillis,
          exception.getMessage(),
          exception.getCause() != null ? exception.getCause() : "NULL",
          exception);

      throw exception;
    }
  }

  private String safeSerialize(final Object obj) {

    try {
      return objectMapper.writeValueAsString(obj);
    } catch (final JsonProcessingException e) {
      return "[unserializable]";
    }
  }
}
