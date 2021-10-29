package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.database.model.home.AccountHome;
import com.jftse.emulator.server.database.model.item.ItemChar;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.player.C2SPlayerCreatePacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerCreateAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.networking.packet.Packet;

public class PlayerCreatePacketHandler extends AbstractHandler {
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
        }
        else {
            if (playerService.findByName(playerCreatePacket.getNickname()) != null) {
                S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) -1);
                connection.sendTCP(playerCreateAnswerPacket);
            }
            else {
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

                    player.setStatusPoints((byte) (player.getStatusPoints() + 19));
                }
                else {
                    player.setStrength(playerCreatePacket.getStrength());
                    player.setStamina(playerCreatePacket.getStamina());
                    player.setDexterity(playerCreatePacket.getDexterity());
                    player.setWillpower(playerCreatePacket.getWillpower());

                    player.setStatusPoints((byte) (playerCreatePacket.getStatusPoints() + 19));
                }

                // make every new char level 20 - only temporary
                player.setLevel((byte) 20);
                player.setExpPoints(15623);
                player.setGold(100000);

                player = playerService.save(player);

                if (homeService.findAccountHomeByAccountId(player.getAccount().getId()) == null) {
                    AccountHome accountHome = new AccountHome();
                    accountHome.setAccount(player.getAccount());

                    accountHome = homeService.save(accountHome);
                }

                S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) 0);
                connection.sendTCP(playerCreateAnswerPacket);

                StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

                S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
                connection.sendTCP(playerStatusPointChangePacket);
            }
        }
    }
}
