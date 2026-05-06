package com.jftse.emulator.server.core.client;

import com.jftse.entities.database.model.player.Player;

import java.util.List;

public record EquippedItemParts(long id, int hair, int face, int dress, int pants, int socks, int shoes, int gloves,
                                int racket, int glasses, int bag, int hat, int dye) {
    public static EquippedItemParts of(Player player) {
        return new EquippedItemParts(
                player.getClothEquipment().getId(),
                player.getClothEquipment().getHair(),
                player.getClothEquipment().getFace(),
                player.getClothEquipment().getDress(),
                player.getClothEquipment().getPants(),
                player.getClothEquipment().getSocks(),
                player.getClothEquipment().getShoes(),
                player.getClothEquipment().getGloves(),
                player.getClothEquipment().getRacket(),
                player.getClothEquipment().getGlasses(),
                player.getClothEquipment().getBag(),
                player.getClothEquipment().getHat(),
                player.getClothEquipment().getDye()
        );
    }

    public static EquippedItemParts of(long id, int hair, int face, int dress, int pants, int socks, int shoes, int gloves,
                                       int racket, int glasses, int bag, int hat, int dye) {
        return new EquippedItemParts(id, hair, face, dress, pants, socks, shoes, gloves, racket, glasses, bag, hat, dye);
    }

    public List<Integer> toList() {
        return List.of(hair, face, dress, pants, socks, shoes, gloves, racket, glasses, bag, hat, dye);
    }
}
