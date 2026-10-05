package com.paymentology.live_coding;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only record of a block/unblock attempt. Must never hold PAN, CVV or expiry date;
 * the card is referenced only by its internal id.
 */
@Entity
@Immutable
@Table(name = "card_audit_events")
public class CardAuditEvent {

    @Id
    private String id;
    @Column(name = "card_id")
    private String cardId;
    @Column(name = "customer_user_id")
    private String customerUserId;
    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private CardAction action;
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome")
    private AuditOutcome outcome;
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason")
    private FailureReason failureReason;
    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private ActionReason reason;
    @Column(name = "previous_status")
    private String previousStatus;
    @Column(name = "new_status")
    private String newStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type")
    private ActorType actorType;
    @Column(name = "actor_id")
    private String actorId;
    @Column(name = "occurred_at")
    private Instant occurredAt;

    protected CardAuditEvent() {
    }

    public CardAuditEvent(String cardId, String customerUserId, CardAction action, AuditOutcome outcome,
                          FailureReason failureReason, ActionReason reason, String previousStatus,
                          String newStatus, ActorType actorType, String actorId, Instant occurredAt) {
        this.id = UUID.randomUUID().toString();
        this.cardId = cardId;
        this.customerUserId = customerUserId;
        this.action = action;
        this.outcome = outcome;
        this.failureReason = failureReason;
        this.reason = reason;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actorType = actorType;
        this.actorId = actorId;
        this.occurredAt = occurredAt;
    }

    public String getId() {
        return id;
    }

    public String getCardId() {
        return cardId;
    }

    public String getCustomerUserId() {
        return customerUserId;
    }

    public CardAction getAction() {
        return action;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public FailureReason getFailureReason() {
        return failureReason;
    }

    public ActionReason getReason() {
        return reason;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public String getActorId() {
        return actorId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
