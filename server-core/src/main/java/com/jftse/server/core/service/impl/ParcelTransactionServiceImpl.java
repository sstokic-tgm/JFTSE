package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.messenger.EParcelType;
import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.transaction.TransactionAction;
import com.jftse.entities.database.model.transaction.TransactionType;
import com.jftse.server.core.service.TransactionLogService;
import com.jftse.entities.database.repository.transaction.TransactionParcelRepository;
import com.jftse.entities.database.repository.transaction.TransactionPocketItemRepository;
import com.jftse.server.core.item.InventoryItemSnapshot;
import com.jftse.server.core.service.ParcelService;
import com.jftse.server.core.service.ParcelTransactionService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.service.transaction.ParcelTransactionResult;
import com.jftse.server.core.service.transaction.TransactionException;
import com.jftse.server.core.service.transaction.TransactionLogBuilder;
import com.jftse.server.core.service.transaction.TransactionParticipants;
import com.jftse.server.core.service.transaction.TransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jftse.server.core.service.transaction.TransactionException.Reason.*;

@Service
@RequiredArgsConstructor
public class ParcelTransactionServiceImpl implements ParcelTransactionService {
    private final TransactionParticipants transactionParticipants;
    private final TransactionParcelRepository transactionParcelRepository;
    private final TransactionPocketItemRepository transactionPocketItemRepository;
    private final TransactionLogService transactionLogService;
    private final ParcelService parcelService;
    private final PlayerService playerService;
    private final PlayerPocketService playerPocketService;
    private final ProductService productService;

    @Override
    @Transactional
    public ParcelTransactionResult send(Long accountId, Long playerId, Long itemId,
                                        String receiverName, int gold, String message) {
        if (gold < 0 || gold > 1000000) {
            throw new TransactionException(INVALID_AMOUNT);
        }

        Player receiver = playerService.findByName(receiverName);
        if (receiver == null) {
            throw new TransactionException(NOT_FOUND);
        }

        TransactionParticipants.Participants participants = transactionParticipants.lock(accountId, playerId, receiver);
        Player sender = participants.player();
        receiver = participants.counterparty();

        PlayerPocket item = transactionPocketItemRepository.findById(itemId)
                .orElseThrow(() -> new TransactionException(NO_ITEM));
        if (!item.getPocket().getId().equals(sender.getPocket().getId())) {
            throw new TransactionException(FORBIDDEN);
        }
        if (item.getItemCount() == null || item.getItemCount() <= 0) {
            throw new TransactionException(INVALID_REQUEST);
        }

        Product product = productService.findProductByItemAndCategoryAndEnabledIsTrue(item.getItemIndex(), item.getCategory());
        if (product != null && !Boolean.TRUE.equals(product.getEnableParcel())) {
            throw new TransactionException(NOT_FOR_SALE);
        }
        if (sender.getLevel() < 20 || receiver.getLevel() < 20) {
            throw new TransactionException(LEVEL_TOO_LOW);
        }
        if (parcelService.findWithPlayerByReceiver(receiver.getId()).size() > 128
                || parcelService.findWithPlayerBySender(sender.getId()).size() > 128) {
            throw new TransactionException(LIMIT_REACHED);
        }

        Parcel parcel = createParcel(sender, receiver, item, gold, message);
        int goldBefore = sender.getGold();
        int goldAfter = goldBefore - 30;
        if (goldAfter < 0) {
            throw new TransactionException(INSUFFICIENT_GOLD);
        }

        if (parcel.getEParcelType() == EParcelType.Gold) {
            goldAfter = goldBefore - parcel.getGold();
            if (goldAfter < 0) {
                throw new TransactionException(INSUFFICIENT_GOLD);
            }
        }

        TransactionLogBuilder journal = new TransactionLogBuilder(TransactionType.PARCEL, accountId, playerId);
        journal.counterparty(receiver);
        journal.getTransaction().setAction(TransactionAction.SEND);
        journal.getTransaction().setParcelType(parcel.getEParcelType());
        journal.getTransaction().setRequestedGold(parcel.getGold());
        journal.pocket(InventoryItemSnapshot.from(item), null, null, null);
        journal.amount(PriceType.GOLD, goldBefore, goldAfter);

        sender.setGold(goldAfter);
        parcel = parcelService.save(parcel);
        playerPocketService.remove(item.getId());
        journal.getTransaction().setSourceId(parcel.getId());
        TransactionSummary summary = transactionLogService.save(journal.getTransaction());

        return new ParcelTransactionResult(parcel, item, goldAfter, participants.account().getAp(),
                summary);
    }

    @Override
    @Transactional
    public ParcelTransactionResult accept(Long accountId, Long playerId, Long parcelId) {
        Parcel parcel = findParcel(parcelId);
        requirePlayer(parcel.getReceiver(), playerId);

        TransactionParticipants.Participants participants = transactionParticipants.lock(accountId, playerId, parcel.getSender());
        Player receiver = participants.player();
        Player sender = participants.counterparty();
        if (receiver.getLevel() < 20) {
            throw new TransactionException(LEVEL_TOO_LOW);
        }

        TransactionLogBuilder journal = parcelJournal(accountId, playerId, sender, parcel, TransactionAction.ACCEPT);
        settleGold(parcel, sender, receiver, journal);
        PlayerPocket item = deliverItem(parcel, receiver, journal);
        parcelService.remove(parcel.getId());
        TransactionSummary summary = transactionLogService.save(journal.getTransaction());

        return new ParcelTransactionResult(parcel, item, receiver.getGold(), participants.account().getAp(),
                summary);
    }

    @Override
    @Transactional
    public ParcelTransactionResult deny(Long accountId, Long playerId, Long parcelId) {
        Parcel parcel = findParcel(parcelId);
        requirePlayer(parcel.getReceiver(), playerId);
        return returnParcel(accountId, playerId, parcel, parcel.getSender(), TransactionAction.DENY);
    }

    @Override
    @Transactional
    public ParcelTransactionResult cancel(Long accountId, Long playerId, Long parcelId) {
        Parcel parcel = findParcel(parcelId);
        requirePlayer(parcel.getSender(), playerId);
        return returnParcel(accountId, playerId, parcel, parcel.getReceiver(), TransactionAction.CANCEL);
    }

    private ParcelTransactionResult returnParcel(Long accountId, Long playerId, Parcel parcel,
                                                Player counterparty, TransactionAction action) {
        TransactionParticipants.Participants participants = transactionParticipants.lock(accountId, playerId, counterparty);
        Player sender = action == TransactionAction.CANCEL ? participants.player() : participants.counterparty();

        TransactionLogBuilder journal = parcelJournal(accountId, playerId, participants.counterparty(), parcel, action);
        PlayerPocket item = deliverItem(parcel, sender, journal);
        parcelService.remove(parcel.getId());
        TransactionSummary summary = transactionLogService.save(journal.getTransaction());

        return new ParcelTransactionResult(parcel, item, participants.player().getGold(), participants.account().getAp(),
                summary);
    }

    private Parcel findParcel(Long parcelId) {
        Parcel parcel = transactionParcelRepository.findById(parcelId)
                .orElseThrow(() -> new TransactionException(NOT_FOUND));
        if (parcel.getGold() == null || parcel.getGold() < 0 || parcel.getItemCount() == null || parcel.getItemCount() <= 0) {
            throw new TransactionException(INVALID_REQUEST);
        }
        return parcel;
    }

    private void requirePlayer(Player expected, Long playerId) {
        if (!expected.getId().equals(playerId)) {
            throw new TransactionException(FORBIDDEN);
        }
    }

    private TransactionLogBuilder parcelJournal(Long accountId, Long playerId, Player counterparty,
                                                Parcel parcel, TransactionAction action) {
        TransactionLogBuilder journal = new TransactionLogBuilder(TransactionType.PARCEL, accountId, playerId);
        journal.counterparty(counterparty);
        journal.getTransaction().setSourceId(parcel.getId());
        journal.getTransaction().setAction(action);
        journal.getTransaction().setParcelType(parcel.getEParcelType());
        journal.getTransaction().setRequestedGold(parcel.getGold());
        return journal;
    }

    private void settleGold(Parcel parcel, Player sender, Player receiver, TransactionLogBuilder journal) {
        int senderBefore = sender.getGold();
        int receiverBefore = receiver.getGold();
        int amount = parcel.getGold();

        if (parcel.getEParcelType() == EParcelType.CashOnDelivery) {
            if (receiverBefore < amount) {
                throw new TransactionException(INSUFFICIENT_GOLD);
            }
            receiver.setGold(Math.subtractExact(receiverBefore, amount));
            sender.setGold(Math.addExact(senderBefore, amount));
        } else if (parcel.getEParcelType() == EParcelType.Gold) {
            if (!sender.getId().equals(receiver.getId())) {
                sender.setGold(Math.subtractExact(senderBefore, amount));
            }
            receiver.setGold(Math.addExact(receiverBefore, amount));
        }

        journal.amount(PriceType.GOLD, receiver.getAccount().getId(), receiver.getId(), receiverBefore, receiver.getGold());
        if (!sender.getId().equals(receiver.getId())) {
            journal.amount(PriceType.GOLD, sender.getAccount().getId(), sender.getId(), senderBefore, sender.getGold());
        }
    }

    private PlayerPocket deliverItem(Parcel parcel, Player receiver, TransactionLogBuilder journal) {
        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(
                parcel.getItemIndex(), parcel.getCategory(), receiver.getPocket());
        InventoryItemSnapshot before = item == null ? null : InventoryItemSnapshot.from(item);

        if (item == null) {
            item = new PlayerPocket();
            item.setCategory(parcel.getCategory());
            item.setItemCount(parcel.getItemCount());
            item.setItemIndex(parcel.getItemIndex());
            item.setUseType(parcel.getUseType());
            item.setPocket(receiver.getPocket());
            item.setEnchantStr(parcel.getEnchantStr());
            item.setEnchantSta(parcel.getEnchantSta());
            item.setEnchantDex(parcel.getEnchantDex());
            item.setEnchantWil(parcel.getEnchantWil());
            item.setEnchantElement(parcel.getEnchantElement());
            item.setEnchantLevel(parcel.getEnchantLevel());
        } else {
            item.setItemCount(Math.addExact(item.getItemCount(), parcel.getItemCount()));
        }

        item = playerPocketService.save(item);
        journal.pocket(before, InventoryItemSnapshot.from(item), null, null);
        return item;
    }

    private Parcel createParcel(Player sender, Player receiver, PlayerPocket item, int gold, String message) {
        Parcel parcel = new Parcel();
        parcel.setReceiver(receiver);
        parcel.setSender(sender);
        parcel.setMessage(message);
        parcel.setGold(gold);
        parcel.setItemCount(item.getItemCount());
        parcel.setCategory(item.getCategory());
        parcel.setItemIndex(item.getItemIndex());
        parcel.setUseType(item.getUseType());
        parcel.setEnchantStr(item.getEnchantStr());
        parcel.setEnchantSta(item.getEnchantSta());
        parcel.setEnchantDex(item.getEnchantDex());
        parcel.setEnchantWil(item.getEnchantWil());
        parcel.setEnchantElement(item.getEnchantElement());
        parcel.setEnchantLevel(item.getEnchantLevel());
        parcel.setEParcelType(receiver.getId().equals(sender.getId()) ? EParcelType.Gold : EParcelType.CashOnDelivery);
        return parcel;
    }
}
