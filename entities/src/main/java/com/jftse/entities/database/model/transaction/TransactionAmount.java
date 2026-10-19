package com.jftse.entities.database.model.transaction;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "TransactionAmount", uniqueConstraints = @UniqueConstraint(name = "uq_transaction_amount_line", columnNames = {"transaction_id", "line_no"}))
public class TransactionAmount extends AbstractIdBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    private TransactionLog transaction;

    @Column(name = "line_no", nullable = false, updatable = false)
    private Integer lineNo;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    @Column(name = "player_id", nullable = false, updatable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private PriceType currency;

    @Column(name = "balance_before", nullable = false, updatable = false)
    private Long balanceBefore;

    @Column(name = "balance_after", nullable = false, updatable = false)
    private Long balanceAfter;

    @Column(nullable = false, updatable = false)
    private Long delta;
}
