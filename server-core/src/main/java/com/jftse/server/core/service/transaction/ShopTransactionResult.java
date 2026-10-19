package com.jftse.server.core.service.transaction;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.AddItemHook;
import java.util.List;

public record ShopTransactionResult(int gold, int ap, int costsGold, int costsAp,
                                    List<PlayerPocket> items, List<AddItemHook> postActions,
                                    TransactionSummary transaction) {
    public ShopTransactionResult {
        items = List.copyOf(items);
        postActions = List.copyOf(postActions);
    }
}
