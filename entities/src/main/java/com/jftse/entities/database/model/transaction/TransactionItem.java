package com.jftse.entities.database.model.transaction;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "TransactionItem", uniqueConstraints = @UniqueConstraint(name = "uq_transaction_item_line", columnNames = {"transaction_id", "line_no"}), indexes = {
        @Index(name = "idx_transaction_item_trace", columnList = "category,item_index,transaction_id"),
        @Index(name = "idx_transaction_item_state", columnList = "pocket_id,reference_id,id")
})
public class TransactionItem extends AbstractIdBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    private TransactionLog transaction;

    @Column(name = "line_no", nullable = false, updatable = false)
    private Integer lineNo;

    @Column(name = "reference_id", updatable = false)
    private Long referenceId;

    @Column(name = "product_index", updatable = false)
    private Integer productIndex;

    @Column(name = "product_option", updatable = false)
    private Byte productOption;

    @Column(updatable = false, length = 32)
    private String category;

    @Column(name = "item_index", updatable = false)
    private Integer itemIndex;

    @Column(name = "pocket_id", updatable = false)
    private Long pocketId;

    @Column(name = "quantity_before", nullable = false, updatable = false)
    private Long quantityBefore;

    @Column(name = "quantity_after", nullable = false, updatable = false)
    private Long quantityAfter;

    @Column(name = "use_type", updatable = false, length = 16)
    private String useType;

    @Column(name = "item_created_before", updatable = false)
    private Date itemCreatedBefore;

    @Column(name = "item_created_after", updatable = false)
    private Date itemCreatedAfter;

    @Column(name = "enchant_str", updatable = false)
    private Integer enchantStr;

    @Column(name = "enchant_sta", updatable = false)
    private Integer enchantSta;

    @Column(name = "enchant_dex", updatable = false)
    private Integer enchantDex;

    @Column(name = "enchant_wil", updatable = false)
    private Integer enchantWil;

    @Column(name = "enchant_element", updatable = false)
    private Integer enchantElement;

    @Column(name = "enchant_level", updatable = false)
    private Integer enchantLevel;

    @OneToMany(mappedBy = "item", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<TransactionItemComponent> components = new ArrayList<>();
}
