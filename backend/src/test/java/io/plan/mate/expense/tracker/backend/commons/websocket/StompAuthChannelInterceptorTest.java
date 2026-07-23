package io.plan.mate.expense.tracker.backend.commons.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.plan.mate.expense.tracker.backend.commons.config.converters.JwtConverter;
import io.plan.mate.expense.tracker.backend.commons.security.GroupAccessEvaluator;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
@DisplayName("StompAuthChannelInterceptor")
class StompAuthChannelInterceptorTest {

  private static final String VALID_TOKEN = "valid-token";

  @Mock private JwtDecoder jwtDecoder;
  @Mock private JwtConverter jwtConverter;
  @Mock private GroupAccessEvaluator groupAccessEvaluator;
  @Mock private MessageChannel channel;

  @InjectMocks private StompAuthChannelInterceptor interceptor;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private Message<?> connectMessage(final String authorizationHeader) {
    final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.addNativeHeader("Authorization", authorizationHeader);
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  private Message<?> subscribeMessage(final String destination, final Authentication user) {
    final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination(destination);
    accessor.setUser(user);
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  @Nested
  @DisplayName("CONNECT")
  class Connect {

    @Test
    @DisplayName("rejects a CONNECT frame with no Authorization header")
    void preSend_shouldThrow_whenAuthorizationHeaderIsMissing() {
      assertThatThrownBy(() -> interceptor.preSend(connectMessage(null), channel))
          .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("rejects a CONNECT frame with a malformed Authorization header")
    void preSend_shouldThrow_whenAuthorizationHeaderIsNotBearer() {
      assertThatThrownBy(() -> interceptor.preSend(connectMessage("Basic abc"), channel))
          .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("rejects a CONNECT frame whose token fails to decode")
    void preSend_shouldThrow_whenTokenIsInvalid() {
      when(jwtDecoder.decode(VALID_TOKEN)).thenThrow(new JwtException("bad token"));

      assertThatThrownBy(
              () -> interceptor.preSend(connectMessage("Bearer " + VALID_TOKEN), channel))
          .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("authenticates and stores the principal when the token is valid")
    void preSend_shouldSetUser_whenTokenIsValid() {
      final Jwt jwt =
          Jwt.withTokenValue(VALID_TOKEN)
              .header("alg", "none")
              .claim("sub", "caller")
              .issuedAt(Instant.now())
              .expiresAt(Instant.now().plusSeconds(60))
              .build();
      final TestingAuthenticationToken authentication = new TestingAuthenticationToken("caller", null);

      when(jwtDecoder.decode(VALID_TOKEN)).thenReturn(jwt);
      when(jwtConverter.convert(jwt)).thenReturn(authentication);

      final Message<?> message = connectMessage("Bearer " + VALID_TOKEN);
      interceptor.preSend(message, channel);

      final StompHeaderAccessor accessor =
          MessageHeaderAccessor.getAccessor(
              message, StompHeaderAccessor.class);

      assertThat(accessor.getUser()).isEqualTo(authentication);
    }
  }

  @Nested
  @DisplayName("SUBSCRIBE")
  class Subscribe {

    @Test
    @DisplayName("rejects a subscription to a destination outside /topic/groups/{id}/*")
    void preSend_shouldThrow_whenDestinationDoesNotMatchGroupTopicShape() {
      final Authentication authentication = new TestingAuthenticationToken("caller", null);

      assertThatThrownBy(
              () ->
                  interceptor.preSend(
                      subscribeMessage("/topic/other", authentication), channel))
          .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("rejects an unauthenticated subscription")
    void preSend_shouldThrow_whenUserIsMissing() {
      assertThatThrownBy(
              () ->
                  interceptor.preSend(
                      subscribeMessage("/topic/groups/1/expenses", null), channel))
          .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    @DisplayName("rejects a subscription when the caller is not a member of the group")
    void preSend_shouldThrow_whenCallerIsNotAMember() {
      final Authentication authentication = new TestingAuthenticationToken("caller", null);
      when(groupAccessEvaluator.isMember(1L)).thenReturn(false);

      assertThatThrownBy(
              () ->
                  interceptor.preSend(
                      subscribeMessage("/topic/groups/1/expenses", authentication), channel))
          .isInstanceOf(MessageDeliveryException.class);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("allows a subscription when the caller is a member of the group")
    void preSend_shouldAllow_whenCallerIsAMember() {
      final Authentication authentication = new TestingAuthenticationToken("caller", null);
      when(groupAccessEvaluator.isMember(1L)).thenReturn(true);

      interceptor.preSend(subscribeMessage("/topic/groups/1/expenses", authentication), channel);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
  }
}
