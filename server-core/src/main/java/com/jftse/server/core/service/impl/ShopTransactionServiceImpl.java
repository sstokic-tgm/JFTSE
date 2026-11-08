package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.transaction.TransactionType;
import com.jftse.entities.database.repository.transaction.TransactionAccountRepository;
import com.jftse.server.core.service.TransactionLogService;
import com.jftse.entities.database.repository.transaction.TransactionPlayerRepository;
import com.jftse.server.core.item.AddItemHook;
import com.jftse.server.core.item.GoldBackAdded;
import com.jftse.server.core.service.InventoryService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.service.ShopTransactionService;
import com.jftse.server.core.service.transaction.ShopTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionLogBuilder;
import com.jftse.server.core.service.transaction.TransactionSummary;
import com.jftse.server.core.shared.packets.shop.ShopBuy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.jftse.server.core.service.transaction.TransactionException.Reason.*;

@Service
@RequiredArgsConstructor
public class ShopTransactionServiceImpl implements ShopTransactionService {
    private final TransactionAccountRepository accountRepository;
    private final TransactionPlayerRepository playerRepository;
    private final TransactionLogService transactionLogService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public ShopTransactionResult buy(Long accountId, Long playerId, List<ShopBuy> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new TransactionException(INVALID_REQUEST);
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new TransactionException(INVALID_REQUEST));
        Player player = playerRepository.findForTransaction(playerId, accountId)
                .orElseThrow(() -> new TransactionException(INVALID_REQUEST));

        Map<Product, Byte> products = new LinkedHashMap<>(productService.findProductsByItemList2(requestedItems));
        products.keySet().removeIf(product -> Boolean.TRUE.equals(product.getNoBuy()));
        if (products.isEmpty()) {
            throw new TransactionException(NOT_FOR_SALE);
        }

        int costsGold = 0;
        int costsAp = 0;
        for (Map.Entry<Product, Byte> entry : products.entrySet()) {
            Product product = entry.getKey();
            int price = getPrice(product, entry.getValue());
            if (PriceType.GOLD.getName().equals(product.getPriceType())) {
                costsGold = Math.addExact(costsGold, price);
            } else if (PriceType.MINT.getName().equals(product.getPriceType())) {
                costsAp = Math.addExact(costsAp, price);
            } else {
                throw new TransactionException(NOT_FOR_SALE);
            }
        }

        int goldBefore = player.getGold();
        int apBefore = account.getAp();
        if (goldBefore < costsGold) {
            throw new TransactionException(INSUFFICIENT_GOLD);
        }
        if (apBefore < costsAp) {
            throw new TransactionException(INSUFFICIENT_AP);
        }

        TransactionLogBuilder journal = new TransactionLogBuilder(TransactionType.SHOP_BUY, accountId, playerId);
        List<PlayerPocket> items = new ArrayList<>();
        List<AddItemHook> postActions = new ArrayList<>();
        long goldBack = 0;

        for (Map.Entry<Product, Byte> entry : products.entrySet()) {
            Product product = entry.getKey();
            byte option = entry.getValue();
            int quantity = getQuantity(product, option);
            List<AddItemHook> hooks = new ArrayList<>();
            List<PlayerPocket> addedItems = inventoryService.addItem(playerId, product.getProductIndex(),
                    quantity, List.of(hooks::add));

            items.addAll(addedItems);
            for (AddItemHook hook : hooks) {
                journal.hook(hook, product, option);
                if (hook instanceof GoldBackAdded added) {
                    goldBack = Math.addExact(goldBack, added.amount());
                }
            }
            postActions.addAll(hooks);
        }

        int goldAfter = Math.toIntExact(Math.addExact((long) goldBefore - costsGold, goldBack));
        int apAfter = apBefore - costsAp;
        if (goldAfter < 0) {
            throw new TransactionException(INVALID_REQUEST);
        }

        player.setGold(goldAfter);
        account.setAp(apAfter);
        journal.amount(PriceType.GOLD, goldBefore, goldBefore - costsGold);
        journal.amount(PriceType.GOLD, goldBefore - costsGold, goldAfter);
        journal.amount(PriceType.MINT, apBefore, apAfter);
        TransactionSummary summary = transactionLogService.save(journal.getTransaction());

        return new ShopTransactionResult(goldAfter, apAfter, costsGold, costsAp, items, postActions,
                summary);
    }

    private int getPrice(Product product, Byte option) {
        if (option == null) {
            throw new TransactionException(INVALID_REQUEST);
        }

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
