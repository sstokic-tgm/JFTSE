package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildLeagueList;
import com.jftse.server.core.shared.packets.guild.GuildLeagueInfo;
import com.jftse.server.core.shared.packets.guild.SMSGGuildLeagueList;

import java.util.List;

@PacketId(CMSGGuildLeagueList.PACKET_ID)
public class GuildLeagueListRequestHandler implements PacketHandler<FTConnection, CMSGGuildLeagueList> {
    private final GuildService guildService;

    private static final int SUCCESS = 0;
    private static final int FAILURE = -2;

    public GuildLeagueListRequestHandler() {
        this.guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildLeagueList packet) {
        FTClient client = connection.getClient();

        int requestedPage = packet.getPage();
        int highestLoadedPage = client.getHighestLoadedGuildLeaguePage();

        if (requestedPage <= highestLoadedPage && requestedPage != 1) {
            SMSGGuildLeagueList leagueListPacket = SMSGGuildLeagueList.builder()
                    .state((byte) SUCCESS)
                    .guildInfoList(List.of())
                    .build();
            connection.sendTCP(leagueListPacket);
            return;
        }

        client.setHighestLoadedGuildLeaguePage(packet.getPage());

        List<Guild> guildList = guildService.findAllGuildLeagues(requestedPage);
        if (guildList == null || guildList.isEmpty()) {
            SMSGGuildLeagueList leagueListPacket = SMSGGuildLeagueList.builder()
                    .state((byte) FAILURE)
                    .build();
            connection.sendTCP(leagueListPacket);

            client.setHighestLoadedGuildLeaguePage(highestLoadedPage);
            return;
        }

        List<GuildLeagueInfo> guildLeagueInfoList = guildList.stream()
                .map(guild -> GuildLeagueInfo.builder()
                        .name(guild.getName())
                        .level(guild.getLevel())
                        .leaguePoints(guild.getLeaguePoints())
                        .wins(guild.getBattleRecordWin())
                        .losses(guild.getBattleRecordLoose())
                        .logoBackgroundId(guild.getLogoBackgroundId())
                        .logoBackgroundColor(guild.getLogoBackgroundColor())
                        .logoPatternId(guild.getLogoPatternId())
                        .logoPatternColor(guild.getLogoPatternColor())
                        .logoMarkId(guild.getLogoMarkId())
                        .logoMarkColor(guild.getLogoMarkColor())
                        .build())
                .toList();

        SMSGGuildLeagueList leagueListPacket = SMSGGuildLeagueList.builder()
                .state((byte) SUCCESS)
                .guildInfoList(guildLeagueInfoList)
                .build();
        connection.sendTCP(leagueListPacket);
    }
}
