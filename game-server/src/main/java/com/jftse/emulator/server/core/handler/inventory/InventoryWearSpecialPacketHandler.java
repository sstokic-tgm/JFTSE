package com.jftse.emulator.server.core.handler.inventory;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.inventory.S2CInventoryWearSpecialAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.SpecialSlotEquipmentService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearSpecial;
import lombok.extern.log4j.Log4j2;

@Log4j2
@PacketId(CMSGInventoryWearSpecial.PACKET_ID)
public class InventoryWearSpecialPacketHandler implements PacketHandler<FTConnection, CMSGInventoryWearSpecial> {
    private final SpecialSlotEquipmentService specialSlotEquipmentService;

    public InventoryWearSpecialPacketHandler() {
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGInventoryWearSpecial packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();
        specialSlotEquipmentService.updateSpecialSlots(player.getPlayer(), packet.getSpecialSlotList());
        player.loadSpecialSlots();

        S2CInventoryWearSpecialAnswerPacket inventoryWearSpecialAnswerPacket = new S2CInventoryWearSpecialAnswerPacket(packet.getSpecialSlotList());
        connection.sendTCP(inventoryWearSpecialAnswerPacket);
    }
}
