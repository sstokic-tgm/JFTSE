package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.messenger.Gift;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.transaction.TransactionType;
import com.jftse.server.core.service.TransactionLogService;
import com.jftse.server.core.item.AddItemHook;
import com.jftse.server.core.item.GoldBackAdded;
import com.jftse.server.core.service.GiftService;
import com.jftse.server.core.service.GiftTransactionService;
import com.jftse.server.core.service.InventoryService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.service.transaction.GiftTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionLogBuilder;
import com.jftse.server.core.service.transaction.TransactionParticipants;
import com.jftse.server.core.service.transaction.TransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.jftse.server.core.service.transaction.TransactionException.Reason.*;

@Service
@RequiredArgsConstructor
public class GiftTransactionServiceImpl implements GiftTransactionService {
    private final TransactionParticipants transactionParticipants;
    private final TransactionLogService transactionLogService;
    private final ProductService productService;
    private final PlayerService playerService;
    private final GiftService giftService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public GiftTransactionResult send(Long accountId, Long playerId, String receiverName,
                                      int productIndex, byte option, String message) {
        Product product = productService.findProductByProductItemIndex(productIndex);
        Player receiver = playerService.findByName(receiverName);
        if (product == null || receiver == null) {
            throw new TransactionException(NOT_FOUND);
        }

        TransactionParticipants.Participants participants = transactionParticipants.lock(accountId, playerId, receiver);
        Account account = participants.account();
        Player sender = participants.player();
        receiver = participants.counterparty();

        if (!Boolean.TRUE.equals(product.getEnabled()) || sender.getLevel() < 20) {
            throw new TransactionException(NOT_FOR_SALE);
        }

        List<Gift> receivedGifts = giftService.findWithPlayerByReceiver(receiver.getId());
        List<Gift> sentGifts = giftService.findWithPlayerBySender(sender.getId());
        if (receivedGifts.size() > 128 || sentGifts.size() > 128) {
            throw new TransactionException(LIMIT_REACHED);
        }

        int price = getPrice(product, option);
        int costsGold = 0;
        int costsAp = 0;
        if (PriceType.GOLD.getName().equals(product.getPriceType())) {
            costsGold = price;
        } else if (PriceType.MINT.getName().equals(product.getPriceType())) {
            costsAp = price;
        } else {
            throw new TransactionException(NOT_FOR_SALE);
        }

        int goldBefore = sender.getGold();
        int apBefore = account.getAp();
        if (goldBefore < costsGold) {
            throw new TransactionException(INSUFFICIENT_GOLD);
        }
        if (apBefore < costsAp) {
            throw new TransactionException(INSUFFICIENT_AP);
        }

        TransactionLogBuilder journal = new TransactionLogBuilder(TransactionType.SHOP_GIFT, accountId, playerId);
        journal.counterparty(receiver);

        List<AddItemHook> postActions = new ArrayList<>();
        List<PlayerPocket> items = inventoryService.addItem(receiver.getId(), productIndex,
                getQuantity(product, option), List.of(postActions::add));

        long goldBack = 0;
        for (AddItemHook hook : postActions) {
            journal.hook(hook, product, option);
            if (hook instanceof GoldBackAdded added) {
                goldBack = Math.addExact(goldBack, added.amount());
            }
        }

        Gift gift = new Gift();
        gift.setReceiver(receiver);
        gift.setSender(sender);
        gift.setMessage(message);
        gift.setSeen(false);
        gift.setProduct(product);
        gift.setUseTypeOption(option);
        gift = giftService.save(gift);

        int goldAfter = Math.toIntExact(Math.addExact((long) goldBefore - costsGold, goldBack));
        int apAfter = apBefore - costsAp;
        if (goldAfter < 0) {
            throw new TransactionException(INVALID_REQUEST);
        }

        sender.setGold(goldAfter);
        account.setAp(apAfter);
        journal.getTransaction().setSourceId(gift.getId());
        journal.amount(PriceType.GOLD, goldBefore, goldBefore - costsGold);
        journal.amount(PriceType.GOLD, goldBefore - costsGold, goldAfter);
        journal.amount(PriceType.MINT, apBefore, apAfter);
        TransactionSummary summary = transactionLogService.save(journal.getTransaction());

        return new GiftTransactionResult(gift, goldAfter, apAfter, items, postActions,
                summary);
    }

    private int getPrice(Product product, byte option) {
        Integer price = switch (option) {
            case 0 -> product.getPrice0();
            case 1 -> product.getPrice1();
            case 2 -> product.getPrice2();
            default -> throw new TransactionException(INVALID_REQUEST);
        };
        if (price == null || price < 0) {
            throw new TransactionException(INVALID_REQUEST);
        }
        return price;
    }

    private int getQuantity(Product product, byte option) {
        Integer quantity = switch (option) {
            case 0 -> product.getUse0() != null && product.getUse0() == 0 ? 1 : product.getUse0();
            case 1 -> product.getUse1();
            case 2 -> product.getUse2();
            default -> throw new TransactionException(INVALID_REQUEST);
        };
        if (quantity == null || quantity <= 0) {
            throw new TransactionException(INVALID_REQUEST);
        }
        return quantity;
    }
}
