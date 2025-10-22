package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.item.ItemChar;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.auth.CMSGPlayerCreate;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerCreate;

@PacketId(CMSGPlayerCreate.PACKET_ID)
public class PlayerCreatePacketHandler implements PacketHandler<FTConnection, CMSGPlayerCreate> {
    private final ProfaneWordsService profaneWordsService;
    private final PlayerService playerService;
    private final ItemCharService itemCharService;
    private final HomeService homeService;
    private final ClothEquipmentService clothEquipmentService;

    public PlayerCreatePacketHandler() {
        profaneWordsService = ServiceManager.getInstance().getProfaneWordsService();
        playerService = ServiceManager.getInstance().getPlayerService();
        itemCharService = ServiceManager.getInstance().getItemCharService();
        homeService = ServiceManager.getInstance().getHomeService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlayerCreate playerCreatePacket) {
        String playerName = playerCreatePacket.getNickname();
        boolean isPlayerNameValid = !profaneWordsService.textContainsProfaneWord(playerName);

        Player player = playerService.findByIdFetched((long) playerCreatePacket.getPlayerId());
        if (player == null || !isPlayerNameValid) {
            SMSGPlayerCreate playerCreate = SMSGPlayerCreate.builder().result((char) -1).build();
            connection.sendTCP(playerCreate);
        } else {
            if (playerService.findByName(playerCreatePacket.getNickname()) != null) {
                SMSGPlayerCreate playerCreate = SMSGPlayerCreate.builder().result((char) -1).build();
                connection.sendTCP(playerCreate);
            } else {
                if (player.getAlreadyCreated()) {
                    SMSGPlayerCreate playerCreate = SMSGPlayerCreate.builder().result((char) -2).build();
                    connection.sendTCP(playerCreate);
                    return;
                }

                player.setName(playerCreatePacket.getNickname());
                player.setAlreadyCreated(true);

                if (playerService.isStatusPointHack(playerCreatePacket, player)) {
                    ItemChar itemChar = itemCharService.findByPlayerType(player.getPlayerType());

                    player.setStrength(itemChar.getStrength());
                    player.setStamina(itemChar.getStamina());
                    player.setDexterity(itemChar.getDexterity());
                    player.setWillpower(itemChar.getWillpower());

                    player.setStatusPoints((byte) 5);
                } else {
                    player.setStrength(playerCreatePacket.getStrength());
                    player.setStamina(playerCreatePacket.getStamina());
                    player.setDexterity(playerCreatePacket.getDexterity());
                    player.setWillpower(playerCreatePacket.getWillpower());

                    player.setStatusPoints(playerCreatePacket.getStatusPoints());
                }

                player.setLevel((byte) 1);
                player.setExpPoints(0);
                player.setGold(10000);

                player = playerService.save(player);

                if (homeService.findAccountHomeByAccountId(player.getAccount().getId()) == null) {
                    AccountHome accountHome = new AccountHome();
                    accountHome.setAccount(player.getAccount());

                    accountHome = homeService.save(accountHome);
                }

                SMSGPlayerCreate playerCreate = SMSGPlayerCreate.builder().result((char) 0).build();
                connection.sendTCP(playerCreate);
            }
        }
    }
}
