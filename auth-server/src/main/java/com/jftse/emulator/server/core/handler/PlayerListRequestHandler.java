package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.auth.CMSGRequestPlayerList;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerList;

import java.util.List;

@PacketId(CMSGRequestPlayerList.PACKET_ID)
public class PlayerListRequestHandler implements PacketHandler<FTConnection, CMSGRequestPlayerList> {
    private final PlayerService playerService;

    public PlayerListRequestHandler() {
        this.playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRequestPlayerList packet) {
        FTClient client = connection.getClient();
        if (client == null) {
            return;
        }

        Long accountId = client.getAccountId();
        if (accountId == null) {
            return;
        }

        int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(accountId);
        List<Player> playerList = playerService.findAllByAccount(accountId);

        SMSGPlayerList playerListPacket = SMSGPlayerList.builder()
                .account(
                        com.jftse.server.core.shared.packets.auth.Account.builder()
                                .id(Math.toIntExact(accountId))
                                .id2(Math.toIntExact(accountId))
                                .tutorialCount((byte) tutorialCount)
                                .gameMaster(client.isGameMaster())
                                .lastPlayedPlayerId(Math.toIntExact(client.getLastPlayedPlayerId() == null ? 0 : client.getLastPlayedPlayerId()))
                                .build()
                )
                .players(playerList.stream().map(p -> com.jftse.server.core.shared.packets.auth.Player.builder()
                        .id(Math.toIntExact(p.getId()))
                        .name(p.getName())
                        .level(p.getLevel())
                        .created(p.getAlreadyCreated())
                        .canDelete(!p.getFirstPlayer())
                        .gold(p.getGold())
                        .playerType(p.getPlayerType())
                        .str(p.getStrength())
                        .sta(p.getStamina())
                        .dex(p.getDexterity())
                        .wil(p.getWillpower())
                        .statPoints(p.getStatusPoints())
                        .oldRenameAllowed(false)
                        .renameAllowed(p.getNameChangeAllowed())
                        .clothEquipment(com.jftse.server.core.shared.packets.auth.ClothEquipment.builder()
                                .hair(p.getClothEquipment().getHair())
                                .face(p.getClothEquipment().getFace())
                                .dress(p.getClothEquipment().getDress())
                                .pants(p.getClothEquipment().getPants())
                                .socks(p.getClothEquipment().getSocks())
                                .shoes(p.getClothEquipment().getShoes())
                                .gloves(p.getClothEquipment().getGloves())
                                .racket(p.getClothEquipment().getRacket())
                                .glasses(p.getClothEquipment().getGlasses())
                                .bag(p.getClothEquipment().getBag())
                                .hat(p.getClothEquipment().getHat())
                                .dye(p.getClothEquipment().getDye())
                                .build()
                        )
                        .build()).toList()
                ).build();
        connection.sendTCP(playerListPacket);
    }
}
