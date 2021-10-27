package com.jftse.emulator.server.game.core.handler.authentication;

import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.game.core.handler.AbstractHandler;
import com.jftse.emulator.server.game.core.manager.ServiceManager;
import com.jftse.emulator.server.game.core.packet.packets.player.C2SPlayerDeletePacket;
import com.jftse.emulator.server.game.core.packet.packets.player.S2CPlayerDeleteAnswerPacket;
import com.jftse.emulator.server.game.core.packet.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.game.core.service.GuildMemberService;
import com.jftse.emulator.server.game.core.service.GuildService;
import com.jftse.emulator.server.game.core.service.PlayerService;
import com.jftse.emulator.server.game.core.service.messaging.*;
import com.jftse.emulator.server.networking.packet.Packet;

public class PlayerDeletePacketHandler extends AbstractHandler {
    private C2SPlayerDeletePacket playerDeletePacket;

    private final PlayerService playerService;
    private final FriendService friendService;
    private final GiftService giftService;
    private final MessageService messageService;
    private final ParcelService parcelService;
    private final ProposalService proposalService;
    private final GuildMemberService guildMemberService;
    private final GuildService guildService;

    public PlayerDeletePacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        giftService = ServiceManager.getInstance().getGiftService();
        messageService = ServiceManager.getInstance().getMessageService();
        parcelService = ServiceManager.getInstance().getParcelService();
        proposalService = ServiceManager.getInstance().getProposalService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public boolean process(Packet packet) {
        playerDeletePacket = new C2SPlayerDeletePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findById((long) playerDeletePacket.getPlayerId());
        if (player != null) {
            GuildMember guildMember = guildMemberService.getByPlayer(player);
            if (guildMember != null) {
                if (guildMember.getMemberRank() != 3) {
                    Guild guild = guildMember.getGuild();
                    guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
                    guildService.save(guild);
                } else {
                    S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) -1);
                    connection.sendTCP(playerDeleteAnswerPacket);
                    return;
                }
            }

            friendService.deleteByPlayer(player);
            giftService.deleteBySender(player);
            giftService.deleteByReceiver(player);
            messageService.deleteBySender(player);
            messageService.deleteByReceiver(player);
            parcelService.deleteBySender(player);
            parcelService.deleteByReceiver(player);
            proposalService.deleteBySender(player);
            proposalService.deleteByReceiver(player);

            connection.getClient().getAccount().getPlayerList().removeIf(pl -> pl.getId().equals(player.getId()));
            playerService.remove(player.getId());

            S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) 0);
            connection.sendTCP(playerDeleteAnswerPacket);

            S2CPlayerListPacket playerListPacket = new S2CPlayerListPacket(connection.getClient().getAccount(), connection.getClient().getAccount().getPlayerList());
            connection.sendTCP(playerListPacket);
        } else {
            S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) -1);
            connection.sendTCP(playerDeleteAnswerPacket);
        }
    }
}
