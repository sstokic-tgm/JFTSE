package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;

@PacketOperationIdentifier(PacketOperations.C2SGuildDataRequest)
public class GuildDataRequestPacketHandler extends AbstractPacketHandler {
    private final GuildMemberService guildMemberService;

    public GuildDataRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null) {
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
            return;
        }

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember == null)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
        else if (guildMember.getWaitingForApproval())
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -1, guildMember.getGuild()));
        else
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guildMember.getGuild()));
    }
}
