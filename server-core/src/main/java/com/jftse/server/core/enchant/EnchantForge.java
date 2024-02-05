package com.jftse.server.core.enchant;

import com.jftse.entities.database.model.item.ItemEnchant;
import com.jftse.entities.database.model.item.ItemEnchantLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class EnchantForge {
    private final Random random;

    public EnchantForge() {
        random = new Random();
    }

    private double calculateSuccessRate(final EnchantingItem item) {
        double successRate = item.getBasicPercentage() + item.getItemEnchant().getAddPercentage() - (item.getEnchantLevel() * (item.getFailedPercentage() / 100.0));
        return Math.max(0.0, Math.min(100.0, successRate));
    }

    public int enchantItem(final EnchantingItem item) {
        double randomValue = random.nextDouble() * 100.0;

        int enchantLevel = item.getEnchantLevel();
        int maxEnchantLevel = item.getMaxEnchantLevel();
        int downGrade = item.getDowngrade();

        if (randomValue <= calculateSuccessRate(item)) {
            enchantLevel++;

            // Ensure the adjusted enchantment level is within the valid range
            enchantLevel = Math.min(enchantLevel, maxEnchantLevel);
        } else {
            // Enchantment failed, handle downgrading if applicable
            if (downGrade > 0 && enchantLevel > 1) {
                enchantLevel--;
            }
        }

        // Ensure the adjusted enchantment level is within the valid range
        return Math.max(1, enchantLevel);
    }

    public EnchantingItem createEnchantingItem(ItemEnchant itemEnchant, ItemEnchantLevel itemEnchantLevel, int currentEnchantLevel) {
        EnchantingItem enchantingItem = new EnchantingItem();
        enchantingItem.setItemEnchant(itemEnchant);
        enchantingItem.setBasicPercentage(itemEnchantLevel.getBasicPercentage());
        enchantingItem.setFailedPercentage(itemEnchantLevel.getFailedPercentage());
        enchantingItem.setDowngrade(itemEnchantLevel.getDowngrade());
        enchantingItem.setEnchantLevel(currentEnchantLevel);
        enchantingItem.setMaxEnchantLevel(9);
        return enchantingItem;
    }
}
