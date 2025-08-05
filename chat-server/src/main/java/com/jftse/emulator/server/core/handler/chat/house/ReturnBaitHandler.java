package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.Fish;
import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.core.life.housing.FishState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CFishMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.SMSGReturnBait;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.CMSG_ReturnBait)
public class ReturnBaitHandler extends AbstractPacketHandler {
    @Override
    public boolean process(com.jftse.server.core.protocol.Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null || roomPlayer == null)
            return;

        if (roomPlayer.getUsedRod().compareAndSet(true, false)) {
            Fish claimedFish = FishManager.getInstance().getClaimedFish(room.getRoomId(), roomPlayer.getPosition());
            if (claimedFish != null) {
                claimedFish.setState(FishState.IDLE);
                claimedFish.setBitBait(false);

                S2CFishMovePacket movePacket = new S2CFishMovePacket(claimedFish.getId(), (byte) claimedFish.getState().getValue(), claimedFish.getX(), claimedFish.getY(), 0.0f);
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(movePacket, (FTConnection) connection);

                claimedFish.setSpeed(FishManager.NORMAL_SPEED_1);
                claimedFish.setTurningSpeed(FishManager.NORMAL_TURNING_SPEED);

                FishManager.getInstance().frightenFishes(room.getRoomId(), roomPlayer.getBaitX(), roomPlayer.getBaitY());
            }

            FishManager.getInstance().removeBaitPosition(roomPlayer.getBaitX(), roomPlayer.getBaitY());
            roomPlayer.setBaitX(0.0f);
            roomPlayer.setBaitY(0.0f);

            SMSGReturnBait returnBaitPacket = new SMSGReturnBait(roomPlayer.getPosition());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(returnBaitPacket, (FTConnection) connection);
        }
    }
}
