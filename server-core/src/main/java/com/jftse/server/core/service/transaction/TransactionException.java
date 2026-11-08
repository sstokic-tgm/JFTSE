package com.jftse.server.core.service.transaction;

import lombok.Getter;

@Getter
public class TransactionException extends RuntimeException {
    public enum Reason {
        INVALID_REQUEST,
        NO_ITEM,
        NOT_FOR_SALE,
        INSUFFICIENT_GOLD,
        INSUFFICIENT_AP,
        NOT_FOUND,
        LIMIT_REACHED,
        LEVEL_TOO_LOW,
        INVALID_AMOUNT,
        FORBIDDEN
    }
    private final Reason reason;

    public TransactionException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }
}
