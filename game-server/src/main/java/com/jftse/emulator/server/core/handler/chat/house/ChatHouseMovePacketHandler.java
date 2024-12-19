package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.house.C2SChatHouseMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CChatHouseMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CHousingRewardItemPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;

import java.util.Calendar;
import java.util.TimeZone;

@PacketOperationIdentifier(PacketOperations.C2SChatHouseMove)
public class ChatHouseMovePacketHandler extends AbstractPacketHandler {
    private C2SChatHouseMovePacket chatHouseMovePacket;

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
    public boolean process(Packet packet) {
        chatHouseMovePacket = new C2SChatHouseMovePacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
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

        pickARandomReward(room, roomPlayer);

        S2CChatHouseMovePacket answerHouseMovePacket = new S2CChatHouseMovePacket(roomPlayer.getPosition(), chatHouseMovePacket.getUnk1(), chatHouseMovePacket.getUnk2(), chatHouseMovePacket.getX(), chatHouseMovePacket.getY(), chatHouseMovePacket.getAnimationType(), chatHouseMovePacket.getUnk3());
        roomPlayer.setLastX(chatHouseMovePacket.getX());
        roomPlayer.setLastY(chatHouseMovePacket.getY());

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerHouseMovePacket, client.getConnection());
    }

    void pickARandomReward(Room room, RoomPlayer roomPlayer) {
        if (room == null || room.getRoomId() != 0 || !ConfigService.getInstance().getValue("town-square.reward.enabled", false)) {
            return;
        }

        Product product = productService.findProductsByName("Hot Pink Watch", EItemCategory.PARTS.getName()).getFirst();

        Pocket pocket = pocketService.findById(roomPlayer.getPlayer().getPocket().getId());
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
