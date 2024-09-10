package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CFriendsListAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CRelationshipAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.EFriendshipState;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

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
                    if (account.getLoggedInServer() == ServerType.CHAT_SERVER) {
                        account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                        account.setLoggedInServer(ServerType.NONE);
                    }
                    account.setLogoutServer(ServerType.CHAT_SERVER);
                    client.saveAccount(account);
                }

                List<Friend> friends = ServiceManager.getInstance().getFriendService().findByPlayer(player);
                friends.forEach(x -> {
                    List<Friend> friendList = ServiceManager.getInstance().getSocialService().getFriendList(x.getFriend(), EFriendshipState.Friends);
                    S2CFriendsListAnswerPacket friendListAnswerPacket = new S2CFriendsListAnswerPacket(friendList);
                    FTConnection friendConnection = GameManager.getInstance().getConnectionByPlayerId(x.getFriend().getId());
                    if (friendConnection != null) {
                        friendConnection.sendTCP(friendListAnswerPacket);
                    } else {
                        RProducerService.getInstance().send("playerId", x.getFriend().getId(), friendListAnswerPacket);
                    }
                });

                GuildMember guildMember = ServiceManager.getInstance().getGuildMemberService().getByPlayer(player);
                if (guildMember != null && guildMember.getGuild() != null) {
                    guildMember.getGuild().getMemberList().stream()
                            .filter(x -> x != guildMember)
                            .forEach(x -> {
                                List<GuildMember> guildMembers = ServiceManager.getInstance().getSocialService().getGuildMemberList(x.getPlayer());

                                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                                FTConnection guildMemberConnection = GameManager.getInstance().getConnectionByPlayerId(x.getPlayer().getId());
                                if (guildMemberConnection != null) {
                                    guildMemberConnection.sendTCP(s2CClubMembersListAnswerPacket);
                                } else {
                                    RProducerService.getInstance().send("playerId", x.getPlayer().getId(), s2CClubMembersListAnswerPacket);
                                }
                            });
                }

                Friend myRelation = ServiceManager.getInstance().getSocialService().getRelationship(player);
                if (myRelation != null) {
                    FTConnection friendRelationClient = GameManager.getInstance().getConnectionByPlayerId(myRelation.getFriend().getId());
                    Friend friendRelation = ServiceManager.getInstance().getSocialService().getRelationship(myRelation.getFriend());

                    if (friendRelationClient != null && friendRelation != null) {
                        S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                        friendRelationClient.sendTCP(s2CRelationshipAnswerPacket);
                    } else if (friendRelation != null) {
                        S2CRelationshipAnswerPacket s2CRelationshipAnswerPacket = new S2CRelationshipAnswerPacket(friendRelation);
                        RProducerService.getInstance().send("playerId", friendRelation.getPlayer().getId(), s2CRelationshipAnswerPacket);
                    }
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
