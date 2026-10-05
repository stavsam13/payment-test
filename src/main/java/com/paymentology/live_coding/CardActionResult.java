package com.paymentology.live_coding;

public record CardActionResult(AuditOutcome outcome, FailureReason failureReason) {

    public static CardActionResult succeeded() {
        return new CardActionResult(AuditOutcome.SUCCEEDED, null);
    }

    public static CardActionResult rejected(FailureReason failureReason) {
        return new CardActionResult(AuditOutcome.REJECTED, failureReason);
    }
}
