package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.GameSession;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.MatchplayGame;
import com.jftse.emulator.server.core.packets.matchplay.S2CMatchplayBackToRoom;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendListMessage;
import com.jftse.emulator.server.core.rabbit.messages.RefreshFriendRelationMessage;
import com.jftse.emulator.server.core.rabbit.messages.NotifyGuildMemberListOnDisconnectMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.proto.util.AccountAction;
import com.jftse.server.core.net.TCPHandlerV2;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.CMSGDisconnectRequest;
import com.jftse.server.core.shared.packets.CMSGHeartbeat;
import com.jftse.server.core.shared.packets.SMSGDisconnectResponse;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandlerV2<FTConnection> {
    private final BlockedIPService blockedIPService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey) {
        super(ftConnectionAttributeKey);

        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();

        PacketRegistry.register(CMSGDisconnectRequest.PACKET_ID, this::handleDisconnectRequest);
        //PacketRegistry.register(CMSGHeartbeat.PACKET_ID, this::handleHeartBeat);
    }

    @Override
    public void connected(FTConnection connection) {
        GameManager.getInstance().queueConnection(connection);
    }

    @Override
    protected void packetReceived(FTConnection connection, IPacket packet) {
        if (packet instanceof CMSGHeartbeat p) {
            handleHeartBeat(connection, p);
        } else {
            connection.queuePacket(packet);
        }
    }

    private void handleHeartBeat(FTConnection connection, CMSGHeartbeat packet) {
        FTClient client = connection.getClient();
        Account account = client.getAccount();
        if (account != null && account.getStatus() == AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
            connection.wantsToCloseConnection();
        }
    }

    private void handleDisconnectRequest(FTConnection connection, CMSGDisconnectRequest packet) {
        GameManager.getInstance().handleRoomPlayerChanges(connection, true);

        SMSGDisconnectResponse response = SMSGDisconnectResponse.builder().status((byte) 0).build();
        connection.sendTCP(response);
        connection.wantsToCloseConnection();
    }

    @Override
    public void disconnected(FTConnection connection) {
        final FTClient client = connection.getClient();
        boolean notifyClients = true;

        final FTPlayer player = client.getPlayer();
        if (player != null) {
            Player p = player.getPlayer();
            p.setOnline(false);
            ServiceManager.getInstance().getPlayerService().save(p);

            Account account = client.getAccount();
            if (account != null && account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                UpdateAccountRequest request = UpdateAccountRequest.newBuilder()
                        .setAccountId(account.getId())
                        .setTimestamp(System.currentTimeMillis())
                        .setServer(ServerType.GAME_SERVER.getValue())
                        .setAccountAction(AccountAction.newBuilder().setAction(com.jftse.server.core.util.AccountAction.DISCONNECT.getValue()).build())
                        .build();
                ServiceManager.getInstance().getGrpcAuthService().updateAccount(request);
            }

            RefreshFriendListMessage refreshFriendListMessage = RefreshFriendListMessage.builder().playerId(player.getId()).build();
            NotifyGuildMemberListOnDisconnectMessage notifyGuildMemberListOnDisconnectMessage = NotifyGuildMemberListOnDisconnectMessage.builder().playerId(player.getId()).build();
            RefreshFriendRelationMessage refreshFriendRelationMessage = RefreshFriendRelationMessage.builder().playerId(player.getId()).build();

            RProducerService.getInstance().send(refreshFriendListMessage, "game.messenger.friendList chat.messenger.friendList", "GameServer");
            RProducerService.getInstance().send(notifyGuildMemberListOnDisconnectMessage, "game.messenger.guildList chat.messenger.guildList", "GameServer");
            RProducerService.getInstance().send(refreshFriendRelationMessage, "game.messenger.friendRelation chat.messenger.friendRelation", "GameServer");
        }

        GameSession gameSession = client.getActiveGameSession();
        if (gameSession != null) {

            Room currentClientRoom = client.getActiveRoom();
            if (currentClientRoom != null) {
                if (player != null && currentClientRoom.getStatus() == RoomStatus.Running) {
                    PlayerStatistic playerStatistic = ServiceManager.getInstance().getPlayerStatisticService().findPlayerStatisticById(player.getPlayerStatisticId());
                    playerStatistic.setNumberOfDisconnects(playerStatistic.getNumberOfDisconnects() + 1);
                    ServiceManager.getInstance().getPlayerStatisticService().save(playerStatistic);
                }

                RoomPlayer roomPlayer = client.getRoomPlayer();
                if (roomPlayer != null) {
                    roomPlayer.getConnectedToRelay().compareAndSet(true, false);
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
                game.getScheduledFutures().forEach(sf -> sf.cancel(false));
                game.getScheduledFutures().clear();

                GameManager.getInstance().getMatchRallyStatsConsumer().clearSession(client.getGameSessionId());

                client.setActiveGameSession(null);
            }
        }
        GameManager.getInstance().handleRoomPlayerChanges(connection, notifyClients);
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        FTClient client = connection.getClient();
        Account account = client.getAccount();
        if (account != null) {
            log.error("({}) exceptionCaught: {}", account.getId(), cause.getMessage(), cause);
        } else {
            log.error("exceptionCaught: {}", cause.getMessage(), cause);
        }
    }
}
