package io.plan.mate.expense.tracker.backend.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CacheErrorConfig implements CachingConfigurer {

  @Override
  public CacheErrorHandler errorHandler() {

    return new CacheErrorHandler() {

      @Override
      public void handleCacheGetError(
          final RuntimeException exception, final Cache cache, final Object key) {

        log.warn("Cache GET failed on '{}' key={} - serving from source", cache.getName(), key, exception);
      }

      @Override
      public void handleCachePutError(
          final RuntimeException exception, final Cache cache, final Object key, final Object value) {

        log.warn("Cache PUT failed on '{}' key={}", cache.getName(), key, exception);
      }

      @Override
      public void handleCacheEvictError(
          final RuntimeException exception, final Cache cache, final Object key) {

        log.error("Cache EVICT failed on '{}' key={} - rejecting to avoid a stale cache", cache.getName(), key, exception);
        throw exception;
      }

      @Override
      public void handleCacheClearError(final RuntimeException exception, final Cache cache) {

        log.error("Cache CLEAR failed on '{}' - rejecting to avoid a stale cache", cache.getName(), exception);
        throw exception;
      }
    };
  }
}
