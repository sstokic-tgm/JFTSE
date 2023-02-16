package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.room.*;
import com.jftse.emulator.server.core.service.ClothEquipmentService;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.SocialService;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class RoomJoinRequestPacketHandler extends AbstractHandler {
    private C2SRoomJoinRequestPacket roomJoinRequestPacket;

    private final GuildMemberService guildMemberService;
    private final ClothEquipmentService clothEquipmentService;
    private final SocialService socialService;

    public RoomJoinRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public boolean process(Packet packet) {
        roomJoinRequestPacket = new C2SRoomJoinRequestPacket(packet, new ArrayList<>(GameManager.getInstance().getRooms()));
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);
            return;
        }

        Room room = GameManager.getInstance().getRooms().stream()
                .filter(r -> r.getRoomId() == roomJoinRequestPacket.getRoomId())
                .findAny()
                .orElse(null);

        // prevent abusive room joins
        if (room != null && connection.getClient().getActiveRoom() != null) {
            Room clientRoom = connection.getClient().getActiveRoom();

            handleRoomUponJoin(clientRoom, clientRoom.getRoomId());

            return;
        }

        if (room == null) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(new ArrayList<>(GameManager.getInstance().getRooms()));

            connection.sendTCP(roomJoinAnswerPacket, roomListAnswerPacket);
            return;
        }

        if (room.getStatus() != RoomStatus.NotRunning) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -1, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
            return;
        }

        Player activePlayer = connection.getClient().getPlayer();
        Account account = connection.getClient().getAccount();
        if (!account.getGameMaster()) {
            if (room.isPrivate() && (StringUtils.isEmpty(roomJoinRequestPacket.getPassword()) || !roomJoinRequestPacket.getPassword().equals(room.getPassword()))) {
                S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -5, (byte) 0, (byte) 0, (byte) 0);
                connection.sendTCP(roomJoinAnswerPacket);

                GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
                return;
            }

            boolean anyPositionAvailable = room.getPositions().stream().anyMatch(x -> x == RoomPositionState.Free);
            if (!anyPositionAvailable) {
                S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
                connection.sendTCP(roomJoinAnswerPacket);

                GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
                return;
            }
        }

        if ((room.isHardMode() || room.isArcade()) && activePlayer.getLevel() < ConfigService.getInstance().getValue("command.room.mode.change.player.level", 60)) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
            return;
        }

        if (room.getBannedPlayers().contains(activePlayer.getId())) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -4, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
            return;
        }

        boolean useGmSlot = false;
        int gmSlot = 9;
        if (account.getGameMaster()) {
            int i = 0;
            boolean isGmSlotInUse = false;
            for (Short pos : room.getPositions()) {
                if (i == gmSlot && pos == RoomPositionState.InUse) {
                    isGmSlotInUse = true;
                    break;
                }
                i++;
            }
            boolean anyPositionAvailable = room.getPositions().stream().anyMatch(x -> x == RoomPositionState.Free);
            if (!isGmSlotInUse) {
                useGmSlot = true;
            } else if (!anyPositionAvailable) {
                S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
                connection.sendTCP(roomJoinAnswerPacket);

                GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
                return;
            }
        }

        if (activePlayer.getLevel() < (room.getLevel() - room.getLevelRange()) && activePlayer.getLevel() > room.getLevel()) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
            return;
        }

        Optional<Short> num = room.getPositions().stream().filter(x -> x == RoomPositionState.Free).findFirst();
        int newPosition = useGmSlot ? 9 : room.getPositions().indexOf(num.get());;

        if (newPosition == -1) {
            S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) -10, (byte) 0, (byte) 0, (byte) 0);
            connection.sendTCP(roomJoinAnswerPacket);

            GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
            return;
        }

        room.getPositions().set(newPosition, RoomPositionState.InUse);

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayerId(activePlayer.getId());

        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        Friend couple = socialService.getRelationship(activePlayer);
        ClothEquipment clothEquipment = clothEquipmentService.findClothEquipmentById(roomPlayer.getPlayer().getClothEquipment().getId());
        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(roomPlayer.getPlayer());

        roomPlayer.setGuildMemberId(guildMember == null ? null : guildMember.getId());
        roomPlayer.setCoupleId(couple == null ? null : couple.getId());
        roomPlayer.setClothEquipmentId(clothEquipment.getId());
        roomPlayer.setStatusPointsAddedDto(statusPointsAddedDto);
        roomPlayer.setPosition((short) newPosition);
        roomPlayer.setMaster(false);
        roomPlayer.setFitting(false);
        room.getRoomPlayerList().add(roomPlayer);

        connection.getClient().setActiveRoom(room);
        connection.getClient().setInLobby(false);

        handleRoomUponJoin(room, roomJoinRequestPacket.getRoomId());
    }

    private void handleRoomUponJoin(Room room, short roomId) {
        S2CRoomJoinAnswerPacket roomJoinAnswerPacket = new S2CRoomJoinAnswerPacket((char) 0, (byte) 0, (byte) 0, (byte) 0);
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);

        connection.sendTCP(roomJoinAnswerPacket, roomInformationPacket);

        final ArrayList<Short> positions = room.getPositions();
        List<Packet> roomSlotCloseAnswerPackets = new ArrayList<>();
        closeRoomSlots(positions, roomSlotCloseAnswerPackets);

        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(room.getRoomPlayerList()));
        GameManager.getInstance().getClientsInRoom(roomId).forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
                c.getConnection().sendTCP(roomPlayerInformationPacket);
            }
        });
        GameManager.getInstance().updateRoomForAllClientsInMultiplayer(connection, room);
        GameManager.getInstance().refreshLobbyPlayerListForAllClients();
    }

    private void closeRoomSlots(ArrayList<Short> positions, List<Packet> roomSlotCloseAnswerPackets) {
        int i = 0;
        for (Iterator<Short> it = positions.iterator(); it.hasNext(); ) {
            short positionState = it.next();
            if (positionState == RoomPositionState.Locked) {
                S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket((byte) i, true);
                roomSlotCloseAnswerPackets.add(roomSlotCloseAnswerPacket);
            }
            i++;
        }
        connection.sendTCP(roomSlotCloseAnswerPackets.toArray(Packet[]::new));
    }
}
