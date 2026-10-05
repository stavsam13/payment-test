package com.paymentology.live_coding;

import java.time.Instant;

/** Support-facing view of an audit event. Carries no card number, CVV or expiry date. */
public record CardActivityItem(String cardId, CardAction action, AuditOutcome outcome,
                               FailureReason failureReason, ActionReason reason, String previousStatus,
                               String newStatus, ActorType actorType, String actorId, Instant occurredAt) {

    static CardActivityItem from(CardAuditEvent event) {
        return new CardActivityItem(event.getCardId(), event.getAction(), event.getOutcome(),
                event.getFailureReason(), event.getReason(), event.getPreviousStatus(), event.getNewStatus(),
                event.getActorType(), event.getActorId(), event.getOccurredAt());
    }
}
