package com.jftse.server.core.service.transaction;

import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.transaction.TransactionAmount;
import com.jftse.entities.database.model.transaction.TransactionItem;
import com.jftse.entities.database.model.transaction.TransactionLog;
import com.jftse.entities.database.model.transaction.TransactionType;
import com.jftse.server.core.item.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TransactionLogBuilder {
    private final TransactionLog transaction = new TransactionLog();
    private final Map<Long, Instant> itemCreated = new HashMap<>();

    public TransactionLogBuilder(TransactionType type, Long accountId, Long playerId) {
        transaction.setOperationId(UUID.randomUUID().toString());
        transaction.setType(type);
        transaction.setAccountId(accountId);
        transaction.setPlayerId(playerId);
    }

    public TransactionLog getTransaction() {
        return transaction;
    }

    public void counterparty(Player player) {
        transaction.setCounterpartyAccountId(player.getAccount().getId());
        transaction.setCounterpartyPlayerId(player.getId());
    }

    public void amount(PriceType currency, int before, int after) {
        amount(currency, transaction.getAccountId(), transaction.getPlayerId(), before, after);
    }

    public void amount(PriceType currency, Long accountId, Long playerId, int before, int after) {
        if (before == after) {
            return;
        }

        TransactionAmount amount = new TransactionAmount();
        amount.setTransaction(transaction);
        amount.setLineNo(transaction.getAmounts().size());
        amount.setAccountId(accountId);
        amount.setPlayerId(playerId);
        amount.setCurrency(currency);
        amount.setBalanceBefore((long) before);
        amount.setBalanceAfter((long) after);
        amount.setDelta((long) after - before);
        transaction.getAmounts().add(amount);
    }

    public void hook(AddItemHook hook, Product product, Byte option) {
        if (hook instanceof PocketItemAdded added) {
            pocket(added.before(), added.after(), product.getProductIndex(), option);
        } else if (hook instanceof PetCreated created) {
            item(created.pet().getId(), EItemCategory.PET_CHAR.getName(), product.getItem0(),
                    0, 1, product.getProductIndex(), option);
        } else if (hook instanceof PlayerCreated created) {
            item(created.player().getId(), EItemCategory.CHAR.getName(), (int) product.getForPlayer(),
                    0, 1, product.getProductIndex(), option);
        } else if (hook instanceof HomeUpdated home) {
            item(home.homeId(), EItemCategory.HOUSE.getName(), product.getItem0(),
                    home.previousLevel(), home.level(), product.getProductIndex(), option);
        }
    }

    public void pocket(InventoryItemSnapshot before, InventoryItemSnapshot after, Integer productIndex, Byte option) {
        InventoryItemSnapshot snapshot = after == null ? before : after;
        long quantityBefore = before == null ? 0 : before.count();
        long quantityAfter = after == null ? 0 : after.count();

        TransactionItem item = item(snapshot.id(), snapshot.category(), snapshot.itemIndex(),
                quantityBefore, quantityAfter, productIndex, option);
        item.setPocketId(snapshot.pocketId());
        item.setUseType(snapshot.useType());

        // PlayerPocket.created is immutable in JPA, including the TIME expiry value.
        Instant persistedCreated = itemCreated.computeIfAbsent(snapshot.id(),
                ignored -> before == null ? snapshot.created() : before.created());
        if (persistedCreated != null) {
            item.setItemCreatedBefore(before == null ? null : Date.from(persistedCreated));
            item.setItemCreatedAfter(after == null ? null : Date.from(persistedCreated));
        }

        item.setEnchantStr(snapshot.enchantStr());
        item.setEnchantSta(snapshot.enchantSta());
        item.setEnchantDex(snapshot.enchantDex());
        item.setEnchantWil(snapshot.enchantWil());
        item.setEnchantElement(snapshot.enchantElement());
        item.setEnchantLevel(snapshot.enchantLevel());
    }

    private TransactionItem item(Long id, String category, Integer itemIndex, long before, long after,
                                 Integer productIndex, Byte option) {
        TransactionItem item = new TransactionItem();
        item.setTransaction(transaction);
        item.setLineNo(transaction.getItems().size());
        item.setCategory(category);
        item.setItemIndex(itemIndex);
        item.setReferenceId(id);
        item.setProductIndex(productIndex);
        item.setProductOption(option);
        item.setQuantityBefore(before);
        item.setQuantityAfter(after);
        transaction.getItems().add(item);
        return item;
    }
}
