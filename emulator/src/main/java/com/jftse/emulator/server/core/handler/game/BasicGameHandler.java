package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayBattleGame;
import com.jftse.emulator.server.core.matchplay.game.MatchplayGuardianGame;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.core.packet.packets.authserver.S2CLoginAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.matchplay.S2CMatchplayBackToRoom;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.service.*;
import com.jftse.emulator.server.core.service.messenger.FriendService;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.shared.module.Client;

import java.net.InetSocketAddress;
import java.util.List;

public class BasicGameHandler {
    private final FriendService friendService;
    private final SocialService socialService;
    private final GuildMemberService guildMemberService;
    private final PlayerStatisticService playerStatisticService;

    public BasicGameHandler() {
        friendService = ServiceManager.getInstance().getFriendService();
        socialService = ServiceManager.getInstance().getSocialService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
    }

    public void sendWelcomePacket(Connection connection) {
        if (connection.getRemoteAddressTCP() != null) {
            InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();
            int port = inetSocketAddress.getPort();

            connection.getClient().setIp(hostAddress);
            connection.getClient().setPort(port);

            S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecKey(), connection.getEncKey(), 0, 0);
            connection.sendTCP(welcomePacket);
        }
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() == null)
            return;

        final Client client = connection.getClient();
        Account account = client.getAccount();

        if (account != null) {
            boolean notifyClients = true;
            Player player = client.getPlayer();
            if (player != null) {
                player.setOnline(false);
                client.savePlayer(player);

                List<Friend> friends = friendService.findByPlayer(player);
                friends.forEach(x -> {
                    List<Friend> friendList = socialService.getFriendList(x.getFriend(), EFriendshipState.Friends);
                    S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(friendList);
                    GameManager.getInstance().getClients().stream()
                            .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(x.getFriend().getId()))
                            .findFirst()
                            .ifPresent(c -> {
                                if (c.getConnection() != null && c.getConnection().isConnected()) {
                                    c.getConnection().sendTCP(friendListAnswerPacket);
                                }
                            });
                });

                GuildMember guildMember = guildMemberService.getByPlayer(player);
                if (guildMember != null && guildMember.getGuild() != null) {
                    guildMember.getGuild().getMemberList().stream()
                            .filter(x -> x != guildMember)
                            .forEach(x -> {
                                List<GuildMember> guildMembers = socialService.getGuildMemberList(x.getPlayer());

                                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                                GameManager.getInstance().getClients().stream()
                                        .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(x.getPlayer().getId()))
                                        .findFirst()
                                        .ifPresent(c -> {
                                            if (c.getConnection() != null && c.getConnection().isConnected()) {
                                                c.getConnection().sendTCP(s2CClubMembersListAnswerPacket);
                                            }
                                        });
                            });
                }

                Friend myRelation = socialService.getRelationship(player);
                if (myRelation != null) {
                    Client friendRelationClient = GameManager.getInstance().getClients().stream()
                            .filter(x -> x.getPlayer() != null && x.getPlayer().getId().equals(myRelation.getFriend().getId()))
                            .findFirst()
                            .orElse(null);
                    Friend friendRelation = socialService.getRelationship(myRelation.getFriend());

                    if (friendRelationClient != null && friendRelation != null) {
                        S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                        friendRelationClient.getConnection().sendTCP(s2CRelationshipAnswerPacket);
                    }
                }
            }

            if (account.getStatus().shortValue() != S2CLoginAnswerPacket.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
                client.saveAccount(account);
            }

            GameSession gameSession = client.getActiveGameSession();
            if (gameSession != null) {

                Room currentClientRoom = client.getActiveRoom();
                if (currentClientRoom != null) {
                    if (player != null && currentClientRoom.getStatus() == RoomStatus.Running) {
                        PlayerStatistic playerStatistic = player.getPlayerStatistic();
                        playerStatistic.setNumberOfDisconnects(playerStatistic.getNumberOfDisconnects() + 1);
                        playerStatistic = playerStatisticService.save(player.getPlayerStatistic());

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
                                    if (c.getConnection() != null && c.getConnection().getId() != connection.getId() && c.getConnection().isConnected()) {
                                        S2CMatchplayBackToRoom backToRoomPacket = new S2CMatchplayBackToRoom();
                                        c.getConnection().sendTCP(backToRoomPacket);
                                    }
                                }
                            });
                            GameSessionManager.getInstance().getGameSessionList().removeIf(gs -> gs.getSessionId() == gameSession.getSessionId());
                        }
                    }
                    MatchplayGame game = gameSession.getActiveMatchplayGame();
                    if (game instanceof MatchplayBattleGame)
                        ((MatchplayBattleGame) game).getScheduledFutures().forEach(sf -> sf.cancel(false));
                    else if (game instanceof MatchplayGuardianGame)
                        ((MatchplayGuardianGame) game).getScheduledFutures().forEach(sf -> sf.cancel(false));

                    client.setActiveGameSession(null);
                }
            }
            GameManager.getInstance().handleRoomPlayerChanges(connection, notifyClients);
        }

        GameManager.getInstance().removeClient(client);
        connection.setClient(null);
    }
}
