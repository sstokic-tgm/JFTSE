package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.room.*;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.*;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.CardSlotEquipmentService;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.SocialService;
import com.jftse.server.core.service.SpecialSlotEquipmentService;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomJoin;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomCloseSlot;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomJoin;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

@PacketId(CMSGRoomJoin.PACKET_ID)
public class RoomJoinRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomJoin> {
    private final GuildMemberService guildMemberService;
    private final ClothEquipmentServiceImpl clothEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final SocialService socialService;

    public RoomJoinRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        specialSlotEquipmentService = ServiceManager.getInstance().getSpecialSlotEquipmentService();
        cardSlotEquipmentService = ServiceManager.getInstance().getCardSlotEquipmentService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRoomJoin roomJoinRequestPacket) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null) {
            SMSGRoomJoin answer = SMSGRoomJoin.builder()
                    .result((char) -10)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(answer);
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
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -10)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(new ArrayList<>(GameManager.getInstance().getRooms().stream().filter(r -> !(r.getRoomType() == 1 && r.getMode() == 2)).toList()));

            resetIsJoiningOrLeavingRoom(ftClient);

            connection.sendTCP(roomJoinAnswerPacket, roomListAnswerPacket);
            return;
        }

        final boolean isTownSquare = room.getRoomType() == 1 && room.getMode() == 2;
        final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();

        if (room.getStatus() != RoomStatus.NotRunning) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -1)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        Player activePlayer = ftClient.getPlayer();
        if (!ftClient.isGameMaster() && room.isPrivate() && (StringUtils.isEmpty(roomJoinRequestPacket.getPassword()) || !roomJoinRequestPacket.getPassword().equals(room.getPassword()))) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -5)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        boolean anyPositionAvailable;
        if (isTownSquare) {
            anyPositionAvailable = roomPlayerList.size() < room.getPlayers();
        } else {
            anyPositionAvailable = room.getPositions().stream().anyMatch(x -> x == RoomPositionState.Free);
        }

        if (!anyPositionAvailable) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -10)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        // prevent abusive room joins
        if (ftClient.getActiveRoom() != null) {
            Room clientRoom = ftClient.getActiveRoom();
            handleRoomUponJoin(connection, clientRoom, true);

            resetIsJoiningOrLeavingRoom(ftClient);
            return;
        }

        if ((room.isHardMode() || room.isArcade()) && activePlayer.getLevel() < ConfigService.getInstance().getValue("command.room.mode.change.player.level", 60)) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -10)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        if (room.getBannedPlayers().contains(activePlayer.getId())) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -4)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        boolean useGmSlot = false;
        int gmSlot = 9;
        if (!isTownSquare) {
            if (ftClient.isGameMaster()) {
                int i = 0;
                boolean isGmSlotInUse = false;
                for (Short pos : room.getPositions()) {
                    if (i == gmSlot && pos == RoomPositionState.InUse) {
                        isGmSlotInUse = true;
                        break;
                    }
                    i++;
                }
                anyPositionAvailable = room.getPositions().stream().anyMatch(x -> x == RoomPositionState.Free);
                if (!isGmSlotInUse) {
                    useGmSlot = true;
                } else if (!anyPositionAvailable) {
                    SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                            .result((char) -10)
                            .roomType((byte) 0)
                            .mode((byte) 0)
                            .mapId((byte) 0)
                            .build();
                    connection.sendTCP(roomJoinAnswerPacket);

                    resetIsJoiningOrLeavingRoom(ftClient);

                    GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
                    return;
                }
            }
        }

        if (activePlayer.getLevel() < (room.getLevel() - room.getLevelRange()) && activePlayer.getLevel() > room.getLevel()) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -10)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        int newPosition = -1;
        if (!isTownSquare) {
            Optional<Short> num = room.getPositions().stream().filter(x -> x == RoomPositionState.Free).findFirst();
            newPosition = useGmSlot ? 9 : num.map(pos -> room.getPositions().indexOf(pos)).orElse(-1);
        } else {
            List<Short> positions = roomPlayerList.stream().map(RoomPlayer::getPosition).toList();
            newPosition = (short) IntStream.range(0, room.getPlayers())
                    .filter(p -> !positions.contains((short) p))
                    .findFirst()
                    .orElse(-1);
        }

        if (newPosition == -1) {
            SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                    .result((char) -10)
                    .roomType((byte) 0)
                    .mode((byte) 0)
                    .mapId((byte) 0)
                    .build();
            connection.sendTCP(roomJoinAnswerPacket);

            resetIsJoiningOrLeavingRoom(ftClient);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(ftClient.getConnection(), room);
            return;
        }

        if (!isTownSquare) {
            room.getPositions().set(newPosition, RoomPositionState.InUse);
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
        if (isTownSquare) {
            ftClient.setInLobby(true);
        } else {
            ftClient.setInLobby(false);
        }

        handleRoomUponJoin(connection, room, false);

        ftClient.getIsJoiningOrLeavingRoom().set(false);
    }

    private void handleRoomUponJoin(final FTConnection connection, Room room, boolean existingRoom) {
        FTClient client = connection.getClient();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        final boolean isTownSquare = room.getRoomType() == 1 && room.getMode() == 2;

        SMSGRoomJoin roomJoinAnswerPacket = SMSGRoomJoin.builder()
                .result((char) 0)
                .roomType(room.getRoomType())
                .mode(room.getMode())
                .mapId(room.getMap())
                .build();
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);

        connection.sendTCP(roomJoinAnswerPacket);
        connection.sendTCP(roomInformationPacket);

        Random rnd = new Random();
        float spawnX = 0.0f, spawnY = 0.0f;
        if (!isTownSquare) {
            final ArrayList<Short> positions = room.getPositions();
            closeRoomSlots(connection, positions);

            S2CRoomPlayerListInformationPacket roomPlayerListInformationPacket = new S2CRoomPlayerListInformationPacket(new ArrayList<>(room.getRoomPlayerList()));
            connection.sendTCP(roomPlayerListInformationPacket);
        } else {
            spawnX = rnd.nextFloat(40.0f, 46.0f);
            spawnY = rnd.nextFloat(60.0f, 64.0f);

            if (!existingRoom) {
                roomPlayer.setLastX(spawnX);
                roomPlayer.setLastY(spawnY);
            } else {
                roomPlayer.setLastX(roomPlayer.getLastX());
                roomPlayer.setLastY(roomPlayer.getLastY());
                client.setInLobby(true);
            }
        }
        roomPlayer.setLastMapLayer(0);

        for (final RoomPlayer rp : room.getRoomPlayerList()) {
            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(rp, isTownSquare ? rp.getLastX() : 0.0f, isTownSquare ? rp.getLastY() : 0.0f, 0.0f, 0.0f, rp.getLastMapLayer());
            if (!isTownSquare) {
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).stream()
                        .filter(c -> !c.getActivePlayerId().equals(client.getActivePlayerId()))
                        .forEach(c -> c.getConnection().sendTCP(roomPlayerInformationPacket));
            } else {
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomPlayerInformationPacket, client.getConnection());
            }
        }

        if (isTownSquare) {
            Packet enableMovement = new Packet(PacketOperations.S2CEnableTownSquareMovement);
            connection.sendTCP(enableMovement);
        }

        GameManager.getInstance().updateLobbyRoomListForAllClients(client.getConnection());
        GameManager.getInstance().refreshLobbyPlayerListForAllClients();
    }

    private void closeRoomSlots(final FTConnection connection, ArrayList<Short> positions) {
        int i = 0;
        for (Iterator<Short> it = positions.iterator(); it.hasNext(); ) {
            short positionState = it.next();
            if (positionState == RoomPositionState.Locked) {
                SMSGRoomCloseSlot closeSlot = SMSGRoomCloseSlot.builder().slot((byte) i).close(true).build();
                connection.sendTCP(closeSlot);
            }
            i++;
        }
    }

    private void resetIsJoiningOrLeavingRoom(FTClient ftClient) {
        ftClient.getIsJoiningOrLeavingRoom().set(false);
    }
}
