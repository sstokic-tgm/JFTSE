package com.jftse.emulator.server.game.core.packet.packets.lobby;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CLobbyUserInfoAnswerPacket extends Packet {
    public S2CLobbyUserInfoAnswerPacket(char result, Player player) {
        super(PacketID.S2CLobbyUserInfoAnswer);

        this.write(result);
        if (player != null) {
            PlayerStatistic playerStatistic = player.getPlayerStatistic();
            this.write(Math.toIntExact(player.getId()));
            this.write(player.getLevel());

            this.write(playerStatistic.getBasicRecordWin());
            this.write(playerStatistic.getBasicRecordLoss());
            this.write(playerStatistic.getBattleRecordWin());
            this.write(playerStatistic.getBattleRecordLoss());
            this.write(playerStatistic.getConsecutiveWins());
            this.write(0);
            this.write(playerStatistic.getNumberOfDisconnects());
            this.write(playerStatistic.getTotalGames());
            this.write(0);
            this.write(0);

            this.write(0); // perfect(s)
            this.write(0); // guard break(s)
            this.write(""); // guild name

            // ?? 2x player info play stats data with a boolean true which adds 3 additional int to be written otherwise there would be 20 ints only
            for (int i = 0; i < 26; i++)
                this.write(0);

            // should be guild stuff, like, the first(or last) int should be the emblem id(but the id saved at our side) and rest are color etc for guild emblem
            for (int i = 0; i < 6; i++)
                this.write(0);

            this.write((byte) 0); // if 0 charming points are shown, if > 0 then no charming points shown but player lvl and grade get 1 and Chicky
            this.write(""); // couple
            this.write(0); // charming points
            this.write(0);
            this.write((short) 0); // emblem slot 1
            this.write((short) 0); // emblem slot 2
            this.write((short) 0); // emblem slot 3
            this.write((short) 0); // emblem slot 4
        }
    }
}