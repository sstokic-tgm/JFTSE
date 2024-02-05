package com.jftse.server.core.enchant;

public enum EnchantResultMessage {
    MSG_ITEM_ENCHANT_SUCCESS(0, "Enchantment success."),
    MSG_ITEM_ENCHANT_FAILED_01(1, "Enchantment failed."),
    MSG_ITEM_ENCHANT_FAILED_02(2, "Pocket index is wrong."),
    MSG_ITEM_ENCHANT_FAILED_03(3, "Cannot find item info."),
    MSG_ITEM_ENCHANT_FAILED_04(4, "Information about enchantment is changed."),
    MSG_ITEM_ENCHANT_FAILED_05(5, "The item has a maximum enchant value."),
    MSG_ITEM_ENCHANT_FAILED_06(6, "You don't have enough gold."),
    MSG_ITEM_ENCHANT_FAILED_07(7, "This item does not support elemental enchantment.");

    private final int code;
    private final String message;

    EnchantResultMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public byte getCode() {
        return (byte) code;
    }

    public String getMessage() {
        return message;
    }
}
