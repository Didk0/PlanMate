package io.plan.mate.expense.tracker.backend.commons.websocket;

import io.plan.mate.expense.tracker.backend.commons.config.converters.JwtConverter;
import io.plan.mate.expense.tracker.backend.commons.security.GroupAccessEvaluator;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final Pattern GROUP_TOPIC = Pattern.compile("^/topic/groups/(\\d+)/[^/]+$");

  private final JwtDecoder jwtDecoder;
  private final JwtConverter jwtConverter;
  private final GroupAccessEvaluator groupAccessEvaluator;

  @Override
  public Message<?> preSend(final @NonNull Message<?> message, final @NonNull MessageChannel channel) {

    final StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      accessor.setUser(authenticate(accessor));
    } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      authorizeSubscription(accessor);
    }

    return message;
  }

  private Authentication authenticate(final StompHeaderAccessor accessor) {

    final String header = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

    if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
      throw new MessageDeliveryException("Missing bearer token on STOMP CONNECT");
    }

    final Jwt jwt;

    try {
      jwt = jwtDecoder.decode(header.substring(BEARER_PREFIX.length()));
    } catch (final JwtException ex) {
      log.warn("Rejecting STOMP CONNECT with an invalid token: {}", ex.getMessage());
      throw new MessageDeliveryException("Invalid bearer token on STOMP CONNECT");
    }

    final Authentication authentication = jwtConverter.convert(jwt);

    if (authentication == null) {
      throw new MessageDeliveryException(
          "Failed to derive an authentication from the STOMP CONNECT token");
    }

    return authentication;
  }

  private void authorizeSubscription(
      final StompHeaderAccessor accessor) {

    final String destination = accessor.getDestination();

    if (destination == null) {
      return;
    }

    final Matcher matcher = GROUP_TOPIC.matcher(destination);

    if (!matcher.matches()) {
      throw new MessageDeliveryException("Subscription to " + destination + " is not allowed");
    }

    final Authentication authentication = asAuthentication(accessor.getUser());

    if (authentication == null) {
      throw new MessageDeliveryException("Unauthenticated STOMP subscription");
    }

    final Long groupId = Long.valueOf(matcher.group(1));

    // The inbound channel threads are pooled, so the context has to be cleared again.
    try {
      SecurityContextHolder.getContext().setAuthentication(authentication);

      if (!groupAccessEvaluator.isMember(groupId)) {
        throw new MessageDeliveryException(
            "Caller is not a member of group with id " + groupId);
      }
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private Authentication asAuthentication(final Principal principal) {
    return principal instanceof Authentication authentication ? authentication : null;
  }
}
