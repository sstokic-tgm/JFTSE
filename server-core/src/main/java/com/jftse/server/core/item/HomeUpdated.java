package com.jftse.server.core.item;

public record HomeUpdated(Long homeId, int previousLevel, int level) implements AddItemHook {
}
