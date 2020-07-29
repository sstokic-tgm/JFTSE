package com.ft.emulator.server.game.core.service;

import com.ft.emulator.server.database.model.pocket.PlayerPocket;
import com.ft.emulator.server.database.model.pocket.Pocket;
import com.ft.emulator.server.database.repository.item.ItemEnchantRepository;
import com.ft.emulator.server.database.repository.item.ItemMaterialRepository;
import com.ft.emulator.server.database.repository.item.ProductRepository;
import com.ft.emulator.server.database.repository.pocket.PlayerPocketRepository;
import com.ft.emulator.server.game.core.item.EItemCategory;
import com.ft.emulator.server.game.core.item.EItemUseType;
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
public class PlayerPocketService {
    private final ItemMaterialRepository itemMaterialRepository;
    private final ItemEnchantRepository itemEnchantRepository;
    private final ProductRepository productRepository;
    private final PlayerPocketRepository playerPocketRepository;

    private final PocketService pocketService;

    public PlayerPocket getItemAsPocket(Long itemPocketId, Pocket pocket) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findByIdAndPocket(itemPocketId, pocket);
        return playerPocket.orElse(null);
    }

    public PlayerPocket getItemAsPocketByItemIndex(Integer itemIndex, Pocket pocket) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findByItemIndexAndPocket(itemIndex, pocket);
        return playerPocket.orElse(null);
    }

    public PlayerPocket findById(Long id) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findById(id);
        return playerPocket.orElse(null);
    }

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
            sellPrice = (int) Math.ceil((double) sellPriceResult.get(0) / 2);
        }
        return sellPrice;
    }

    public PlayerPocket save(PlayerPocket playerPocket) {
        return playerPocketRepository.save(playerPocket);
    }

    public void remove(Long playerPocketId) {
        playerPocketRepository.deleteById(playerPocketId);
    }
}
