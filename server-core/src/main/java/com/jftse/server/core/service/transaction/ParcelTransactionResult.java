package com.jftse.server.core.service.transaction;

import com.jftse.entities.database.model.messenger.Parcel;
import com.jftse.entities.database.model.pocket.PlayerPocket;

public record ParcelTransactionResult(Parcel parcel, PlayerPocket item, int gold, int ap,
                                      TransactionSummary transaction) {
}
