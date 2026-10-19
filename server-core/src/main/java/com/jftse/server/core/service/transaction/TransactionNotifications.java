package com.jftse.server.core.service.transaction;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TransactionNotifications {
    private TransactionNotifications() {
    }

    public static void afterCommit(Runnable notification) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            notification.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notification.run();
            }
        });
    }
}
