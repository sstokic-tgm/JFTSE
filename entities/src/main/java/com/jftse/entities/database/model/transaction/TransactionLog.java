package com.jftse.entities.database.model.transaction;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.messenger.EParcelType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "TransactionLog", uniqueConstraints = @UniqueConstraint(name = "uq_transaction_operation", columnNames = "operation_id"), indexes = {
        @Index(name = "idx_transaction_account", columnList = "account_id,created,id"),
        @Index(name = "idx_transaction_player", columnList = "player_id,created,id"),
        @Index(name = "idx_transaction_type", columnList = "type,created,id"),
        @Index(name = "idx_transaction_source", columnList = "type,source_id,action,id"),
        @Index(name = "idx_transaction_counterparty", columnList = "counterparty_account_id,created,id"),
        @Index(name = "idx_transaction_parent", columnList = "component_of_id,id")
})
public class TransactionLog extends AbstractBaseModel {
    @Column(name = "operation_id", nullable = false, updatable = false, length = 36)
    private String operationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 24)
    private TransactionType type;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    @Column(name = "player_id", nullable = false, updatable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, length = 16)
    private TransactionAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_of_id", updatable = false)
    private TransactionLog componentOf;

    @Column(name = "source_id", updatable = false)
    private Long sourceId;

    @Column(name = "counterparty_account_id", updatable = false)
    private Long counterpartyAccountId;

    @Column(name = "counterparty_player_id", updatable = false)
    private Long counterpartyPlayerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "parcel_type", updatable = false, length = 24)
    private EParcelType parcelType;

    @Column(name = "requested_gold", updatable = false)
    private Integer requestedGold;

    @Column(name = "schema_version", nullable = false, updatable = false)
    private Integer schemaVersion = 2;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.PERSIST)
    @OrderBy("lineNo ASC")
    private List<TransactionAmount> amounts = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.PERSIST)
    @OrderBy("lineNo ASC")
    private List<TransactionItem> items = new ArrayList<>();
}
