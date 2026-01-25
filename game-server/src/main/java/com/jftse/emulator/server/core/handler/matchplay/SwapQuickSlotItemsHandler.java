package com.jftse.emulator.server.core.handler.matchplay;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayGivePlayerSkills;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.shared.packets.matchplay.CMSGSwapSpell;

@PacketId(CMSGSwapSpell.PACKET_ID)
public class SwapQuickSlotItemsHandler implements PacketHandler<FTConnection, CMSGSwapSpell> {
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;

    public SwapQuickSlotItemsHandler() {
        pocketService = ServiceManager.getInstance().getPocketService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGSwapSpell packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer() || ftClient.getActiveGameSession() == null || ftClient.getActiveRoom() == null )
            return;

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer == null)
            return;

        Pocket pocket = pocketService.findById(roomPlayer.getPocketId());
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(21, EItemCategory.SPECIAL.getName(), pocket);
        if (playerPocket != null) {
            playerPocket = playerPocketService.decrementPocketItemCount(playerPocket);
            if (playerPocket.getItemCount() == 0) {
                playerPocketService.remove(playerPocket.getId());
                pocketService.decrementPocketBelongings(pocket);
            }
        }

        S2CMatchplayGivePlayerSkills givePlayerSkills
                = new S2CMatchplayGivePlayerSkills(roomPlayer.getPosition(), packet.getTargetLeftSlotSkill(), packet.getTargetRightSlotSkill());
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(givePlayerSkills, connection);
    }
}
