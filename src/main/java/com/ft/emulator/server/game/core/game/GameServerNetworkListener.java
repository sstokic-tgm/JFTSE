package com.ft.emulator.server.game.core.game;

import com.ft.emulator.server.game.core.game.handler.GamePacketHandler;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.ConnectionListener;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import org.springframework.beans.factory.annotation.Autowired;

public class GameServerNetworkListener implements ConnectionListener {

    @Autowired
    private GamePacketHandler gamePacketHandler;

    public void connected(Connection connection) {

        Client client = new Client();
        client.setConnection(connection);

        connection.setClient(client);
        gamePacketHandler.getGameHandler().addClient(client);

        gamePacketHandler.sendWelcomePacket(connection);
    }

    public void disconnected(Connection connection) {

        gamePacketHandler.handleDisconnected(connection);
    }

    public void received(Connection connection, Packet packet) {

        switch (packet.getPacketId()) {

        case PacketID.C2SDisconnectRequest:

            gamePacketHandler.handleDisconnectPacket(connection, packet);
            break;

        case PacketID.C2SGameLoginData:

            gamePacketHandler.handleGameServerLoginPacket(connection, packet);
            break;

        case PacketID.C2SGameReceiveData:

            gamePacketHandler.handleGameServerDataRequestPacket(connection, packet);
            break;

        case PacketID.C2SHomeItemsLoadReq:

            gamePacketHandler.handleHomeItemsLoadRequestPacket(connection, packet);
            break;

        case PacketID.C2SHomeItemsPlaceReq:

            gamePacketHandler.handleHomeItemsPlaceRequestPacket(connection, packet);
            break;

        case PacketID.C2SHomeItemsClearReq:

            gamePacketHandler.handleHomeItemClearRequestPacket(connection, packet);
            break;

        case PacketID.C2SInventorySellReq:
        case PacketID.C2SInventorySellItemCheckReq:

            gamePacketHandler.handleInventoryItemSellPackets(connection, packet);
            break;

        case PacketID.C2SInventoryWearClothRequest:

            gamePacketHandler.handleInventoryWearClothPacket(connection, packet);
            break;

        case PacketID.C2SInventoryWearQuickRequest:

            gamePacketHandler.handleInventoryWearQuickPacket(connection, packet);
            break;

        case PacketID.C2SInventoryItemTimeExpiredRequest:

            gamePacketHandler.handleInventoryItemTimeExpiredPacket(connection, packet);
            break;

        case PacketID.C2SShopMoneyReq:

            gamePacketHandler.handleShopMoneyRequestPacket(connection, packet);
            break;

        case PacketID.C2SShopBuyReq:

            gamePacketHandler.handleShopBuyRequestPacket(connection, packet);
            break;

        case PacketID.C2SShopRequestDataPrepare:
        case PacketID.C2SShopRequestData:

            gamePacketHandler.handleShopRequestDataPackets(connection, packet);
            break;

        case PacketID.C2SPlayerStatusPointChange:

            gamePacketHandler.handlePlayerStatusPointChangePacket(connection, packet);
            break;

        case PacketID.C2SChallengeProgressReq:

            gamePacketHandler.handleChallengeProgressRequestPacket(connection, packet);
            break;

        case PacketID.C2STutorialProgressReq:

            gamePacketHandler.handleTutorialProgressRequestPacket(connection, packet);
            break;

        case PacketID.C2SChallengeBeginReq:

            gamePacketHandler.handleChallengeBeginRequestPacket(connection, packet);
            break;

        case PacketID.C2SChallengeHp:

            gamePacketHandler.handleChallengeHpPacket(connection, packet);
            break;

        case PacketID.C2SChallengePoint:

            gamePacketHandler.handleChallengePointPacket(connection, packet);
            break;

        case PacketID.C2SChallengeDamage:

            gamePacketHandler.handleChallengeDamagePacket(connection, packet);
            break;

        case PacketID.C2SQuickSlotUseRequest:

            gamePacketHandler.handleQuickSlotUseRequest(connection, packet);
            break;

        case PacketID.C2SChallengeSet:

            gamePacketHandler.handleChallengeSetPacket(connection, packet);
            break;

        case PacketID.C2STutorialBegin:

            gamePacketHandler.handleTutorialBeginPacket(connection, packet);
            break;

        case PacketID.C2STutorialEnd:

            gamePacketHandler.handleTutorialEndPacket(connection, packet);
            break;

        case PacketID.C2SLobbyUserListRequest:

            gamePacketHandler.handleLobbyUserListReqPacket(connection, packet);
            break;

        case PacketID.C2SChatLobbyReq:
        case PacketID.C2SChatRoomReq:
        case PacketID.C2SWhisperReq:

            gamePacketHandler.handleChatMessagePackets(connection, packet);
            break;

        case PacketID.C2SLobbyJoin:

            gamePacketHandler.handleLobbyJoinLeave(connection, true);
            break;

        case PacketID.C2SLobbyLeave:

            gamePacketHandler.handleLobbyJoinLeave(connection, false);
            break;

        case PacketID.C2SEmblemListRequest:

            gamePacketHandler.handleEmblemListRequestPacket(connection, packet);
            break;

        case PacketID.C2SHeartbeat:
        case PacketID.C2SLoginAliveClient:
            break;

        case 0x1071:

            gamePacketHandler.handle1071Packet(connection, packet);
            break;

        default:

            gamePacketHandler.handleUnknown(connection, packet);
            break;
        }
    }

    public void idle(Connection connection) {
    }
}