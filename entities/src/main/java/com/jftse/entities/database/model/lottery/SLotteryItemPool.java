package com.jftse.entities.database.model.lottery;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import com.jftse.entities.database.model.KStatus;
import com.jftse.entities.database.model.item.Product;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(
        name = "S_LotteryItemPool",
        indexes = {
                @Index(name = "idx_lip_gachaindex", columnList = "gacha_index"),
                @Index(name = "idx_lip_productid", columnList = "product_id"),
                @Index(name = "idx_lip_rarityid", columnList = "rarity_id"),
                @Index(name = "idx_lip_gachaindex_statusid", columnList = "gacha_index, status_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lip_gacha_product", columnNames = {"gacha_index", "product_id"})
        }
)
public class SLotteryItemPool extends AbstractIdBaseModel {

    @Column(name = "gacha_index", nullable = false)
    private int gachaIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rarity_id", referencedColumnName = "id")
    private SRarity rarity;

    private Integer qtyMin;
    private Integer qtyMax;
    private Integer qty;
    private Double weight;

    private Integer gachaTokens;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private KStatus status;
}
