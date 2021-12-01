package com.jftse.emulator.server.core.handler.game.inventory;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.inventory.*;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.Map;

public class UnknownInventoryOpenPacketHandler extends AbstractHandler {
    private Packet packet;

    private final PlayerService playerService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;

    public UnknownInventoryOpenPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        quickSlotEquipmentService = ServiceManager.getInstance().getQuickSlotEquipmentService();
        toolSlotEquipmentService = ServiceManager.getInstance().getToolSlotEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findById(connection.getClient().getActivePlayer().getId());

        if (player != null) {
            StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);
            Map<String, Integer> equippedCloths = clothEquipmentService.getEquippedCloths(player);
            List<Integer> equippedQuickSlots = quickSlotEquipmentService.getEquippedQuickSlots(player);
            List<Integer> equippedToolSlots = toolSlotEquipmentService.getEquippedToolSlots(player);
            List<Integer> equippedSpecialSlots = specialSlotEquipmentService.getEquippedSpecialSlots(player);
            List<Integer> equippedCardSlots = cardSlotEquipmentService.getEquippedCardSlots(player);

            S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 0, equippedCloths, player, statusPointsAddedDto);
            S2CInventoryWearQuickAnswerPacket inventoryWearQuickAnswerPacket = new S2CInventoryWearQuickAnswerPacket(equippedQuickSlots);
            S2CInventoryWearToolAnswerPacket inventoryWearToolAnswerPacket = new S2CInventoryWearToolAnswerPacket(equippedToolSlots);
            S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(equippedSpecialSlots);
            S2CInventoryWearCardAnswerPacket inventoryWearCardAnswerPacket = new S2CInventoryWearCardAnswerPacket(equippedCardSlots);

            connection.sendTCP(inventoryWearClothAnswerPacket, inventoryWearQuickAnswerPacket, inventoryWearToolAnswerPacket, inventoryWearSpecialAnswerPacket, inventoryWearCardAnswerPacket);
        }

        Packet answer = new Packet((char) (packet.getPacketId() + 1));
        answer.write((char) 0);
        connection.sendTCP(answer);
    }
}
