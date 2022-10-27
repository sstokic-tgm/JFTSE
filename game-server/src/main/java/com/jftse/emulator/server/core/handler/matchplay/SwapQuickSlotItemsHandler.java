package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplaySwapQuickSlotItems;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGivePlayerSkills;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;

@PacketOperationIdentifier(PacketOperations.C2SMatchplaySwapQuickSlotItems)
public class SwapQuickSlotItemsHandler extends AbstractPacketHandler {
    private C2SMatchplaySwapQuickSlotItems swapQuickSlotItems;

    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;

    public SwapQuickSlotItemsHandler() {
        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        swapQuickSlotItems = new C2SMatchplaySwapQuickSlotItems(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getActiveGameSession() == null
                || ftClient.getActiveRoom() == null || ftClient.getPlayer() == null)
            return;

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null)
            return;

        Pocket pocket = pocketService.findById(roomPlayer.getPlayer().getPocket().getId());
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(21, EItemCategory.SPECIAL.getName(), pocket);
        if (playerPocket != null) {
            playerPocket = playerPocketService.decrementPocketItemCount(playerPocket);
            if (playerPocket.getItemCount() == 0) {
                playerPocketService.remove(playerPocket.getId());
                pocketService.decrementPocketBelongings(pocket);
            }
        }

        S2CMatchplayGivePlayerSkills givePlayerSkills
                = new S2CMatchplayGivePlayerSkills(roomPlayer.getPosition(), swapQuickSlotItems.getTargetLeftSlotSkill(), swapQuickSlotItems.getTargetRightSlotSkill());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(givePlayerSkills, ftClient.getConnection());
    }
}
