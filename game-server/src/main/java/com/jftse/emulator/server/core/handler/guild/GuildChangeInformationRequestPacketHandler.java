package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.packets.guild.C2SGuildChangeInformationRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
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

@PacketOperationIdentifier(PacketOperations.C2SGuildChangeInformationRequest)
public class GuildChangeInformationRequestPacketHandler extends AbstractPacketHandler {
    private C2SGuildChangeInformationRequestPacket guildChangeInformationRequestPacket;

    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildChangeInformationRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChangeInformationRequestPacket = new C2SGuildChangeInformationRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            Guild guild = guildMember.getGuild();

            guild.setIntroduction(guildChangeInformationRequestPacket.getIntroduction());
            guild.setLevelRestriction(guildChangeInformationRequestPacket.getMinLevel());
            guild.setIsPublic(guildChangeInformationRequestPacket.isPublic());
            guild.setAllowedCharacterType(guildChangeInformationRequestPacket.getAllowedCharacterType());
            guildService.save(guild);

            connection.sendTCP(new S2CGuildDataAnswerPacket((byte) 0, guild));
        }
    }
}
