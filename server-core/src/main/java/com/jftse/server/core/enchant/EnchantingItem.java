package com.jftse.server.core.enchant;

import com.jftse.entities.database.model.item.ItemEnchant;
import com.jftse.entities.database.model.item.ItemPart;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnchantingItem extends ItemPart {
    private ItemEnchant itemEnchant;
    private double basicPercentage;
    private int failedPercentage;
    private int downgrade;
    private int enchantLevel;
    private int maxEnchantLevel;
}
