package com.jftse.server.core.service;

import com.jftse.server.core.service.transaction.ShopTransactionResult;
import com.jftse.server.core.shared.packets.shop.ShopBuy;
import java.util.List;

public interface ShopTransactionService {
    ShopTransactionResult buy(Long accountId, Long playerId, List<ShopBuy> requestedItems);
}
