package io.plan.mate.expense.tracker.backend.commons.websocket;

import io.plan.mate.expense.tracker.backend.expense.controller.payload.event.ExpenseCreatedEvent;
import io.plan.mate.expense.tracker.backend.member.controller.payload.event.MemberChangedEvent;
import io.plan.mate.expense.tracker.backend.settlement.controller.payload.event.SettlementsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  private final WebSocketEventPublisher eventPublisher;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onExpenseCreated(final ExpenseCreatedEvent event) {
    eventPublisher.publishExpenseCreated(event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onMemberChanged(final MemberChangedEvent event) {
    eventPublisher.publishMembersUpdate(event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  public void onSettlementsChanged(final SettlementsChangedEvent event) {
    eventPublisher.publishSettlementsChanged(event);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  public void onExpenseCreatedRolledBack(final ExpenseCreatedEvent event) {
    log.warn(
        "Transaction rolled back; skipping WS publish for expense creation in group {}",
        event.groupId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  public void onMemberChangedRolledBack(final MemberChangedEvent event) {
    log.warn(
        "Transaction rolled back; skipping WS publish for member change in group {}",
        event.groupId());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  public void onSettlementsChangedRolledBack(final SettlementsChangedEvent event) {
    log.warn(
        "Transaction rolled back; skipping WS publish for settlements change in group {}",
        event.groupId());
  }
}
