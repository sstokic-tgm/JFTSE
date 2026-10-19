package com.jftse.server.core.service.transaction;

import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.messenger.EParcelType;
import com.jftse.entities.database.model.transaction.TransactionAction;
import com.jftse.entities.database.model.transaction.TransactionComponentType;
import com.jftse.entities.database.model.transaction.TransactionAmount;
import com.jftse.entities.database.model.transaction.TransactionItem;
import com.jftse.entities.database.model.transaction.TransactionLog;
import com.jftse.entities.database.model.transaction.TransactionType;

import java.time.Instant;
import java.util.List;

public record TransactionSummary(Long id, String operationId, Long componentOfId, TransactionType type, TransactionAction action,
                                 Long sourceId, Long accountId, Long playerId, Long counterpartyAccountId,
                                 Long counterpartyPlayerId, EParcelType parcelType, Integer requestedGold,
                                 Instant created, List<Amount> amounts, List<Item> items) {
    public TransactionSummary {
        amounts = List.copyOf(amounts);
        items = List.copyOf(items);
    }

    public record Amount(Long accountId, Long playerId, PriceType currency, long before, long after, long delta) {
    }

    public record Item(Long referenceId, Integer productIndex, Byte option, String category, Integer itemIndex,
                       Long pocketId, long before, long after, String useType, Instant createdBefore,
                       Instant createdAfter, Integer enchantStr, Integer enchantSta, Integer enchantDex,
                       Integer enchantWil, Integer enchantElement, Integer enchantLevel, List<Component> components) {
        public Item {
            components = List.copyOf(components);
        }
    }

    public record Component(Long componentOfId, TransactionComponentType type, long quantity) {
    }

    public static TransactionSummary from(TransactionLog transaction) {
        List<Amount> amounts = transaction.getAmounts().stream()
                .map(TransactionSummary::amount)
                .toList();
        List<Item> items = transaction.getItems().stream()
                .map(TransactionSummary::item)
                .toList();

        Long componentOfId = transaction.getComponentOf() == null ? null : transaction.getComponentOf().getId();
        return new TransactionSummary(transaction.getId(), transaction.getOperationId(), componentOfId, transaction.getType(),
                transaction.getAction(), transaction.getSourceId(), transaction.getAccountId(), transaction.getPlayerId(),
                transaction.getCounterpartyAccountId(), transaction.getCounterpartyPlayerId(), transaction.getParcelType(),
                transaction.getRequestedGold(), transaction.getCreated().toInstant(), amounts, items);
    }

    private static Amount amount(TransactionAmount amount) {
        return new Amount(amount.getAccountId(), amount.getPlayerId(), amount.getCurrency(),
                amount.getBalanceBefore(), amount.getBalanceAfter(), amount.getDelta());
    }

    private static Item item(TransactionItem item) {
        Instant createdBefore = item.getItemCreatedBefore() == null ? null : item.getItemCreatedBefore().toInstant();
        Instant createdAfter = item.getItemCreatedAfter() == null ? null : item.getItemCreatedAfter().toInstant();
        List<Component> components = item.getComponents().stream()
                .map(component -> new Component(component.getComponentOf().getId(), component.getType(), component.getQuantity()))
                .toList();

        return new Item(item.getReferenceId(), item.getProductIndex(), item.getProductOption(), item.getCategory(),
                item.getItemIndex(), item.getPocketId(), item.getQuantityBefore(), item.getQuantityAfter(), item.getUseType(),
                createdBefore, createdAfter, item.getEnchantStr(), item.getEnchantSta(), item.getEnchantDex(),
                item.getEnchantWil(), item.getEnchantElement(), item.getEnchantLevel(), components);
    }
}
