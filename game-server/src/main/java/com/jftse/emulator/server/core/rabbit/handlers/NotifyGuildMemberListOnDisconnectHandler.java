package com.jftse.emulator.server.core.rabbit.handlers;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.rabbit.MessageTypes;
import com.jftse.emulator.server.core.rabbit.messages.NotifyGuildMemberListOnDisconnectMessage;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.rabbit.AbstractMessageHandler;
import com.jftse.server.core.rabbit.MessageHandlerRegistry;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class NotifyGuildMemberListOnDisconnectHandler extends AbstractMessageHandler<NotifyGuildMemberListOnDisconnectMessage> {
    @Autowired
    private GameManager gameManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private GuildMemberService guildMemberService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private SocialService socialService;

    @Override
    public void register(MessageHandlerRegistry registry) {
        registry.register(MessageTypes.GUILD_MEMBER_LIST_ON_DISCONNECT.getValue(), this);
    }

    @Override
    public void handle(NotifyGuildMemberListOnDisconnectMessage message) {
        log.info("Player {} disconnected, notifying guild members", message.getPlayerId());

        Player player = playerService.findById(message.getPlayerId());
        if (player == null) {
            log.error("Player {} not found", message.getPlayerId());
            return;
        }

        GuildMember guildMember = guildMemberService.getByPlayer(player);
        if (guildMember == null) {
            log.error("Player {} is not a guild member", message.getPlayerId());
            return;
        }

        AtomicInteger notifyCount = new AtomicInteger();
        Guild guild = guildService.findById(guildMember.getGuild().getId());
        guild.getMemberList().stream()
                .filter(m -> !m.getPlayer().getId().equals(player.getId()))
                .forEach(m -> {
                    List<GuildMember> guildMembers = socialService.getGuildMemberList(m.getPlayer());

                    S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                    FTConnection guildMemberConnection = gameManager.getConnectionByPlayerId(m.getPlayer().getId());
                    if (guildMemberConnection != null) {
                        guildMemberConnection.sendTCP(s2CClubMembersListAnswerPacket);
                        notifyCount.getAndIncrement();
                    }
                });

        log.info("Player {} notified {} guild members", message.getPlayerId(), notifyCount.get());
    }
}
