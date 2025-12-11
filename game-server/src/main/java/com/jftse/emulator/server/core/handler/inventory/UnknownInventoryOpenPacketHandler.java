package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.*;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.inventory.CMSGUnknownInventoryOpen;

import java.util.List;
import java.util.Map;

@PacketId(CMSGUnknownInventoryOpen.PACKET_ID)
public class UnknownInventoryOpenPacketHandler implements PacketHandler<FTConnection, CMSGUnknownInventoryOpen> {
    private final ClothEquipmentServiceImpl clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final BattlemonSlotEquipmentService battlemonSlotEquipmentService;

    public UnknownInventoryOpenPacketHandler() {
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        battlemonSlotEquipmentService = ServiceManager.getInstance().getBattlemonSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGUnknownInventoryOpen packet) {
        FTClient client = connection.getClient();
        if (client != null) {
            Player player = client.getPlayer();

            if (player != null) {
                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
                Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
                List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);
                List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
                List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
                List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);
                List<Integer> equippedBattlemonSlots = battlemonSlotEquipmentService.getEquippedBattlemonSlots(player);

                S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
                S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
                S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
                S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
                S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);
                S2CInventoryWearBattlemonAnswerPacket inventoryWearBattlemonAnswerPacket = new S2CInventoryWearBattlemonAnswerPacket(equippedBattlemonSlots);

                //connection.sendTCP(inventoryWearClothAnswerPacket, inventoryWearQuickAnswerPacket, inventoryWearToolAnswerPacket, inventoryWearSpecialAnswerPacket, inventoryWearCardAnswerPacket, inventoryWearBattlemonAnswerPacket);
            }
        }
    }
}
