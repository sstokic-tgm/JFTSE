package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CDisconnectAnswerPacket;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ResetLoginStatusCommand extends Command {
    public ResetLoginStatusCommand() {
        setDescription("Resets login status of a player");
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (params.size() < 1) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Use -rsLogin <playerName>");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Use -rsLogin <playerName>");
            connection.sendTCP(answer);
            return;
        }

        boolean successfullyReseted = false;
        String playerName = params.get(0);
        final ConcurrentLinkedDeque<FTClient> clients = GameManager.getInstance().getClients();
        for (Iterator<FTClient> it = clients.iterator(); it.hasNext(); ) {
            FTClient client = it.next();

            if (client != null && client.getPlayer() != null) {
                Player activePlayer = client.getPlayer();
                if (activePlayer.getName().equals(playerName) && client.getConnection() != null) {
                    Account account = client.getAccount();
                    if (account != null) {
                        if (account.getStatus() == AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN) {
                            account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                            client.saveAccount(account);
                        }
                    }

                    S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
                    client.getConnection().sendTCP(disconnectAnswerPacket);
                    client.getConnection().close();

                    successfullyReseted = true;
                    break;
                }
            }
        }
        if (!successfullyReseted) {
            Player player = ServiceManager.getInstance().getPlayerService().findByName(playerName);
            if (player != null) {
                Account playerAccount = ServiceManager.getInstance().getAuthenticationService().findAccountById(player.getAccount().getId());
                if (playerAccount != null) {
                    if (playerAccount.getStatus() == AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN) {
                        playerAccount.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                        ServiceManager.getInstance().getAuthenticationService().updateAccount(playerAccount);

                        successfullyReseted = true;
                    }
                }
            }
        }

        Packet answer;
        if (successfullyReseted) {
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player " + playerName + " status has been reseted");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player " + playerName + " status has been reseted");
        } else {
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player " + playerName + " status couldn't be reseted");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player " + playerName + " status couldn't be reseted");
        }
        connection.sendTCP(answer);
    }
}
