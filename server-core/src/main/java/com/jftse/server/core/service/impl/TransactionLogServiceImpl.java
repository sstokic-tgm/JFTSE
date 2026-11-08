package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.transaction.*;
import com.jftse.entities.database.repository.transaction.TransactionItemRepository;
import com.jftse.entities.database.repository.transaction.TransactionLogRepository;
import com.jftse.server.core.service.TransactionLogService;
import com.jftse.server.core.service.transaction.TransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionLogServiceImpl implements TransactionLogService {
    private final TransactionLogRepository transactionLogRepository;
    private final TransactionItemRepository transactionItemRepository;

    @Override
    @Transactional
    public TransactionSummary save(TransactionLog transaction) {
        TransactionLog parcelSend = findParcelSend(transaction);
        transaction.setComponentOf(parcelSend);

        Map<Long, TransactionItem> latestItems = new HashMap<>();
        for (TransactionItem item : transaction.getItems()) {
            linkPreviousState(item, latestItems);
            if (parcelSend != null) {
                linkParcelSource(item, parcelSend);
            }
        }

        transactionLogRepository.saveAndFlush(transaction);
        return TransactionSummary.from(transaction);
    }

    private TransactionLog findParcelSend(TransactionLog transaction) {
        if (transaction.getType() != TransactionType.PARCEL || transaction.getAction() == TransactionAction.SEND) {
            return null;
        }

        // Parcels already present before tracking began have no historical send record.
        return transactionLogRepository.findFirstByTypeAndSourceIdAndActionOrderByIdAsc(
                TransactionType.PARCEL, transaction.getSourceId(), TransactionAction.SEND).orElse(null);
    }

    private void linkPreviousState(TransactionItem item, Map<Long, TransactionItem> latestItems) {
        if (item.getPocketId() == null || item.getReferenceId() == null) {
            return;
        }

        if (item.getQuantityBefore() > 0) {
            TransactionItem previous = latestItems.get(item.getReferenceId());
            if (previous == null) {
                previous = transactionItemRepository.findFirstByPocketIdAndReferenceIdOrderByIdDesc(
                        item.getPocketId(), item.getReferenceId()).orElse(null);
            }
            if (previous != null) {
                addComponent(item, previous, TransactionComponentType.PREVIOUS_STATE, item.getQuantityBefore());
            }
        }
        latestItems.put(item.getReferenceId(), item);
    }

    private void linkParcelSource(TransactionItem item, TransactionLog parcelSend) {
        for (TransactionItem source : parcelSend.getItems()) {
            if (source.getPocketId() != null && source.getCategory().equals(item.getCategory())
                    && source.getItemIndex().equals(item.getItemIndex())) {
                addComponent(item, source, TransactionComponentType.SOURCE, source.getQuantityBefore());
            }
        }
    }

    private void addComponent(TransactionItem item, TransactionItem parent, TransactionComponentType type, long quantity) {
        TransactionItemComponent component = new TransactionItemComponent();
        component.setItem(item);
        component.setComponentOf(parent);
        component.setType(type);
        component.setQuantity(quantity);
        item.getComponents().add(component);
    }
}
