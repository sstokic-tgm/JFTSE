package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildLeaveAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;

@PacketOperationIdentifier(PacketOperations.C2SGuildLeaveRequest)
public class GuildLeaveRequestPacketHandler extends AbstractPacketHandler {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildLeaveRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember.getMemberRank() == 3) {
            S2CGuildLeaveAnswerPacket guildLeaveAnswerPacket = new S2CGuildLeaveAnswerPacket((char) -2);
            connection.sendTCP(guildLeaveAnswerPacket);
        } else {
            Guild guild = guildMember.getGuild();
            guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
            guildService.save(guild);

            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, guild));
        }
    }
}
