package com.jftse.server.core.service.transaction;

public record InventorySaleResult(int gold, int ap, int price, int quantity, int itemPocketId,
                                   TransactionSummary transaction) {
}
