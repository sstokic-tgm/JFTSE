package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.log.CommandLog;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.packets.auth.CMSGPlayerDelete;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerDelete;
import com.jftse.server.core.shared.packets.auth.SMSGPlayerList;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@PacketId(CMSGPlayerDelete.PACKET_ID)
public class PlayerDeletePacketHandler implements PacketHandler<FTConnection, CMSGPlayerDelete> {
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
    public void handle(FTConnection connection, CMSGPlayerDelete playerDeletePacket) {
        FTClient client = connection.getClient();
        if (client != null) {
            Account account = client.getAccount();
            Player player = playerService.findById((long) playerDeletePacket.getPlayerId());
            if (account != null && player != null) {
                List<Player> playerList = playerService.findAllByAccount(account);
                int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(account.getId());

                boolean removed = playerList.removeIf(p -> p.getId().equals(player.getId()));
                if (removed) {
                    SMSGPlayerDelete response = SMSGPlayerDelete.builder().result((char) 0).build();
                    connection.sendTCP(response);

                    SMSGPlayerList playerListPacket = SMSGPlayerList.builder()
                            .account(
                                    com.jftse.server.core.shared.packets.auth.Account.builder()
                                            .id(Math.toIntExact(account.getId()))
                                            .id2(Math.toIntExact(account.getId()))
                                            .tutorialCount((byte) tutorialCount)
                                            .gameMaster(account.getGameMaster())
                                            .lastPlayedPlayerId(Math.toIntExact(account.getLastSelectedPlayerId() == null ? 0 : account.getLastSelectedPlayerId()))
                                            .build()
                            )
                            .players(playerList.stream().map(p -> com.jftse.server.core.shared.packets.auth.Player.builder()
                                    .id(Math.toIntExact(p.getId()))
                                    .name(p.getName())
                                    .level(p.getLevel())
                                    .created(p.getAlreadyCreated())
                                    .canDelete(!p.getFirstPlayer())
                                    .gold(p.getGold())
                                    .playerType(p.getPlayerType())
                                    .str(p.getStrength())
                                    .sta(p.getStamina())
                                    .dex(p.getDexterity())
                                    .wil(p.getWillpower())
                                    .statPoints(p.getStatusPoints())
                                    .oldRenameAllowed(false)
                                    .renameAllowed(p.getNameChangeAllowed())
                                    .clothEquipment(com.jftse.server.core.shared.packets.auth.ClothEquipment.builder()
                                            .hair(p.getClothEquipment().getHair())
                                            .face(p.getClothEquipment().getFace())
                                            .dress(p.getClothEquipment().getDress())
                                            .pants(p.getClothEquipment().getPants())
                                            .socks(p.getClothEquipment().getSocks())
                                            .shoes(p.getClothEquipment().getShoes())
                                            .gloves(p.getClothEquipment().getGloves())
                                            .racket(p.getClothEquipment().getRacket())
                                            .glasses(p.getClothEquipment().getGlasses())
                                            .bag(p.getClothEquipment().getBag())
                                            .hat(p.getClothEquipment().getHat())
                                            .dye(p.getClothEquipment().getDye())
                                            .build()
                                    )
                                    .build()).toList()
                            ).build();
                    connection.sendTCP(playerListPacket);

                    ThreadManager.getInstance().newTask(() -> {
                        try {
                            GuildMember guildMember = guildMemberService.getByPlayer(player);
                            if (guildMember != null) {
                                if (guildMember.getMemberRank() != 3) {
                                    Guild guild = guildMember.getGuild();
                                    guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
                                    guildService.save(guild);
                                } else {
                                    SMSGPlayerDelete playerDelete = SMSGPlayerDelete.builder().result((char) -2).build();
                                    connection.sendTCP(playerDelete);
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
                        } catch (Exception e) {
                            log.error("Error while deleting player: " + e.getMessage(), e);
                            SMSGPlayerDelete playerDelete = SMSGPlayerDelete.builder().result((char) -1).build();
                            connection.sendTCP(playerDelete);
                        }
                    });
                }
            } else {
                SMSGPlayerDelete playerDelete = SMSGPlayerDelete.builder().result((char) -1).build();
                connection.sendTCP(playerDelete);
            }
        }
    }
}
