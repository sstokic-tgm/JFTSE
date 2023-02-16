package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.Command;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;

import java.util.List;

public class UnbanPlayerCommand extends Command {

    private final AuthenticationService authenticationService;
    private final PlayerService playerService;

    public UnbanPlayerCommand() {
        setDescription("Unbans the given player");

        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (params.size() < 1) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Use -unban <playerName>");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Use -unban <playerName>");
            connection.sendTCP(answer);
            return;
        }

        String playerName = params.get(0);
        Player playerToUnban = playerService.findByName(playerName);
        if (playerToUnban == null) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player not found");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player not found");
            connection.sendTCP(answer);
            return;
        }

        Account accountToUnban = authenticationService.findAccountById(playerToUnban.getAccount().getId());
        if (accountToUnban != null) {
            accountToUnban.setStatus((int) AuthenticationServiceImpl.SUCCESS);
            accountToUnban.setBanReason(null);
            accountToUnban.setBannedUntil(null);

            authenticationService.updateAccount(accountToUnban);

            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player " + playerName + " has been unbanned");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player " + playerName + " has been unbanned");
            connection.sendTCP(answer);
        }
    }
}
