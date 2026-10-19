package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.transaction.TransactionType;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.repository.transaction.TransactionAccountRepository;
import com.jftse.entities.database.repository.transaction.TransactionPlayerRepository;
import com.jftse.entities.database.repository.transaction.TransactionPocketItemRepository;
import com.jftse.server.core.item.InventoryItemSnapshot;
import com.jftse.server.core.service.InventorySaleService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.TransactionLogService;
import com.jftse.server.core.service.transaction.InventorySaleResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionLogBuilder;
import com.jftse.server.core.service.transaction.TransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jftse.server.core.service.transaction.TransactionException.Reason.*;

@Service
@RequiredArgsConstructor
public class InventorySaleServiceImpl implements InventorySaleService {
    private final TransactionAccountRepository accountRepository;
    private final TransactionPlayerRepository playerRepository;
    private final TransactionPocketItemRepository transactionPocketItemRepository;
    private final TransactionLogService transactionLogService;
    private final PlayerPocketService playerPocketService;
    private final PocketService pocketService;

    @Override
    @Transactional
    public InventorySaleResult sell(Long accountId, Long playerId, Long itemPocketId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new TransactionException(INVALID_REQUEST));
        Player player = playerRepository.findForTransaction(playerId, accountId)
                .orElseThrow(() -> new TransactionException(INVALID_REQUEST));
        PlayerPocket item = transactionPocketItemRepository.findByIdAndPocketId(itemPocketId, player.getPocket().getId())
                .orElseThrow(() -> new TransactionException(NO_ITEM));
        if (item.getItemCount() == null || item.getItemCount() <= 0 || player.getPocket().getBelongings() <= 0) {
            throw new TransactionException(INVALID_REQUEST);
        }

        int price = playerPocketService.getSellPrice(item);
        if (price < 0) {
            throw new TransactionException(INVALID_REQUEST);
        }

        int goldBefore = player.getGold();
        int goldAfter = Math.addExact(goldBefore, price);
        int itemId = Math.toIntExact(item.getId());
        TransactionLogBuilder journal = new TransactionLogBuilder(TransactionType.INVENTORY_SELL, accountId, playerId);
        journal.pocket(InventoryItemSnapshot.from(item), null, null, null);
        journal.amount(PriceType.GOLD, goldBefore, goldAfter);

        playerPocketService.remove(item.getId());
        pocketService.decrementPocketBelongings(player.getPocket());
        player.setGold(goldAfter);
        TransactionSummary summary = transactionLogService.save(journal.getTransaction());

        return new InventorySaleResult(goldAfter, account.getAp(), price, item.getItemCount(), itemId,
                summary);
    }
}
