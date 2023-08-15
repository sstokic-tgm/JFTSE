package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.AbstractCommand;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ServerKickCommand extends AbstractCommand {

    public ServerKickCommand() {
        setDescription("Kicks player from server");
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (params.size() < 1) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Use -serverKick <playerName>");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Use -serverKick <playerName>");
            connection.sendTCP(answer);
            return;
        }

        boolean successfullyKicked = false;
        String playerName = params.get(0);
        final ConcurrentLinkedDeque<FTClient> clients = GameManager.getInstance().getClients();
        for (Iterator<FTClient> it = clients.iterator(); it.hasNext(); ) {
            FTClient client = it.next();

            if (client != null && client.getPlayer() != null) {
                Player activePlayer = client.getPlayer();
                if (activePlayer.getName().equals(playerName) && client.getConnection() != null) {
                    Account account = client.getAccount();
                    if (account != null) {
                        if (account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID)
                            account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                        client.saveAccount(account);
                    }

                    S2CDCMsgPacket msgPacket = new S2CDCMsgPacket(1);
                    client.getConnection().sendTCP(msgPacket);
                    client.getConnection().close();

                    successfullyKicked = true;
                    break;
                }
            }
        }

        Packet answer;
        if (successfullyKicked) {
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player " + playerName + " has been kicked from the server");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player " + playerName + " has been kicked from the server");
        } else {
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player " + playerName + " couldn't be kicked from the server");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player " + playerName + " couldn't be kicked from the server");
        }
        connection.sendTCP(answer);
    }
}
