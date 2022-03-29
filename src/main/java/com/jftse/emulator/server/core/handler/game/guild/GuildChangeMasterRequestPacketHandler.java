package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChangeMasterRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildChangeMasterAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildChangeMasterRequestPacketHandler extends AbstractHandler {
    private C2SGuildChangeMasterRequestPacket guildChangeMasterRequestPacket;

    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;

    public GuildChangeMasterRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChangeMasterRequestPacket = new C2SGuildChangeMasterRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null) {
            connection.sendTCP(new S2CGuildChangeMasterAnswerPacket((short) -1));
            return;
        }

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
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
