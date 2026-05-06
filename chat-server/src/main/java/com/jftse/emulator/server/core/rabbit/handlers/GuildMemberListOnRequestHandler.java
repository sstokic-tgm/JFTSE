package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.GuildMemberListOnRequestMessage;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class GuildMemberListOnRequestHandler extends AbstractMessageHandler<GuildMemberListOnRequestMessage> {
    @Autowired
    private GameManager gameManager;
    @Autowired
    private SocialService socialService;
    @Autowired
    private PlayerService playerService;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register(MessageTypes.GUILD_MEMBER_LIST_ON_REQUEST.getValue(), this);
    }

    @Override
    public void handle(GuildMemberListOnRequestMessage message) {
        log.info("Player {} requested guild member list", message.getPlayerId());

        final FTConnection connection = gameManager.getConnectionByPlayerId(message.getPlayerId());
        List<Player> guildMembers = null;
        if (connection != null) {
            final FTClient client = connection.getClient();
            // shouldn't be null, but just in case
            if (client == null) {
                log.error("Client is null for player {} on server {}", message.getPlayerId(), gameManager.getServer());
                return;
            }

            FTPlayer activePlayer = client.getPlayer();
            guildMembers = socialService.getGuildMemberList(activePlayer.getPlayerRef()).stream()
                    .map(GuildMember::getPlayer)
                    .toList();

            S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
            connection.sendTCP(s2CClubMembersListAnswerPacket);
        }

        if (guildMembers == null || guildMembers.isEmpty()) {
            Player player = playerService.findById(message.getPlayerId());
            if (player == null) {
                log.error("Player {} not found", message.getPlayerId());
                return;
            }

            guildMembers = socialService.getGuildMemberList(player).stream()
                    .map(GuildMember::getPlayer)
                    .toList();
        }

        guildMembers.forEach(gm -> {
            List<Player> otherGuildMembers = socialService.getGuildMemberList(gm).stream()
                    .map(GuildMember::getPlayer)
                    .toList();

            S2CClubMembersListAnswerPacket otherGuildMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(otherGuildMembers);
            FTConnection guildMemberConnection = GameManager.getInstance().getConnectionByPlayerId(gm.getId());
            if (guildMemberConnection != null) {
                guildMemberConnection.sendTCP(otherGuildMembersListAnswerPacket);
            }
        });

        log.info("Guild member list refreshed for player {}", message.getPlayerId());
    }
}
