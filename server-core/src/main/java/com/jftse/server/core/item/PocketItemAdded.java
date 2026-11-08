package com.jftse.server.core.item;

public record PocketItemAdded(InventoryItemSnapshot before, InventoryItemSnapshot after) implements AddItemHook {
}
