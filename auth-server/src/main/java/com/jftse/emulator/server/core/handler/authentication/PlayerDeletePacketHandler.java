package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.player.C2SPlayerDeletePacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerDeleteAnswerPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerListPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.log.CommandLog;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketOperationIdentifier(PacketOperations.C2SPlayerDelete)
public class PlayerDeletePacketHandler extends AbstractPacketHandler {
    private C2SPlayerDeletePacket playerDeletePacket;

    private final PlayerService playerService;
    private final FriendService friendService;
    private final GiftService giftService;
    private final MessageService messageService;
    private final ParcelService parcelService;
    private final ProposalService proposalService;
    private final GuildMemberService guildMemberService;
    private final GuildService guildService;
    private final CommandLogService commandLogService;

    public PlayerDeletePacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        friendService = ServiceManager.getInstance().getFriendService();
        giftService = ServiceManager.getInstance().getGiftService();
        messageService = ServiceManager.getInstance().getMessageService();
        parcelService = ServiceManager.getInstance().getParcelService();
        proposalService = ServiceManager.getInstance().getProposalService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        guildService = ServiceManager.getInstance().getGuildService();
        commandLogService = ServiceManager.getInstance().getCommandLogService();
    }

    @Override
    public boolean process(Packet packet) {
        playerDeletePacket = new C2SPlayerDeletePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client != null) {
            Account account = client.getAccount();
            Player player = playerService.findById((long) playerDeletePacket.getPlayerId());
            if (account != null && player != null) {
                try {
                    GuildMember guildMember = guildMemberService.getByPlayer(player);
                    if (guildMember != null) {
                        if (guildMember.getMemberRank() != 3) {
                            Guild guild = guildMember.getGuild();
                            guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
                            guildService.save(guild);
                        } else {
                            S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) -2);
                            connection.sendTCP(playerDeleteAnswerPacket);
                            return;
                        }
                    }

                    friendService.deleteAllByPlayer(player);
                    friendService.deleteAllByFriend(player);
                    giftService.deleteBySender(player);
                    giftService.deleteByReceiver(player);
                    messageService.deleteBySender(player);
                    messageService.deleteByReceiver(player);
                    parcelService.deleteBySender(player);
                    parcelService.deleteByReceiver(player);
                    proposalService.deleteBySender(player);
                    proposalService.deleteByReceiver(player);

                    List<CommandLog> commandLogList = commandLogService.findAllByPlayerId(player.getId());
                    for (CommandLog commandLog : commandLogList) {
                        commandLog.setPlayer(null);
                        commandLog.setCommand(commandLog.getCommand() + " by " + player.getName() + " (deleted)");
                        commandLogService.save(commandLog);
                    }

                    playerService.remove(player.getId());

                    S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) 0);
                    connection.sendTCP(playerDeleteAnswerPacket);

                    List<Player> playerList = playerService.findAllByAccount(account);
                    int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(account.getId());

                    S2CPlayerListPacket playerListPacket = new S2CPlayerListPacket(account, playerList, tutorialCount);
                    connection.sendTCP(playerListPacket);
                } catch (Exception e) {
                    log.error("Error while deleting player: " + e.getMessage(), e);
                    S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) -1);
                    connection.sendTCP(playerDeleteAnswerPacket);
                }
            } else {
                S2CPlayerDeleteAnswerPacket playerDeleteAnswerPacket = new S2CPlayerDeleteAnswerPacket((char) -1);
                connection.sendTCP(playerDeleteAnswerPacket);
            }
        }
    }
}
