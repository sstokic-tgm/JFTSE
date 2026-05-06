package com.jftse.entities.database.model.auctionhouse;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.item.Product;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class AuctionHouseInventory extends AbstractBaseModel {
    private Long playerId;
    private Long pocketId;
    private Long playerPocketId;

    private Integer itemIndex;
    private String itemCategory;
    private String itemName;
    private String itemUseType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    private Integer price;
    private Integer amount;

    private PriceType priceType = PriceType.GOLD;

    private TradeStatus tradeStatus;
}
