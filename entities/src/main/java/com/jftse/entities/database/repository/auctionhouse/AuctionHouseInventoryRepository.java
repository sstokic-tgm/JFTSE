package com.jftse.entities.database.repository.auctionhouse;

import com.jftse.entities.database.model.auctionhouse.AuctionHouseInventory;
import com.jftse.entities.database.model.auctionhouse.TradeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuctionHouseInventoryRepository extends JpaRepository<AuctionHouseInventory, Long> {
    List<AuctionHouseInventory> findAllByItemName(String itemName, Pageable pageable);
    List<AuctionHouseInventory> findAllByItemCategory(String itemName, Pageable pageable);
    List<AuctionHouseInventory> findAllByPlayerId(Long playerId, Pageable pageable);
    List<AuctionHouseInventory> findAllByPlayerIdAndTradeStatus(Long playerId, TradeStatus tradeStatus, Pageable pageable);
}
