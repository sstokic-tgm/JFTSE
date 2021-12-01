package com.jftse.emulator.server.core.handler.game.matchplay;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.item.EItemCategory;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.matchplay.C2SMatchplaySwapQuickSlotItems;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayGivePlayerSkills;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.networking.packet.Packet;

public class SwapQuickSlotItemsHandler extends AbstractHandler {
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
        if (connection.getClient() == null || connection.getClient().getActiveGameSession() == null
                || connection.getClient().getActiveRoom() == null || connection.getClient().getActivePlayer() == null)
            return;

        RoomPlayer roomPlayer = connection.getClient().getActiveRoom().getRoomPlayerList().stream()
                .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findFirst()
                .orElse(null);
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
        GameManager.getInstance().sendPacketToAllClientsInSameGameSession(givePlayerSkills, connection);
    }
}
