package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.packets.chat.house.C2SChatHouseMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.C2SChatHousePositionPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CChatHouseMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CChatHousePositionPacket;
import com.jftse.emulator.server.core.packets.chat.square.C2SChatSquareMovePacket;
import com.jftse.emulator.server.core.packets.chat.square.S2CChatSquareMovePacket;
import com.jftse.emulator.server.core.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.*;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.entities.database.model.player.*;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SRoomJoin)
public class RoomJoinRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomJoinRequestPacket roomJoinRequestPacket;

    private final GuildMemberService guildMemberService;
    private final ClothEquipmentServiceImpl clothEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final SocialService socialService;
    private final HomeService homeService;

    public RoomJoinRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        socialService = ServiceManager.getInstance().getSocialService();
        homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public boolean process(Packet packet) {
        roomJoinRequestPacket = new C2SRoomJoinRequestPacket(packet, new ArrayList<>(GameManager.getInstance().getRooms()));
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);
            return;
        }

        if (!ftClient.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        Room room = GameManager.getInstance().getRooms().stream()
                .filter(r -> r.getRoomId() == roomJoinRequestPacket.getRoomId())
                .findAny()
                .orElse(null);

        if (room == null) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(new ArrayList<>(GameManager.getInstance().getRooms()));

            resetIsJoiningOrLeavingRoom(ftClient);

            connection.sendTCP(roomJoinAnswerPacket, roomListAnswerPacket);
            return;
        }

        if (room.getStatus() != RoomStatus.NotRunning) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -1, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        Player activePlayer = ftClient.getPlayer();
        if (!ftClient.isGameMaster() && room.isPrivate() && (StringUtils.isEmpty(roomJoinRequestPacket.getPassword()) || !roomJoinRequestPacket.getPassword().equals(room.getPassword()))) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -5, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        boolean anyPositionAvailable = room.getRoomPlayerList().size() < room.getPlayers();
        if (!anyPositionAvailable) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        // prevent abusive room joins
        if (ftClient.getActiveRoom() != null) {
            Room clientRoom = ftClient.getActiveRoom();

            handleRoomUponJoin(clientRoom);

            resetIsJoiningOrLeavingRoom(ftClient);

            return;
        }

        if (room.getBannedPlayers().contains(activePlayer.getId())) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -4, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        if (activePlayer.getLevel() < (room.getLevel() - room.getLevelRange()) && activePlayer.getLevel() > room.getLevel()) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        List<Short> positions = room.getRoomPlayerList().stream().map(RoomPlayer::getPosition).collect(Collectors.toList());
        final int currentNextPosition = room.getNextPlayerPosition().get();
        int nextPosition = room.getNextPlayerPosition().getAndUpdate(currentPosition -> {
            if (currentPosition >= 0 && currentPosition < room.getPlayers() && !positions.contains((short) currentPosition)) {
                return currentPosition;
            } else {
                return -1;
            }
        });

        if (nextPosition == -1) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        positions.add((short) nextPosition);
        final int newPosition = nextPosition;

        while (positions.contains((short) nextPosition)) {
            nextPosition = (nextPosition + 1) % room.getPlayers();
        }

        if (!room.getNextPlayerPosition().compareAndSet(currentNextPosition, nextPosition)) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayerId(activePlayer.getId());

        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        Friend couple = socialService.getRelationship(activePlayer);
        ClothEquipment clothEquipment = clothEquipmentService.findClothEquipmentById(roomPlayer.getPlayer().getClothEquipment().getId());
        SpecialSlotEquipment specialSlotEquipment = specialSlotEquipmentService.findById(roomPlayer.getPlayer().getSpecialSlotEquipment().getId());
        CardSlotEquipment cardSlotEquipment = cardSlotEquipmentService.findById(roomPlayer.getPlayer().getCardSlotEquipment().getId());
        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(roomPlayer.getPlayer());

        roomPlayer.setGuildMemberId(guildMember == null ? null : guildMember.getId());
        roomPlayer.setCoupleId(couple == null ? null : couple.getId());
        roomPlayer.setClothEquipmentId(clothEquipment.getId());
        roomPlayer.setSpecialSlotEquipmentId(specialSlotEquipment.getId());
        roomPlayer.setCardSlotEquipmentId(cardSlotEquipment.getId());
        roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
        roomPlayer.setPosition((short) newPosition);
        roomPlayer.setMaster(false);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        ftClient.setActiveRoom(room);
        ftClient.setInLobby(false);

        handleRoomUponJoin(room);

        ftClient.getIsJoiningOrLeavingRoom().set(false);
    }

    private void handleRoomUponJoin(Room room) {
        FTClient client = (FTClient) connection.getClient();

        Optional<RoomPlayer> roomPlayerMaster = room.getRoomPlayerList().stream().filter(RoomPlayer::isMaster).findFirst();

        S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) 0, room.getRoomType(), room.getMode(), room.getMap());
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);

        connection.sendTCP(roomJoinAnswerPacket);
        connection.sendTCP(roomInformationPacket);

        if (roomPlayerMaster.isPresent()) {
            RoomPlayer master = roomPlayerMaster.get();
            Long accountId = master.getPlayer().getAccount().getId();
            AccountHome accountHome = homeService.findAccountHomeByAccountId(accountId);
            List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);

            S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(homeInventoryList);
            connection.sendTCP(homeItemsLoadAnswerPacket);
        }

        for (final RoomPlayer roomPlayer : room.getRoomPlayerList()) {
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(roomPlayer);
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomPlayerInformationPacket, client.getConnection());
        }

        GameManager.getInstance().updateLobbyRoomListForAllClients(client.getConnection());
        GameManager.getInstance().refreshLobbyPlayerListForAllClients();

        for (final RoomPlayer roomPlayer : room.getRoomPlayerList()) {
            if (roomPlayer.getPlayerId().equals(client.getActivePlayerId())) {
                continue;
            }

            if (room.getMode() == 0) {
                C2SChatSquareMovePacket chatSquareMovePacket = roomPlayer.getLastSquareMovePacket().get();
                if (chatSquareMovePacket != null) {
                    S2CChatSquareMovePacket chatSquareMovePacketAnswer = new S2CChatSquareMovePacket(roomPlayer.getPosition(), (byte) 1, chatSquareMovePacket.getX2(), chatSquareMovePacket.getY2());
                    connection.sendTCP(chatSquareMovePacketAnswer);
                }
            }

            if (room.getMode() == 1) {
                C2SChatHousePositionPacket chatHousePositionPacket = roomPlayer.getLastHousePositionPacket().get();
                C2SChatHouseMovePacket chatHouseMovePacket = roomPlayer.getLastHouseMovePacket().get();
                if (chatHousePositionPacket != null) {
                    S2CChatHousePositionPacket chatHousePositionPacketAnswer = new S2CChatHousePositionPacket(roomPlayer.getPosition(), chatHousePositionPacket.getLevel(), chatHousePositionPacket.getX(), chatHousePositionPacket.getY());
                    connection.sendTCP(chatHousePositionPacketAnswer);
                }
                if (chatHouseMovePacket != null) {
                    byte level = chatHousePositionPacket == null ? (byte) 0 : chatHousePositionPacket.getLevel();
                    S2CChatHousePositionPacket chatHousePositionPacketAnswer = new S2CChatHousePositionPacket(roomPlayer.getPosition(), level, chatHouseMovePacket.getX(), chatHouseMovePacket.getY());
                    S2CChatHouseMovePacket chatHouseMovePacketAnswer = new S2CChatHouseMovePacket(roomPlayer.getPosition(), chatHouseMovePacket.getUnk1(), chatHouseMovePacket.getUnk2(), chatHouseMovePacket.getX(), chatHouseMovePacket.getY(), chatHouseMovePacket.getAnimationType(), chatHouseMovePacket.getUnk3());
                    connection.sendTCP(chatHousePositionPacketAnswer);
                    connection.sendTCP(chatHouseMovePacketAnswer);
                }
            }
        }
    }

    private void resetIsJoiningOrLeavingRoom(FTClient ftClient) {
        ftClient.getIsJoiningOrLeavingRoom().set(false);
    }
}
