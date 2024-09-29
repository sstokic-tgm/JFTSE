package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.C2SGuildChangeMasterRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildChangeMasterAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;

@PacketOperationIdentifier(PacketOperations.C2SGuildChangeMasterRequest)
public class GuildChangeMasterRequestPacketHandler extends AbstractPacketHandler {
    private C2SGuildChangeMasterRequestPacket guildChangeMasterRequestPacket;

    private final GuildMemberService guildMemberService;

    public GuildChangeMasterRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChangeMasterRequestPacket = new C2SGuildChangeMasterRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null) {
            connection.sendTCP(new S2CGuildChangeMasterAnswerPacket((short) -1));
            return;
        }

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            GuildMember newClubMaster = GameManager.getInstance().getGuildMemberByPlayerPositionInGuild(
                    guildChangeMasterRequestPacket.getPlayerPositionInGuild(),
                    guildMember);

            if (newClubMaster != null) {
                guildMember.setMemberRank((byte) 2);
                guildMemberService.save(guildMember);

                newClubMaster.setMemberRank((byte) 3);
                guildMemberService.save(newClubMaster);

                connection.sendTCP(new S2CGuildChangeMasterAnswerPacket((short) 0));
            }
        } else {
            connection.sendTCP(new S2CGuildChangeMasterAnswerPacket((short) -1));
        }
    }
}
