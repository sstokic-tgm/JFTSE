package com.jftse.emulator.server.core.interaction;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.core.packets.messenger.S2CReceivedMessageNotificationPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.messenger.Message;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.shared.rabbit.messages.PacketMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Implementation of PlayerScriptable interface to interact with a player.
 * Provides methods to give experience, gold, ability points, items,
 * send gifts, and send messages to the player.
 *
 * Usage:
 * <pre>
 *     PlayerScriptable playerScriptable = new PlayerScriptableImpl(playerId);
 *     playerScriptable.giveExp(1000);
 *     playerScriptable.sendMessage("Hello, Player!");
 * </pre>
 *
 * Usage within scripts:
 * <pre>
 *     const PlayerScriptableImpl = Java.type("com.jftse.emulator.server.core.interaction.PlayerScriptableImpl");
 *
 *     let player = new PlayerScriptableImpl(playerId);
 *     player.giveGold(500);
 *     player.giveItem(12345, 2);
 *     player.sendGift(67890, 1, "Enjoy this gift!");
 * </pre>
 */
@Getter
@Setter
public class PlayerScriptableImpl implements PlayerScriptable {
    private FTClient client;
    private Long playerId;

    private final ServiceManager serviceManager;
    private final GameManager gameManager;

    private PlayerScriptableImpl() {
        this.serviceManager = ServiceManager.getInstance();
        this.gameManager = GameManager.getInstance();
    }

    /**
     * Constructs a PlayerScriptableImpl for the given player ID.
     *
     * @param playerId the ID of the player
     * @throws ValidationException if the player ID is invalid
     */
    public PlayerScriptableImpl(Long playerId) throws ValidationException {
        this();
        this.playerId = playerId;

        FTConnection connection = gameManager.getConnectionByPlayerId(playerId);
        if (connection != null)
            this.client = connection.getClient();
    }

    /**
     * Constructs a PlayerScriptableImpl for the given FTClient.
     *
     * @param client the FTClient instance
     */
    public PlayerScriptableImpl(FTClient client) {
        this();
        this.client = client;
    }

    /**
     * Retrieves the db Player associated with this scriptable.
     * If the Player ID is not set, it attempts to fetch it from the client.
     *
     * @return an Optional containing the Player if available, otherwise an empty Optional
     */
    public Optional<Player> getPlayer() {
        if (this.playerId == null && this.client != null) {
            try {
                this.playerId = this.client.getPlayer().getId();
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        Player p = serviceManager.getPlayerService().findById(this.playerId);
        return Optional.ofNullable(p);
    }

    @Override
    public void giveExp(int exp) {
        Optional<Player> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || exp <= 0)
            return;

        Player player = optionalPlayer.get();

        final byte currentLevel = player.getLevel();
        final byte level = serviceManager.getLevelService().getLevel(exp, player.getExpPoints(), currentLevel);
        if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (currentLevel < level))
            player.setExpPoints(player.getExpPoints() + exp);
        serviceManager.getLevelService().setNewLevelStatusPoints(level, player);

        if (client != null && client.hasPlayer()) {
            client.getPlayer().syncExpPoints(player.getExpPoints());
            client.getPlayer().syncLevel(player.getLevel());
        }
    }

    @Override
    public void giveGold(int gold) {
        Optional<Player> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || gold <= 0)
            return;

        Player player = optionalPlayer.get();

        player.setGold(player.getGold() + gold);
        serviceManager.getPlayerService().save(player);

        if (client != null && client.hasPlayer()) {
            client.getPlayer().syncGold(player.getGold());
        }
    }

    @Override
    public void giveAp(int ap) {
        Optional<Player> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || ap <= 0)
            return;

        Player player = optionalPlayer.get();

        Account account = ServiceManager.getInstance().getAuthenticationService().findAccountById(player.getAccount().getId());
        if (account == null)
            return;

        int currentAp = account.getAp();
        int newAp = currentAp + ap;
        account.setAp(newAp);
        serviceManager.getAuthenticationService().updateAccount(account);

        if (client != null && client.hasPlayer()) {
            client.getAp().compareAndSet(currentAp, newAp);
        }
    }

    @Override
    public void giveItem(int productIndex, int quantity) {
        if (playerId == null || productIndex <= 0 || quantity <= 0)
            return;

        serviceManager.getInventoryService().addItem(playerId, productIndex, quantity, null);
    }

    @Override
    public void giveItem(int itemIndex, String category, int quantity) {
        if (playerId == null || itemIndex <= 0 || quantity <= 0)
            return;

        try {
            EItemCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        serviceManager.getInventoryService().addItem(playerId, itemIndex, category, quantity, null);
    }

    @Override
    public void sendGift(int productIndex, int quantity, String message) {
        Optional<Player> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || productIndex <= 0 || quantity <= 0)
            return;

        Product product = serviceManager.getProductService().findProductByProductItemIndex(productIndex);
        if (product == null)
            return;

        Player player = optionalPlayer.get();

        Player sender = serviceManager.getPlayerService().findByName("JFTSE");
        if (sender == null) {
            sender = new Player();
            sender.setId(player.getId());
        }

        Gift gift = new Gift();
        gift.setReceiver(player);
        gift.setSender(sender);
        gift.setMessage(message);
        gift.setSeen(false);
        gift.setProduct(product);
        gift.setUseTypeOption((byte) 0);
        serviceManager.getGiftService().save(gift);

        serviceManager.getInventoryService().addItem(player.getId(), productIndex, quantity, null);
    }

    @Override
    public void sendMessage(String message) {
        Optional<Player> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || message == null || message.isEmpty())
            return;

        Player player = optionalPlayer.get();

        Player sender = serviceManager.getPlayerService().findByName("JFTSE");
        if (sender == null) {
            sender = new Player();
            sender.setId(player.getId());
        }

        Message msg = new Message();
        msg.setSeen(false);
        msg.setSender(sender);
        msg.setReceiver(player);
        msg.setMessage(message);
        serviceManager.getMessageService().save(msg);

        FTConnection connection = gameManager.getConnectionByPlayerId(player.getId());
        if (connection != null) {
            S2CReceivedMessageNotificationPacket notifyPacket = new S2CReceivedMessageNotificationPacket(msg, sender.getName());

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(player.getId())
                    .packet(notifyPacket)
                    .build();
            RProducerService.getInstance().send(packetMessage, "game.messenger.message chat.messenger.message", player.getName() + "(GameServer)");
        }
    }

    @Override
    public void sendMessage(String sender, String message) {
        Optional<Player> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || sender == null || sender.isEmpty() || message == null || message.isEmpty())
            return;

        Player player = optionalPlayer.get();

        Player senderPlayer = serviceManager.getPlayerService().findByName(sender);
        if (senderPlayer == null) {
            senderPlayer = serviceManager.getPlayerService().findByName("JFTSE");
            if (senderPlayer == null) {
                senderPlayer = new Player();
                senderPlayer.setId(player.getId());
            }
        }

        Message msg = new Message();
        msg.setSeen(false);
        msg.setSender(senderPlayer);
        msg.setReceiver(player);
        msg.setMessage(message);
        serviceManager.getMessageService().save(msg);

        FTConnection connection = gameManager.getConnectionByPlayerId(player.getId());
        if (connection != null) {
            S2CReceivedMessageNotificationPacket notifyPacket = new S2CReceivedMessageNotificationPacket(msg, senderPlayer.getName());

            PacketMessage packetMessage = PacketMessage.builder()
                    .receivingPlayerId(player.getId())
                    .packet(notifyPacket)
                    .build();
            RProducerService.getInstance().send(packetMessage, "game.messenger.message chat.messenger.message", player.getName() + "(GameServer)");
        }
    }


    @Override
    public void sendChat(String name, String message) {
        if (client == null)
            return;

        Packet packet = client.isInLobby() && client.getActiveRoom() == null ? new S2CChatLobbyAnswerPacket((char) 0, name, message) : new S2CChatRoomAnswerPacket((byte) 2, name, message);
        if (client.getConnection() != null)
            client.getConnection().sendTCP(packet);
    }

    @Override
    public void sendChat(String name, String message, Integer chatMode) {
        if (client == null)
            return;

        char cm = (char) (chatMode == null || chatMode == 0 ? 0 : chatMode);
        Packet packet = client.isInLobby() && client.getActiveRoom() == null ? new S2CChatLobbyAnswerPacket(cm, name, message) : new S2CChatRoomAnswerPacket((byte) cm, name, message);
        if (client.getConnection() != null)
            client.getConnection().sendTCP(packet);
    }
}
