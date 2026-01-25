package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CHousingRewardItemPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.packets.chat.house.CMSGChatHouseMove;
import com.jftse.server.core.shared.packets.chat.house.SMSGChatHouseMove;

import java.util.Calendar;
import java.util.TimeZone;

@PacketId(CMSGChatHouseMove.PACKET_ID)
public class ChatHouseMovePacketHandler implements PacketHandler<FTConnection, CMSGChatHouseMove> {
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;
    private final ProductService productService;

    private final short christmasTreeX = 44;
    private final short christmasTreeY = 23;
    private final int radius = 6;

    public ChatHouseMovePacketHandler() {
        this.playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
        this.pocketService = ServiceManager.getInstance().getPocketService();
        this.productService = ServiceManager.getInstance().getProductService();
    }

    @Override
    public void handle(FTConnection connection, CMSGChatHouseMove chatHouseMovePacket) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        Room room = client.getActiveRoom();

        if (roomPlayer.isFitting())
            return;

        int deltaX = chatHouseMovePacket.getX() - christmasTreeX;
        int deltaY = chatHouseMovePacket.getY() - christmasTreeY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance < radius) {
            double angle = Math.atan2(deltaY, deltaX);
            int adjustedX = (int) (christmasTreeX + Math.cos(angle) * radius);
            int adjustedY = (int) (christmasTreeY + Math.sin(angle) * radius);

            chatHouseMovePacket.setX((short) adjustedX);
            chatHouseMovePacket.setY((short) adjustedY);
        }

        pickARandomReward(connection, room, roomPlayer);

        SMSGChatHouseMove answerHouseMove = SMSGChatHouseMove.builder()
                .position(roomPlayer.getPosition())
                .unk0(chatHouseMovePacket.getUnk0())
                .unk1(chatHouseMovePacket.getUnk1())
                .x(chatHouseMovePacket.getX())
                .y(chatHouseMovePacket.getY())
                .animationType(chatHouseMovePacket.getAnimationType())
                .unk2(chatHouseMovePacket.getUnk2())
                .build();
        roomPlayer.setLastX(chatHouseMovePacket.getX());
        roomPlayer.setLastY(chatHouseMovePacket.getY());

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerHouseMove, connection);
    }

    void pickARandomReward(final FTConnection connection, Room room, RoomPlayer roomPlayer) {
        if (room == null || room.getRoomId() != 0 || !ConfigService.getInstance().getValue("town-square.reward.enabled", false)) {
            return;
        }

        Product product = productService.findProductsByName("Hot Pink Watch", EItemCategory.PARTS.getName()).getFirst();

        Pocket pocket = pocketService.findById(roomPlayer.getPocketId());
        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(product.getItem0(), product.getCategory(), pocket);
        int existingItemCount = 0;
        boolean existingItem = false;

        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
            existingItemCount = playerPocket.getItemCount();
            existingItem = true;
        } else {
            playerPocket = new PlayerPocket();
        }

        playerPocket.setCategory(product.getCategory());
        playerPocket.setItemIndex(product.getItem0());
        playerPocket.setUseType(product.getUseType());

        int quantity = 1;
        if (playerPocket.getUseType().equals("N/A") && quantity > 1 && existingItemCount == 0)
            quantity = 1;

        playerPocket.setItemCount(quantity + existingItemCount);

        if (playerPocket.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, playerPocket.getItemCount());

            playerPocket.setCreated(cal.getTime());
            playerPocket.setItemCount(1);
        }
        playerPocket.setPocket(pocket);

        playerPocket = playerPocketService.save(playerPocket);
        if (!existingItem)
            pocket = pocketService.incrementPocketBelongings(pocket);

        S2CHousingRewardItemPacket housingRewardItemPacket = new S2CHousingRewardItemPacket(playerPocket, true);
        connection.sendTCP(housingRewardItemPacket);
    }
}
