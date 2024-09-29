package com.jftse.emulator.server.core.interaction;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.chat.S2CChatLobbyAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.S2CChatRoomAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class PlayerScriptableImpl implements PlayerScriptable {
    private FTClient client;
    private FTPlayer ftPlayer;

    private final ServiceManager serviceManager;
    private final GameManager gameManager;

    private PlayerScriptableImpl() {
        this.serviceManager = ServiceManager.getInstance();
        this.gameManager = GameManager.getInstance();
    }

    public PlayerScriptableImpl(Long playerId) throws ValidationException {
        this();
        this.ftPlayer = new FTPlayer(playerId);

        FTConnection connection = gameManager.getConnectionByPlayerId(playerId);
        if (connection != null)
            this.client = connection.getClient();
    }

    public PlayerScriptableImpl(FTClient client) {
        this();
        this.client = client;
    }

    public Optional<FTPlayer> getPlayer() {
        if (this.ftPlayer == null && this.client != null) {
            try {
                this.ftPlayer = new FTPlayer(this.client.getPlayer().getId());
            } catch (ValidationException e) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(ftPlayer);
    }

    @Override
    public void giveExp(int exp) {
        Optional<FTPlayer> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || exp <= 0)
            return;

        Player player = optionalPlayer.get().getPlayer();

        final byte currentLevel = player.getLevel();
        final byte level = serviceManager.getLevelService().getLevel(exp, player.getExpPoints(), currentLevel);
        if ((level < ConfigService.getInstance().getValue("player.level.max", 60)) || (currentLevel < level))
            player.setExpPoints(player.getExpPoints() + exp);
        serviceManager.getLevelService().setNewLevelStatusPoints(level, player);
    }

    @Override
    public void giveGold(int gold) {
        Optional<FTPlayer> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || gold <= 0)
            return;

        Player player = optionalPlayer.get().getPlayer();

        player.setGold(player.getGold() + gold);
        serviceManager.getPlayerService().save(player);
    }

    @Override
    public void giveAp(int ap) {
        Optional<FTPlayer> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || ap <= 0)
            return;

        Player player = optionalPlayer.get().getPlayer();

        Account account = ServiceManager.getInstance().getAuthenticationService().findAccountById(player.getAccount().getId());
        if (account == null)
            return;

        account.setAp(account.getAp() + ap);
        serviceManager.getAuthenticationService().updateAccount(account);
    }

    @Override
    public void giveItem(int productIndex, int quantity) {
        Optional<FTPlayer> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || productIndex <= 0 || quantity <= 0)
            return;

        FTPlayer player = optionalPlayer.get();
        player.getInventory().addItem(productIndex, quantity);
    }

    @Override
    public void giveItem(int itemIndex, String category, int quantity) {
        Optional<FTPlayer> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || itemIndex <= 0 || quantity <= 0)
            return;

        try {
            EItemCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }

        FTPlayer player = optionalPlayer.get();
        player.getInventory().addItem(itemIndex, category, quantity);
    }

    @Override
    public void sendGift(int productIndex, int quantity, String message) {
        Optional<FTPlayer> optionalPlayer = getPlayer();
        if (optionalPlayer.isEmpty() || productIndex <= 0 || quantity <= 0)
            return;

        Product product = serviceManager.getProductService().findProductByProductItemIndex(productIndex);
        if (product == null)
            return;

        FTPlayer player = optionalPlayer.get();

        Player sender = serviceManager.getPlayerService().findByName("JFTSE");
        if (sender == null) {
            sender = new Player();
            sender.setId(player.getPlayer().getId());
        }

        Gift gift = new Gift();
        gift.setReceiver(player.getPlayer());
        gift.setSender(sender);
        gift.setMessage(message);
        gift.setSeen(false);
        gift.setProduct(product);
        gift.setUseTypeOption((byte) 0);
        serviceManager.getGiftService().save(gift);

        player.getInventory().addItem(productIndex, quantity);
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
