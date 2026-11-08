package com.jftse.server.core.service.transaction;

import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.AddItemHook;

import java.util.List;

public record GiftTransactionResult(Gift gift, int gold, int ap, List<PlayerPocket> items,
                                    List<AddItemHook> postActions, TransactionSummary transaction) {
    public GiftTransactionResult {
        items = List.copyOf(items);
        postActions = List.copyOf(postActions);
    }
}
