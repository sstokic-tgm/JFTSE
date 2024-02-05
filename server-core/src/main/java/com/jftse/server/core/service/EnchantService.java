package com.jftse.server.core.service;

import com.jftse.entities.database.model.item.ItemEnchant;
import com.jftse.entities.database.model.item.ItemEnchantLevel;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.item.EElementalKind;

public interface EnchantService {
    PlayerPocket getPlayerPocket(int playerPocketId);
    boolean isValidPlayerPocketId(int playerPocketId, Pocket pocket);
    boolean isEnchantable(int playerPocketId);
    boolean isElemental(int playerPocketId);
    boolean isMaxEnchantLevel(int playerPocketId, boolean forElemental, EElementalKind elementalKind);
    boolean hasJewel(int playerPocketId, Pocket pocket);
    boolean hasElemental(int playerPocketId, Pocket pocket);
    ItemEnchant getItemEnchant(int playerPocketId);
    ItemEnchantLevel getItemEnchantLevel(String elementalKind, int grade);
}
