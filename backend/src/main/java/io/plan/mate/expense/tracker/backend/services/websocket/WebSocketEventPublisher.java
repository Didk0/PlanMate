package io.plan.mate.expense.tracker.backend.services.websocket;

import io.plan.mate.expense.tracker.backend.payloads.events.ExpenseCreatedEvent;
import io.plan.mate.expense.tracker.backend.payloads.events.MemberChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketEventPublisher {

  private static final String BASE_DEST = "/topic/groups/";

  private final SimpMessagingTemplate messagingTemplate;

  public void publishExpenseCreated(final ExpenseCreatedEvent event) {

    final String destination = BASE_DEST + event.groupId() + "/expenses";
    messagingTemplate.convertAndSend(destination, event);
  }

  public void publishMembersUpdate(final MemberChangedEvent event) {

    final String destination = BASE_DEST + event.groupId() + "/users";
    messagingTemplate.convertAndSend(destination, event);
  }
}
