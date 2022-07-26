package com.jftse.emulator.server.core.life.auctionhouse;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.PocketService;
import com.jftse.entities.database.model.auctionhouse.AuctionHouseInventory;
import com.jftse.entities.database.model.auctionhouse.PriceType;
import com.jftse.entities.database.model.auctionhouse.TradeStatus;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.auctionhouse.AuctionHouseInventoryRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Service
@Getter
@Setter
@Log4j2
public class AuctionHouse {
    private static AuctionHouse instance;

    private final AuctionHouseInventoryRepository ahInventoryRepository;
    private final PocketService pocketService;
    private final PlayerPocketService playerPocketService;
    private final PlayerService playerService;

    @Autowired
    public AuctionHouse(AuctionHouseInventoryRepository auctionHouseInventoryRepository, PocketService pocketService, PlayerPocketService playerPocketService, PlayerService playerService) {
        this.ahInventoryRepository = auctionHouseInventoryRepository;
        this.pocketService = pocketService;
        this.playerPocketService = playerPocketService;
        this.playerService = playerService;
    }

    @PostConstruct
    public void init() {
        instance = this;

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static AuctionHouse getInstance() {
        return instance;
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuctionHouseInventory placeOffer(Player player, AuctionHouseInventory offer) {
        offer.setPlayerId(player.getId());
        offer.setPocketId(player.getPocket().getId());
        offer.setTradeStatus(TradeStatus.IN_QUEUE);
        offer.setPriceType(PriceType.GOLD);

        offer = ahInventoryRepository.save(offer);
        return offer;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuctionHouseInventory cancelOffer(Player player, AuctionHouseInventory offer) throws ValidationException {
        offer.setTradeStatus(TradeStatus.CANCELED);
        offer = ahInventoryRepository.save(offer);

        Long pocketId = offer.getPocketId();
        Pocket pocket = pocketService.findById(pocketId);
        if (pocket == null)
            throw new ValidationException("Pocket was not found with the id: " + pocketId);

        PlayerPocket playerPocket = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(offer.getItemIndex(), offer.getItemCategory(), pocket);
        boolean existingItem = false;
        if (playerPocket != null && !playerPocket.getUseType().equals("N/A")) {
            existingItem = true;
        } else {
            playerPocket = new PlayerPocket();
        }

        playerPocket.setPocket(pocket);
        playerPocket.setItemCount(existingItem ? playerPocket.getItemCount() + offer.getAmount() : offer.getAmount());
        playerPocket.setCategory(offer.getItemCategory());
        playerPocket.setUseType(offer.getItemUseType());
        playerPocket.setItemIndex(offer.getItemIndex());
        playerPocketService.save(playerPocket);

        if (!existingItem)
            pocketService.incrementPocketBelongings(pocket);

        return offer;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuctionHouseInventory buyOffer(Player player, AuctionHouseInventory offer) {
        offer.setTradeStatus(TradeStatus.SOLD);
        offer = ahInventoryRepository.save(offer);

        Player sellingPlayer = playerService.findById(offer.getPlayerId());
        playerService.updateMoney(sellingPlayer, offer.getPrice());

        return offer;
    }
}
