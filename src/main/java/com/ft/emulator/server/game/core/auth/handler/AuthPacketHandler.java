package com.ft.emulator.server.game.core.auth.handler;

import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.home.AccountHome;
import com.ft.emulator.server.database.model.player.*;
import com.ft.emulator.server.database.model.pocket.Pocket;
import com.ft.emulator.server.game.core.packet.packets.S2CDisconnectAnswerPacket;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.game.core.packet.packets.authserver.*;
import com.ft.emulator.server.game.core.packet.packets.player.*;
import com.ft.emulator.server.game.core.service.*;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthPacketHandler {
    private final AuthenticationService authenticationService;
    private final PlayerService playerService;
    private final PocketService pocketService;
    private final HomeService homeService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final PlayerStatisticService playerStatisticService;

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleLoginPacket(Connection connection, Packet packet) {
        C2SLoginPacket loginPacket = new C2SLoginPacket(packet);

        // version check
        if (loginPacket.getVersion() != 21108180) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.INVAILD_VERSION);
            connection.sendTCP(loginAnswerPacket);

            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
        }

        Account account = authenticationService.login(loginPacket.getUsername(), loginPacket.getPassword());

        if (account == null) {
            S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.ACCOUNT_INVALID_USER_ID);
            connection.sendTCP(loginAnswerPacket);
        }
        else {
            Integer accountStatus = account.getStatus();
            if (!accountStatus.equals((int) S2CLoginAnswerPacket.SUCCESS)) {
                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(accountStatus.shortValue());
                connection.sendTCP(loginAnswerPacket);
            }
            else {
                // set last login date
                account.setLastLogin(new Date());
                // mark as logged in
                account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
                account = authenticationService.updateAccount(account);

                connection.getClient().setAccount(account);

                S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.SUCCESS);
                connection.sendTCP(loginAnswerPacket);

                S2CPlayerListPacket PlayerListPacket = new S2CPlayerListPacket(account, account.getPlayerList());
                connection.sendTCP(PlayerListPacket);

                S2CGameServerListPacket gameServerListPacket = new S2CGameServerListPacket(authenticationService.getGameServerList());
                connection.sendTCP(gameServerListPacket);
            }
        }
    }

    public void handleFirstPlayerPacket(Connection connection, Packet packet) {
        C2SFirstPlayerPacket firstPlayerPacket = new C2SFirstPlayerPacket(packet);
        Client client = connection.getClient();

        if (client.getAccount().getPlayerList().isEmpty()) {
            Player player = new Player();
            player.setAccount(client.getAccount());
            player.setPlayerType(firstPlayerPacket.getPlayerType());
            player.setFirstPlayer(true);

            ClothEquipment clothEquipment = new ClothEquipment();
            clothEquipment = clothEquipmentService.save(clothEquipment);
            player.setClothEquipment(clothEquipment);

            QuickSlotEquipment quickSlotEquipment = new QuickSlotEquipment();
            quickSlotEquipment = quickSlotEquipmentService.save(quickSlotEquipment);
            player.setQuickSlotEquipment(quickSlotEquipment);

            Pocket pocket = new Pocket();
            pocket = pocketService.save(pocket);
            player.setPocket(pocket);

            PlayerStatistic playerStatistic = new PlayerStatistic();
            playerStatistic = playerStatisticService.save(playerStatistic);
            player.setPlayerStatistic(playerStatistic);

            player = playerService.save(player);

            AccountHome accountHome = new AccountHome();
            accountHome.setAccount(client.getAccount());
            accountHome = homeService.save(accountHome);

            S2CFirstPlayerAnswerPacket firstPlayerAnswerPacket = new S2CFirstPlayerAnswerPacket((char) 0, player.getId(), player.getPlayerType());
            connection.sendTCP(firstPlayerAnswerPacket);
        }
        else {
            S2CFirstPlayerAnswerPacket firstPlayerAnswerPacket = new S2CFirstPlayerAnswerPacket((char) -1, 0L, (byte) 0);
            connection.sendTCP(firstPlayerAnswerPacket);
        }
    }

    public void handlePlayerNameCheckPacket(Connection connection, Packet packet) {
        C2SPlayerNameCheckPacket playerNameCheckPacket = new C2SPlayerNameCheckPacket(packet);

        Player player = playerService.findByName(playerNameCheckPacket.getNickname());
        if (player == null) {
            S2CPlayerNameCheckAnswerPacket playerNameCheckAnswerPacket = new S2CPlayerNameCheckAnswerPacket((char) 0);
            connection.sendTCP(playerNameCheckAnswerPacket);
        }
        else {
            S2CPlayerNameCheckAnswerPacket playerNameCheckAnswerPacket = new S2CPlayerNameCheckAnswerPacket((char) -1);
            connection.sendTCP(playerNameCheckAnswerPacket);
        }
    }

    public void handlePlayerCreatePacket(Connection connection, Packet packet) {

        C2SPlayerCreatePacket playerCreatePacket = new C2SPlayerCreatePacket(packet);

        Player player = playerService.findByIdFetched((long) playerCreatePacket.getPlayerId());
        if (player == null) {
            S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) -1);
            connection.sendTCP(playerCreateAnswerPacket);
        }
        else {
            if (playerService.findByName(playerCreatePacket.getNickname()) != null) {
                S2CPlayerCreateAnswerPacket playerCreateAnswerPacket = new S2CPlayerCreateAnswerPacket((char) -1);
                connection.sendTCP(playerCreateAnswerPacket);
            }
            else {
                player.setName(playerCreatePacket.getNickname());
                player.setAlreadyCreated(true);
                player.setStrength(playerCreatePacket.getStrength());
                player.setStamina(playerCreatePacket.getStamina());
                player.setDexterity(playerCreatePacket.getDexterity());
                player.setWillpower(playerCreatePacket.getWillpower());

                // make every new char level 20 - only temporary
                player.setStatusPoints((byte) (playerCreatePacket.getStatusPoints() + 20));
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

    public void handlePlayerDeletePacket(Connection connection, Packet packet) {
        C2SPlayerDeletePacket playerDeletePacket = new C2SPlayerDeletePacket(packet);

        Player player = playerService.findById((long) playerDeletePacket.getPlayerId());
        if (player != null) {

            connection.getClient().getAccount().getPlayerList().removeIf(pl -> pl.getId().equals(player.getId()));
            playerService.remove(player.getId());

            S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) 0);
            connection.sendTCP(playerDeleteAnswerPacket);

            S2CPlayerListPacket playerListPacket = new S2CPlayerListPacket(connection.getClient().getAccount(), connection.getClient().getAccount().getPlayerList());
            connection.sendTCP(playerListPacket);
        }
        else {
            S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) -1);
            connection.sendTCP(playerDeleteAnswerPacket);
        }
    }

    public void handleAuthServerLoginPacket(Connection connection, Packet packet) {

        C2SAuthLoginPacket authLoginPacket = new C2SAuthLoginPacket(packet);

        Account account = authenticationService.findAccountByUsername(authLoginPacket.getUsername());
        if (account != null) {
            account.setStatus((int) S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);
            account = authenticationService.updateAccount(account);

            connection.getClient().setAccount(account);

            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) 0, (byte) 1);
            connection.sendTCP(authLoginAnswerPacket);

            S2CPlayerListPacket PlayerListPacket = new S2CPlayerListPacket(account, account.getPlayerList());
            connection.sendTCP(PlayerListPacket);
        }
        else {
            S2CAuthLoginPacket authLoginAnswerPacket = new S2CAuthLoginPacket((char) -1, (byte) 0);
            connection.sendTCP(authLoginAnswerPacket);
        }
    }

    public void handleDisconnectPacket(Connection connection, Packet packet) {
        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
            account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
            authenticationService.updateAccount(account);
        }

        S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
        connection.sendTCP(disconnectAnswerPacket);
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient().getAccount() != null) {
            // reset status
            Account account = authenticationService.findAccountById(connection.getClient().getAccount().getId());
            account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
            authenticationService.updateAccount(account);
        }

        connection.setClient(null);
        connection.close();
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        unknownAnswer.write((short) 0);
        connection.sendTCP(unknownAnswer);
    }
}
