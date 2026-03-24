package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.SRelationships;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.item.ItemEnchantRepository;
import com.jftse.entities.database.repository.item.ItemMaterialRepository;
import com.jftse.entities.database.repository.item.ProductRepository;
import com.jftse.entities.database.repository.pocket.PlayerPocketRepository;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.PocketService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerPocketServiceImpl implements PlayerPocketService {
    private final Logger log = LogManager.getLogger(PlayerPocketServiceImpl.class);

    private final ItemMaterialRepository itemMaterialRepository;
    private final ItemEnchantRepository itemEnchantRepository;
    private final ProductRepository productRepository;
    private final PlayerPocketRepository playerPocketRepository;

    private final PocketService pocketService;

    private final JdbcUtil jdbcUtil;

    @Override
    @Transactional(readOnly = true)
    public PlayerPocket getItemAsPocket(Long itemPocketId, Pocket pocket) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findByIdAndPocket(itemPocketId, pocket);
        return playerPocket.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerPocket getItemAsPocket(Long itemPocketId, Long pocketId) {
        return playerPocketRepository.findByIdAndPocketId(itemPocketId, pocketId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerPocket> getItemsAsPocket(List<Long> itemPocketIds, Pocket pocket) {
        return playerPocketRepository.findAllByPocketAndIdIn(pocket, itemPocketIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerPocket> getItemsAsPocketByItemIndex(List<Integer> itemIndices, Pocket pocket) {
        return playerPocketRepository.findAllByPocketAndItemIndexIn(pocket, itemIndices);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerPocket getItemAsPocketByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Pocket pocket) {
        List<PlayerPocket> playerPocketList = playerPocketRepository.findAllByItemIndexAndCategoryAndPocket(itemIndex, category, pocket);
        return getPlayerPocketAndHandleDuplicates(playerPocketList);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerPocket getItemAsPocketByItemIndexAndCategoryAndPocket(Integer itemIndex, String category, Long pocketId) {
        List<PlayerPocket> playerPocketList = playerPocketRepository.findAllByItemIndexAndCategoryAndPocketId(itemIndex, category, pocketId);
        return getPlayerPocketAndHandleDuplicates(playerPocketList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerPocket> getItemsAsPocketByItemIndexListAndCategoryAndPocket(List<Integer> itemIndexList, String category, Pocket pocket) {
        return playerPocketRepository.findAllByPocketAndCategoryAndItemIndexIn(pocket, category, itemIndexList);
    }

    private PlayerPocket getPlayerPocketAndHandleDuplicates(List<PlayerPocket> playerPocketList) {
        return playerPocketList.isEmpty() ? null : playerPocketList.getFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerPocket findById(Long id) {
        Optional<PlayerPocket> playerPocket = playerPocketRepository.findById(id);
        return playerPocket.orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<PlayerPocket> getPlayerPocketItems(Pocket pocket) {
        List<PlayerPocket> playerPocketItems = playerPocketRepository.findAllByPocket(pocket);
        return filterExpiredItems(playerPocketItems, pocket);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<PlayerPocket> getPlayerPocketItemsByCategory(Pocket pocket, String category) {
        List<PlayerPocket> playerPocketItems = playerPocketRepository.findAllByPocketAndCategory(pocket, category);
        return filterExpiredItems(playerPocketItems, pocket);
    }

    private List<PlayerPocket> filterExpiredItems(List<PlayerPocket> playerPocketItems, Pocket pocket) {
        List<PlayerPocket> result = new ArrayList<>();

        long now = new Date().getTime();
        for (PlayerPocket item : playerPocketItems) {
            if (((item.getCreated().getTime() * 10000) - (now * 10000) <= 0) && item.getUseType().equalsIgnoreCase(EItemUseType.TIME.getName())) {
                playerPocketRepository.deleteById(item.getId());
                pocket = pocketService.decrementPocketBelongings(pocket);
                continue;
            }
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public int getSellPrice(PlayerPocket playerPocket) {
        int itemCount = playerPocket.getItemCount();

        int sellPrice = 0;

        if (playerPocket.getCategory().equals(EItemCategory.MATERIAL.getName())) {
            List<Integer> sellPriceResult = itemMaterialRepository.getItemSellPriceByItemIndex(playerPocket.getItemIndex());
            sellPrice = sellPriceResult.getFirst() * itemCount;
        }
        else if (playerPocket.getCategory().equals(EItemCategory.ENCHANT.getName())) {
            List<Integer> sellPriceResult = itemEnchantRepository.getItemSellPriceByItemIndex(playerPocket.getItemIndex());
            sellPrice = sellPriceResult.getFirst() * itemCount;
        }
        else { // everything else buy price / 2
            List<Product> products = productRepository.findProductsByItem0AndCategory(playerPocket.getItemIndex(), playerPocket.getCategory());
            if (!products.isEmpty()) {
                List<SRelationships> relationships = new ArrayList<>();
                jdbcUtil.execute(em -> {
                    try {
                        TypedQuery<SRelationships> query = em.createQuery("SELECT sr FROM SRelationships sr " +
                                "WHERE sr.id_f = :id_f AND sr.status.id = 1 AND " +
                                "sr.relationship.id = 8 AND sr.role.id = 4", SRelationships.class);
                        query.setParameter("id_f", products.getFirst().getProductIndex().longValue());
                        relationships.addAll(query.getResultList());
                    } catch (Exception e) {
                        log.error("Error while getting sell price: {}", e.getMessage());
                    }
                });

                if (!relationships.isEmpty()) {
                    SRelationships relationship = relationships.getFirst();
                    sellPrice = relationship.getId_t().intValue() * itemCount;
                } else { // default to buy price / 2
                    int buyPrice = products.stream().filter(p -> p.getPrice0() > 0).findFirst().map(Product::getPrice0).orElse(1);

                    sellPrice = (int) Math.ceil((double) (buyPrice / 2) * (1 + (itemCount / (double) (buyPrice / 2))));
                }
            }
        }
        return sellPrice;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerPocket save(PlayerPocket playerPocket) {
        return playerPocketRepository.save(playerPocket);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<PlayerPocket> saveAll(List<PlayerPocket> playerPockets) {
        return playerPocketRepository.saveAll(playerPockets);
    }

    @Override
    @Transactional
    public void remove(Long playerPocketId) {
        playerPocketRepository.deleteById(playerPocketId);
    }
}
