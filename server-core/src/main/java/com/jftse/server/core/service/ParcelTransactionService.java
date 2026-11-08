package com.jftse.server.core.service;

import com.jftse.server.core.service.transaction.ParcelTransactionResult;

public interface ParcelTransactionService {
    ParcelTransactionResult send(Long accountId, Long playerId, Long itemId,
                                 String receiverName, int gold, String message);

    ParcelTransactionResult accept(Long accountId, Long playerId, Long parcelId);

    ParcelTransactionResult deny(Long accountId, Long playerId, Long parcelId);

    ParcelTransactionResult cancel(Long accountId, Long playerId, Long parcelId);
}
