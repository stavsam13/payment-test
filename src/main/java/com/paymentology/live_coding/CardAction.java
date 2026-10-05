package com.paymentology.live_coding;

public enum CardAction {
    BLOCK("BLOCKED"),
    UNBLOCK("ACTIVE");

    private final String targetStatus;

    CardAction(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public static CardAction fromParam(String value) {
        for (CardAction action : values()) {
            if (action.name().equalsIgnoreCase(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unsupported action: " + value);
    }
}
