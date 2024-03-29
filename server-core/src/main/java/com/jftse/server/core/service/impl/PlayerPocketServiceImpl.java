package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.item.ItemEnchantRepository;
import com.jftse.entities.database.repository.item.ItemMaterialRepository;
import com.jftse.entities.database.repository.item.ProductRepository;
import com.jftse.entities.database.repository.pocket.PlayerPocketRepository;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PlayerPocketServiceImpl implements PlayerPocketService {
    private final ItemMaterialRepository itemMaterialRepository;
    private final ItemEnchantRepository itemEnchantRepository;
    private final ProductRepository productRepository;
    private final PlayerPocketRepository playerPocketRepository;

    private final PocketService pocketService;

    @Override
    public PlayerPocket getItemAsPocket(Long itemPocketId, Pocket pocket) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findByIdAndPocket(itemPocketId, pocket);
        return playerPocket.orElse(null);
    }

    @Override
    public PlayerPocket getItemAsPocketByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Pocket pocket) {
        List<PlayerPocket> playerPocketList = playerPocketRepository.findAllByItemIndexAndCategoryAndPocket(itemIndex, category, pocket);
        return getPlayerPocketAndHandleDuplicates(playerPocketList);
    }

    @Override
    @Deprecated(forRemoval = true)
    public PlayerPocket getItemAsPocketByItemIndexAndPocket(Integer itemIndex, Pocket pocket) {
        List<PlayerPocket> playerPocketList = playerPocketRepository.findAllByItemIndexAndPocket(itemIndex, pocket);
        return getPlayerPocketAndHandleDuplicates(playerPocketList);
    }

    private PlayerPocket getPlayerPocketAndHandleDuplicates(List<PlayerPocket> playerPocketList) {
        PlayerPocket playerPocket = null;
        if (playerPocketList.size() >= 1) {
            playerPocket = playerPocketList.get(0);

            for (int i = 1; i < playerPocketList.size(); i++) {
                if (playerPocketList.get(i).getCategory().equals(EItemCategory.PARTS.getName()))
                    this.remove(playerPocketList.get(i).getId());
            }
        }
        return playerPocket;
    }

    @Override
    public PlayerPocket findById(Long id) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findById(id);
        return playerPocket.orElse(null);
    }

    @Override
    public List<PlayerPocket> getPlayerPocketItems(Pocket pocket) {
        List<PlayerPocket> playerPocketItems = playerPocketRepository.findAllByPocket(pocket);
        List<PlayerPocket> result = new ArrayList<>();

        for (PlayerPocket item : playerPocketItems) {
            if (((item.getCreated().getTime() * 10000) - (new Date().getTime() * 10000) <= 0) && item.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                remove(item.getId());
                pocket = pocketService.decrementPocketBelongings(pocket);
                continue;
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public int getSellPrice(PlayerPocket playerPocket) {
        int itemCount = playerPocket.getItemCount();

        int sellPrice = 0;

        if (playerPocket.getCategory().equals(EItemCategory.MATERIAL.getName())) {
            List<Integer> sellPriceResult = itemMaterialRepository.getItemSellPriceByItemIndex(playerPocket.getItemIndex());
            sellPrice = sellPriceResult.get(0) * itemCount;
        }
        else if (playerPocket.getCategory().equals(EItemCategory.ENCHANT.getName())) {
            List<Integer> sellPriceResult = itemEnchantRepository.getItemSellPriceByItemIndex(playerPocket.getItemIndex());
            sellPrice = sellPriceResult.get(0) * itemCount;
        }
        else { // everything else buy price / 2
            List<Integer> sellPriceResult = productRepository.getItemSellPriceByItemIndexAndCategory(playerPocket.getItemIndex(), playerPocket.getCategory());
            int buyPrice = sellPriceResult.get(0);
            if (buyPrice <= 0)
                buyPrice = 1;

            sellPrice = (int) Math.ceil((double) (buyPrice / 2) * (1 + (itemCount / (double) (buyPrice / 2))));
        }
        return sellPrice;
    }

    @Override
    public PlayerPocket decrementPocketItemCount(PlayerPocket playerPocket) {
        Optional<PlayerPocket> tmpPocket = playerPocketRepository.findById(playerPocket.getId());
        if (tmpPocket.isPresent()) {
            playerPocket = tmpPocket.get();

            playerPocket.setItemCount(playerPocket.getItemCount() - 1);
            return save(playerPocket);
        }
        else {
            return playerPocket;
        }
    }

    @Override
    public PlayerPocket save(PlayerPocket playerPocket) {
        return playerPocketRepository.save(playerPocket);
    }

    @Override
    public void remove(Long playerPocketId) {
        playerPocketRepository.deleteById(playerPocketId);
    }
}
