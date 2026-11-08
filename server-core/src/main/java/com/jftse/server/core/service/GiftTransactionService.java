package com.jftse.server.core.service;

import com.jftse.server.core.service.transaction.GiftTransactionResult;

public interface GiftTransactionService {
    GiftTransactionResult send(Long accountId, Long playerId, String receiverName,
                               int productIndex, byte option, String message);
}
