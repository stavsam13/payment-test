package com.paymentology.live_coding;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;

@Service
public class CardActionService {

    private final CardRepository cardRepository;
    private final CardAuditEventRepository auditRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public CardActionService(CardRepository cardRepository, CardAuditEventRepository auditRepository,
                             TransactionTemplate transactionTemplate) {
        this.cardRepository = cardRepository;
        this.auditRepository = auditRepository;
        this.transactionTemplate = transactionTemplate;
        this.clock = Clock.systemUTC();
    }

    public CardActionResult perform(String cardNumber, CardAction action, String userId,
                                    ActorType actorType, ActionReason reason) {
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            // The card number is deliberately not audited: only internal ids are stored.
            String customerUserId = actorType == ActorType.CUSTOMER ? userId : null;
            auditRepository.save(rejection(null, customerUserId, action, FailureReason.CARD_NOT_FOUND,
                    reason, null, actorType, userId));
            return CardActionResult.rejected(FailureReason.CARD_NOT_FOUND);
        }

        try {
            // Status change and its audit row commit (or roll back) together.
            return transactionTemplate.execute(status -> apply(card.getId(), action, userId, actorType, reason));
        } catch (RuntimeException e) {
            // The business transaction rolled back; record the failure in its own transaction.
            auditRepository.save(new CardAuditEvent(card.getId(), card.getUserId(), action, AuditOutcome.FAILED,
                    FailureReason.INTERNAL_ERROR, reason, card.getStatus(), null, actorType, userId, now()));
            throw e;
        }
    }

    private CardActionResult apply(String cardId, CardAction action, String userId,
                                   ActorType actorType, ActionReason reason) {
        Card card = cardRepository.findById(cardId).orElseThrow();
        String previousStatus = card.getStatus();

        FailureReason failureReason = null;
        if (actorType == ActorType.CUSTOMER && !card.getUserId().equals(userId)) {
            failureReason = FailureReason.NOT_CARD_OWNER;
        } else if (action.getTargetStatus().equals(previousStatus)) {
            failureReason = action == CardAction.BLOCK ? FailureReason.ALREADY_BLOCKED : FailureReason.ALREADY_ACTIVE;
        }
        if (failureReason != null) {
            auditRepository.save(rejection(card.getId(), card.getUserId(), action, failureReason,
                    reason, previousStatus, actorType, userId));
            return CardActionResult.rejected(failureReason);
        }

        card.setStatus(action.getTargetStatus());
        cardRepository.save(card);
        auditRepository.save(new CardAuditEvent(card.getId(), card.getUserId(), action, AuditOutcome.SUCCEEDED,
                null, reason, previousStatus, card.getStatus(), actorType, userId, now()));
        return CardActionResult.succeeded();
    }

    private CardAuditEvent rejection(String cardId, String customerUserId, CardAction action,
                                     FailureReason failureReason, ActionReason reason, String previousStatus,
                                     ActorType actorType, String userId) {
        return new CardAuditEvent(cardId, customerUserId, action, AuditOutcome.REJECTED, failureReason,
                reason, previousStatus, null, actorType, userId, now());
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
