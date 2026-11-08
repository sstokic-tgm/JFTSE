package com.jftse.entities.database.model.transaction;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "TransactionItemComponent", uniqueConstraints = {
        @UniqueConstraint(name = "uq_transaction_component", columnNames = {"item_id", "component_of_id", "type"})
}, indexes = {
        @Index(name = "idx_transaction_component_parent", columnList = "component_of_id,item_id")
})
public class TransactionItemComponent extends AbstractIdBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, updatable = false)
    private TransactionItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "component_of_id", nullable = false, updatable = false)
    private TransactionItem componentOf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 24)
    private TransactionComponentType type;

    @Column(nullable = false, updatable = false)
    private Long quantity;
}
