package com.jftse.server.core.service;

import com.jftse.server.core.service.transaction.InventorySaleResult;

public interface InventorySaleService {
    InventorySaleResult sell(Long accountId, Long playerId, Long itemPocketId);
}
