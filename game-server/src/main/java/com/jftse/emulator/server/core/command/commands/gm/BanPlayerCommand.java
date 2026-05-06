package com.jftse.emulator.server.core.command.commands.gm;

import com.jftse.emulator.server.core.command.AbstractCommand;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class BanPlayerCommand extends AbstractCommand {

    private final AuthenticationService authenticationService;
    private final PlayerService playerService;

    public BanPlayerCommand() {
        setDescription("Bans the given player");

        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
        this.playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void execute(FTConnection connection, List<String> params) {
        if (params.size() < 3) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Use -ban <playerName> <\"banReason\"> <daysToBan>");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Use -ban <playerName> <\"banReason\"> <daysToBan>");
            connection.sendTCP(answer);
            return;
        }

        String playerName = params.get(0);
        String banReason = params.get(1);
        int daysToBan = Integer.parseInt(params.get(2));
        Player playerToBan = playerService.findByName(playerName);
        if (playerToBan == null) {
            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player not found");
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player not found");
            connection.sendTCP(answer);
            return;
        }

        Account accountToBan = authenticationService.findAccountById(playerToBan.getAccount().getId());
        if (accountToBan != null) {
            accountToBan.setStatus((int) AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID);
            accountToBan.setBanReason(banReason);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_YEAR, daysToBan);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            accountToBan.setBannedUntil(cal.getTime());

            authenticationService.updateAccount(accountToBan);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

            Packet answer;
            if (connection.getClient().isInLobby())
                answer = new S2CChatLobbyAnswerPacket((char) 0, "Command", "Player " + playerName + " has been banned until " + sdf.format(cal.getTime()));
            else
                answer = new S2CChatRoomAnswerPacket((byte) 2, "Room", "Player " + playerName + " has been banned until " + sdf.format(cal.getTime()));
            connection.sendTCP(answer);
        }
    }
}
