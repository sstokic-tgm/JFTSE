package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayBackToRoom;
import com.jftse.emulator.server.core.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandler<FTConnection> {
    private final BlockedIPService blockedIPService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey, final PacketHandlerFactory phf) {
        super(ftConnectionAttributeKey, phf);

        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();
    }

    @Override
    public void connected(FTConnection connection) {
        final String remoteAddress = connection.getRemoteAddressTCP().toString();
        log.info("(" + remoteAddress + ") Channel Active");

        if (!checkIp(connection, remoteAddress, () -> blockedIPService, () -> log))
            return;

        FTClient client = new FTClient();

        client.setIp(remoteAddress.substring(1, remoteAddress.lastIndexOf(":")));
        client.setPort(Integer.parseInt(remoteAddress.substring(remoteAddress.indexOf(":") + 1)));
        client.setConnection(connection);
        connection.setClient(client);

        GameManager.getInstance().addClient(client);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    @Override
    public void handlerNotFound(FTConnection connection, Packet packet) throws Exception {
        log.warn("(" + connection.getRemoteAddressTCP().toString() + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    public void packetNotProcessed(FTConnection connection, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void disconnected(FTConnection connection) {
        log.info("(" + connection.getRemoteAddressTCP().toString() + ") Channel Inactive");

        final FTClient client = connection.getClient();
        if (client != null) {
            boolean notifyClients = true;

            Player player = client.getPlayer();
            if (player != null) {
                player.setOnline(false);
                client.savePlayer(player);

                Account account = client.getAccount();
                if (account != null && account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                    account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                    client.saveAccount(account);
                }

                List<Friend> friends = ServiceManager.getInstance().getFriendService().findByPlayer(player);
                friends.forEach(x -> {
                    List<Friend> friendList = ServiceManager.getInstance().getSocialService().getFriendList(x.getFriend(), EFriendshipState.Friends);
                    S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(friendList);
                    GameManager.getInstance().getClients().stream()
                            .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(x.getFriend().getId()))
                            .findFirst()
                            .ifPresent(c -> {
                                if (c.getConnection() != null) {
                                    c.getConnection().sendTCP(friendListAnswerPacket);
                                }
                            });
                });

                GuildMember guildMember = ServiceManager.getInstance().getGuildMemberService().getByPlayer(player);
                if (guildMember != null && guildMember.getGuild() != null) {
                    guildMember.getGuild().getMemberList().stream()
                            .filter(x -> x != guildMember)
                            .forEach(x -> {
                                List<GuildMember> guildMembers = ServiceManager.getInstance().getSocialService().getGuildMemberList(x.getPlayer());

                                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                                GameManager.getInstance().getClients().stream()
                                        .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(x.getPlayer().getId()))
                                        .findFirst()
                                        .ifPresent(c -> {
                                            if (c.getConnection() != null) {
                                                c.getConnection().sendTCP(s2CClubMembersListAnswerPacket);
                                            }
                                        });
                            });
                }

                Friend myRelation = ServiceManager.getInstance().getSocialService().getRelationship(player);
                if (myRelation != null) {
                    FTClient friendRelationClient = GameManager.getInstance().getClients().stream()
                            .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(myRelation.getFriend().getId()))
                            .findFirst()
                            .orElse(null);
                    Friend friendRelation = ServiceManager.getInstance().getSocialService().getRelationship(myRelation.getFriend());

                    if (friendRelationClient != null && friendRelation != null) {
                        S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                        friendRelationClient.getConnection().sendTCP(s2CRelationshipAnswerPacket);
                    }
                }
            }

            GameSession gameSession = client.getActiveGameSession();
            if (gameSession != null) {

                Room currentClientRoom = client.getActiveRoom();
                if (currentClientRoom != null) {
                    if (player != null && currentClientRoom.getStatus() == RoomStatus.Running) {
                        PlayerStatistic playerStatistic = player.getPlayerStatistic();
                        playerStatistic.setNumberOfDisconnects(playerStatistic.getNumberOfDisconnects() + 1);
                        playerStatistic = ServiceManager.getInstance().getPlayerStatisticService().save(player.getPlayerStatistic());

                        player.setPlayerStatistic(playerStatistic);
                        client.savePlayer(player);
                    }

                    RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
                    if (roomPlayer != null) {
                        notifyClients = roomPlayer.getPosition() < 4;
                        if (notifyClients) {
                            synchronized (currentClientRoom) {
                                currentClientRoom.setStatus(RoomStatus.NotRunning);
                            }
                            client.setActiveRoom(currentClientRoom);

                            gameSession.getClients().forEach(c -> {
                                Room room = c.getActiveRoom();
                                if (room != null) {
                                    if (c.getConnection() != null && c.getConnection().getId() != connection.getId()) {
                                        S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                                        c.getConnection().sendTCP(backToRoomPacket);
                                    }
                                }
                            });
                            GameSessionManager.getInstance().getGameSessionList().remove(client.getGameSessionId(), gameSession);
                        }
                    }
                    MatchplayGame game = gameSession.getMatchplayGame();
                    if (game instanceof MatchplayBattleGame)
                        ((MatchplayBattleGame) game).getScheduledFutures().forEach(sf -> sf.cancel(false));
                    else if (game instanceof MatchplayGuardianGame)
                        ((MatchplayGuardianGame) game).getScheduledFutures().forEach(sf -> sf.cancel(false));

                    client.setActiveGameSession(null);
                }
            }
            GameManager.getInstance().handleRoomPlayerChanges(connection, notifyClients);
            GameManager.getInstance().removeClient(client);
        }
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        if (!cause.equals(ReadTimeoutException.INSTANCE)) {
            var shouldHandleException = switch (cause.getMessage()) {
                case "Connection reset", "Connection timed out", "No route to host" -> false;
                default -> true;
            };

            if (shouldHandleException) {
                log.warn("(" + connection.getRemoteAddressTCP().toString() + ") exceptionCaught: " + cause.getMessage(), cause);
            }
        }
    }
}
