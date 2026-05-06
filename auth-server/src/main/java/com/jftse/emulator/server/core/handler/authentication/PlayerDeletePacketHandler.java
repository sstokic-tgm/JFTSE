package com.jftse.emulator.server.core.handler.authentication;

import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.log.CommandLog;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.CommandLogService;
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
    private final GuildService guildService;
    private final CommandLogService commandLogService;
    private final JdbcUtil jdbcUtil;

    public PlayerDeletePacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildService = ServiceManager.getInstance().getGuildService();
        commandLogService = ServiceManager.getInstance().getCommandLogService();
        jdbcUtil = AuthenticationManager.getInstance().getJdbcUtil();
    }

    @Override
    public void handle(FTConnection connection, CMSGPlayerDelete playerDeletePacket) {
        FTClient client = connection.getClient();
        if (client != null) {
            Long accountId = client.getAccountId();
            Player player = playerService.findById((long) playerDeletePacket.getPlayerId());
            if (accountId != null && player != null) {
                List<Player> playerList = playerService.getPlayerListByAccountId(accountId);
                int tutorialCount = playerService.getTutorialProgressSucceededCountByAccount(accountId);

                boolean removed = playerList.removeIf(p -> p.getId().equals(player.getId()));
                if (removed) {
                    SMSGPlayerDelete response = SMSGPlayerDelete.builder().result((char) 0).build();
                    connection.sendTCP(response);

                    SMSGPlayerList playerListPacket = SMSGPlayerList.builder()
                            .account(
                                    com.jftse.server.core.shared.packets.auth.Account.builder()
                                            .id(Math.toIntExact(accountId))
                                            .id2(Math.toIntExact(accountId))
                                            .tutorialCount((byte) tutorialCount)
                                            .gameMaster(client.isGameMaster())
                                            .lastPlayedPlayerId(Math.toIntExact(client.getLastPlayedPlayerId() == null ? 0 : client.getLastPlayedPlayerId()))
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
                            Guild guild = guildService.findWithMembersByPlayerId(player.getId());
                            if (guild != null) {
                                GuildMember guildMember = guild.getMemberList().stream().filter(m -> m.getPlayer().getId().equals(player.getId())).findFirst().orElse(null);
                                if (guildMember != null && guildMember.getMemberRank() != 3) {
                                    guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
                                    guildService.save(guild);
                                } else {
                                    SMSGPlayerDelete playerDelete = SMSGPlayerDelete.builder().result((char) -2).build();
                                    connection.sendTCP(playerDelete);
                                    return;
                                }
                            }

                            List<CommandLog> commandLogList = commandLogService.findAllByPlayerId(player.getId());
                            for (CommandLog commandLog : commandLogList) {
                                commandLog.setPlayer(null);
                                commandLog.setCommand(commandLog.getCommand() + " by " + player.getName() + " (deleted)");
                                commandLogService.save(commandLog);
                            }

                            jdbcUtil.execute(em -> {
                                em.createQuery("DELETE FROM PlayerLotteryProgress pl WHERE pl.player.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Friend f WHERE f.player.id = :playerId OR f.friend.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Gift g WHERE g.sender.id = :playerId OR g.receiver.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Message m WHERE m.sender.id = :playerId OR m.receiver.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Parcel p WHERE p.sender.id = :playerId OR p.receiver.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Proposal pr WHERE pr.sender.id = :playerId OR pr.receiver.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM PlayerPocket pp WHERE pp.pocket.id = :pocketId")
                                        .setParameter("pocketId", player.getPocket().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM GuildGoldUsage ggu WHERE ggu.player.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM ChallengeProgress cp WHERE cp.player.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM TutorialProgress tp WHERE tp.player.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM PetStatistic ps WHERE ps.id IN (SELECT p.petStatistic.id FROM Pet p WHERE p.player.id = :playerId)")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Pet p WHERE p.player.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Player p WHERE p.id = :playerId")
                                        .setParameter("playerId", player.getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM Pocket p WHERE p.id = :pocketId")
                                        .setParameter("pocketId", player.getPocket().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM ClothEquipment ce WHERE ce.id = :clothEquipmentId")
                                        .setParameter("clothEquipmentId", player.getClothEquipment().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM QuickSlotEquipment qse WHERE qse.id = :quickSlotEquipmentId")
                                        .setParameter("quickSlotEquipmentId", player.getQuickSlotEquipment().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM ToolSlotEquipment tse WHERE tse.id = :toolSlotEquipmentId")
                                        .setParameter("toolSlotEquipmentId", player.getToolSlotEquipment().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM SpecialSlotEquipment sse WHERE sse.id = :specialSlotEquipmentId")
                                        .setParameter("specialSlotEquipmentId", player.getSpecialSlotEquipment().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM CardSlotEquipment cse WHERE cse.id = :cardSlotEquipmentId")
                                        .setParameter("cardSlotEquipmentId", player.getCardSlotEquipment().getId())
                                        .executeUpdate();
                                em.createQuery("DELETE FROM PlayerStatistic ps WHERE ps.id = :playerStatisticId")
                                        .setParameter("playerStatisticId", player.getPlayerStatistic().getId())
                                        .executeUpdate();
                            });
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
