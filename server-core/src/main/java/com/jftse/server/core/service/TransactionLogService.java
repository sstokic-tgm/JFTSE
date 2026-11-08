package com.jftse.server.core.service;

import com.jftse.entities.database.model.transaction.TransactionLog;
import com.jftse.server.core.service.transaction.TransactionSummary;

public interface TransactionLogService {
    TransactionSummary save(TransactionLog transaction);
}
