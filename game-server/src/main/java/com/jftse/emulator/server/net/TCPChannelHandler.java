package com.jftse.emulator.server.net;

import com.jftse.emulator.common.utilities.StringUtils;
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
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CServerNoticePacket;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import com.jftse.server.core.thread.ThreadManager;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

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
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Active");

        FTClient client = new FTClient();

        client.setIp(remoteAddress.substring(1, remoteAddress.lastIndexOf(":")));
        client.setPort(Integer.parseInt(remoteAddress.substring(remoteAddress.indexOf(":") + 1)));
        client.setConnection(connection);
        connection.setClient(client);

        GameManager.getInstance().addClient(client);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);

        final String motd = GameManager.getInstance().getMotd();
        if (!StringUtils.isEmpty(motd)) {
            ThreadManager.getInstance().schedule(() -> {
                S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(motd);
                connection.sendTCP(serverNoticePacket);
            }, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void handlerNotFound(FTConnection connection, Packet packet) throws Exception {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.warn("(" + remoteAddress + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    public void packetNotProcessed(FTConnection connection, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void disconnected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Inactive");

        final FTClient client = connection.getClient();
        if (client != null) {
            boolean notifyClients = true;

            Player player = client.getPlayer();
            if (player != null) {
                player.setOnline(false);
                client.savePlayer(player);

                Account account = client.getAccount();
                if (account != null && account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                    if (account.getLoggedInServer() == ServerType.GAME_SERVER) {
                        account.setLoggedInServer(ServerType.NONE);
                    }
                    account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                    account.setLogoutServer(ServerType.GAME_SERVER);
                    client.saveAccount(account);
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
                        PlayerStatistic playerStatistic = player.getPlayerStatistic();
                        playerStatistic.setNumberOfDisconnects(playerStatistic.getNumberOfDisconnects() + 1);
                        playerStatistic = ServiceManager.getInstance().getPlayerStatisticService().save(player.getPlayerStatistic());

                        player.setPlayerStatistic(playerStatistic);
                        client.savePlayer(player);
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
                InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
                String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
                log.warn("(" + remoteAddress + ") exceptionCaught: " + cause.getMessage(), cause);
            }
        }
    }
}
