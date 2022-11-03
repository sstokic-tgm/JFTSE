package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.S2CPlayerCreateAnswerPacket;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.item.ItemChar;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.player.C2SPlayerCreatePacket;

@PacketOperationIdentifier(PacketOperations.C2SPlayerCreate)
public class PlayerCreatePacketHandler extends AbstractPacketHandler {
    private C2SPlayerCreatePacket playerCreatePacket;

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
    public boolean process(Packet packet) {
        playerCreatePacket = new C2SPlayerCreatePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        String playerName = playerCreatePacket.getNickname();
        boolean isPlayerNameValid = !profaneWordsService.textContainsProfaneWord(playerName);

        Player player = playerService.findByIdFetched((long) playerCreatePacket.getPlayerId());
        if (player == null || !isPlayerNameValid) {
            S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) -1);
            connection.sendTCP(playerCreateAnswerPacket);
        } else {
            if (playerService.findByName(playerCreatePacket.getNickname()) != null) {
                S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) -1);
                connection.sendTCP(playerCreateAnswerPacket);
            } else {
                if (player.getAlreadyCreated()) {
                    S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) -2);
                    connection.sendTCP(playerCreateAnswerPacket);
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

                S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) 0);
                connection.sendTCP(playerCreateAnswerPacket);
            }
        }
    }
}
