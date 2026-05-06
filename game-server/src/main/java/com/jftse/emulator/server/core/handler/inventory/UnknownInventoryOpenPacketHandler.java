package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.*;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.*;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.BattlemonSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGUnknownInventoryOpen;

@PacketId(CMSGUnknownInventoryOpen.PACKET_ID)
public class UnknownInventoryOpenPacketHandler implements PacketHandler<FTConnection, CMSGUnknownInventoryOpen> {
    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;

    public UnknownInventoryOpenPacketHandler() {
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGUnknownInventoryOpen packet) {
        FTClient client = connection.getClient();
        if (client.hasPlayer()) {
            FTPlayer player = client.getPlayer();

            EquippedQuickSlots equippedQuickSlots = player.getQuickSlots();
            EquippedToolSlots equippedToolSlots = player.getToolSlots();
            EquippedSpecialSlots equippedSpecialSlots = player.getSpecialSlots();
            EquippedCardSlots equippedCardSlots = player.getCardSlots();
            EquippedPetSlots equippedPetSlots = player.getPetSlots();

            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, player);
            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots.toList());
            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots.toList());
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots.toList());
            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots.toList());
            S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(equippedPetSlots.toList());

            //connection.sendTCP(inventoryWearClothAnswerPacket, inventoryWearQuickAnswerPacket, inventoryWearToolAnswerPacket, inventoryWearSpecialAnswerPacket, inventoryWearCardAnswerPacket, inventoryWearBattlemonAnswerPacket);
        }
    }
}
