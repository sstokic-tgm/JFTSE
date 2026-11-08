package com.jftse.server.core.item;

import com.jftse.entities.database.model.pocket.PlayerPocket;

import java.time.Instant;

public record InventoryItemSnapshot(Long id, Long pocketId, String category, Integer itemIndex,
                                    String useType, Integer count, Instant created,
                                    Integer enchantStr, Integer enchantSta, Integer enchantDex,
                                    Integer enchantWil, Integer enchantElement, Integer enchantLevel) {
    public static InventoryItemSnapshot from(PlayerPocket item) {
        Instant created = item.getCreated() == null ? null : item.getCreated().toInstant();
        return new InventoryItemSnapshot(item.getId(), item.getPocket().getId(), item.getCategory(),
                item.getItemIndex(), item.getUseType(), item.getItemCount(), created,
                item.getEnchantStr(), item.getEnchantSta(), item.getEnchantDex(), item.getEnchantWil(),
                item.getEnchantElement(), item.getEnchantLevel());
    }
}
