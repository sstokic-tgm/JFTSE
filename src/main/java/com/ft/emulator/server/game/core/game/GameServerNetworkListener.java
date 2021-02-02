package com.ft.emulator.server.game.core.game;

import com.ft.emulator.server.game.core.game.handler.GamePacketHandler;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.ConnectionListener;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
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

            case PacketID.C2SLobbyUserInfoRequest:
                gamePacketHandler.handleLobbyUserInfoReqPacket(connection, packet);
                break;

            case PacketID.C2SLobbyUserInfoClothRequest:
                gamePacketHandler.handleLobbyUserInfoClothReqPacket(connection, packet);
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

            case PacketID.C2SOpenGachaReq:
                gamePacketHandler.handleOpenGachaRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomCreate:
                gamePacketHandler.handleRoomCreateRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomNameChange:
                gamePacketHandler.handleRoomNameChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomGameModeChange:
                gamePacketHandler.handleGameModeChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomIsPrivateChange:
                gamePacketHandler.handleRoomIsPrivateChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomLevelRangeChange:
                gamePacketHandler.handleRoomLevelRangeChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomSkillFreeChange:
                gamePacketHandler.handleRoomSkillFreeChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomAllowBattlemonChange:
                gamePacketHandler.handleRoomAllowBattlemonChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomQuickSlotChange:
                gamePacketHandler.handleRoomQuickSlotChangePacket(connection, packet);
                break;

            case PacketID.C2SRoomJoin:
                gamePacketHandler.handleRoomJoinRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomLeave:
                gamePacketHandler.handleRoomLeaveRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomReadyChange:
                gamePacketHandler.handleRoomReadyChangeRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomMapChange:
                gamePacketHandler.handleRoomMapChangeRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomPositionChange:
                gamePacketHandler.handleRoomPositionChangeRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomKickPlayer:
                gamePacketHandler.handleRoomKickPlayerRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomSlotCloseReq:
                gamePacketHandler.handleRoomSlotCloseRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomFittingReq:
                gamePacketHandler.handleRoomFittingRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomTriggerStartGame:
                gamePacketHandler.handleRoomStartGamePacket(connection, packet);
                break;

            case PacketID.C2SRoomCreateQuick:
                gamePacketHandler.handleRoomCreateQuickRequestPacket(connection, packet);
                break;

            case PacketID.C2SRoomListReq:
                gamePacketHandler.handleRoomListRequestPacket(connection, packet);
                break;

            case PacketID.C2SGameAnimationSkipReady:
                gamePacketHandler.handleGameAnimationReadyToSkipPacket(connection, packet);
                break;

            case PacketID.C2SGameAnimationSkipTriggered:
                gamePacketHandler.handleGameAnimationSkipTriggeredPacket(connection, packet);
                break;

            case PacketID.D2SDevPacket:
                gamePacketHandler.handleDevPacket(connection, packet);
                break;

            case PacketID.C2SMatchplayPoint:
                gamePacketHandler.handleMatchplayPointPacket(connection, packet);
                break;

            case PacketID.C2SMatchplayClientBackInRoom:
                gamePacketHandler.handleClientBackInRoomPacket(connection, packet);
                break;

            case PacketID.C2SHeartbeat:
            case PacketID.C2SLoginAliveClient:
                // empty..
                break;

            default:
                gamePacketHandler.handleUnknown(connection, packet);
                break;
        }
    }

    public void idle(Connection connection) {
        // empty..
    }

    public void onException(Connection connection, Exception exception) {
        log.error(exception.getMessage(), exception);
    }
}
