package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.account.Account;
import com.jftse.emulator.server.database.model.item.ItemChar;
import com.jftse.emulator.server.database.model.item.Product;
import com.jftse.emulator.server.database.model.player.*;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.database.repository.item.*;
import com.jftse.emulator.server.game.core.item.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ProductService {

    private final ProductRepository productRepository;
    private final ItemPartRepository itemPartRepository;
    private final ItemHouseDecoRepository itemHouseDecoRepository;
    private final ItemRecipeRepository itemRecipeRepository;
    private final ItemEnchantRepository itemEnchantRepository;

    private final PlayerService playerService;
    private final ClothEquipmentService clothEquipmentService;
    private final QuickSlotEquipmentService quickSlotEquipmentService;
    private final SpecialSlotEquipmentService specialSlotEquipmentService;
    private final ToolSlotEquipmentService toolSlotEquipmentService;
    private final CardSlotEquipmentService cardSlotEquipmentService;
    private final PocketService pocketService;
    private final PlayerStatisticService playerStatisticService;
    private final ItemCharService itemCharService;

    public Product findProductByProductItemIndex(int productItemIndex) {
        return this.productRepository.findProductByProductIndex(productItemIndex);
    }

    public Map<Product, Byte> findProductsByItemList(Map<Integer, Byte> itemList) {
        List<Integer> productIndexList = new ArrayList<>(itemList.keySet());

        List<Product> productList = productRepository.findProductsByProductIndexIn(productIndexList);

        Map<Product, Byte> result = new HashMap<>();
        productList.forEach(p -> {
            if (p.getEnabled())
                result.put(p, itemList.get(p.getProductIndex()));
        });

        return result;
    }

    public List<Product> findProductsByItemList(List<Integer> itemList) {
        return productRepository.findProductsByProductIndexIn(itemList);
    }

    public int getProductListSize(byte category, byte part, byte player) {
        long productListSize = 0;

        switch (EItemCategory.valueOf(category)) {

        case PARTS: {
            List<Integer> itemIndexList = part == EItemPart.SET.getValue() ?
                itemPartRepository.findItemIndexListByForPlayer(EItemChar.getNameByValue(player)) :
                itemPartRepository.findItemIndexListByForPlayerAndPartIn(EItemChar.getNameByValue(player), EItemSubPart.getNamesByValue(part));
            productListSize = part == EItemPart.SET.getValue() ?
                productRepository.countProductsByCategoryAndEnabledAndItem0InAndItem1Not(EItemCategory.getNameByValue(category), true, itemIndexList, 0) :
                productRepository.countProductsByCategoryAndEnabledAndItem0InAndItem1Is(EItemCategory.getNameByValue(category), true, itemIndexList, 0);
        } break;
        case HOUSE_DECO:
            productListSize = productRepository.countProductsByCategoryAndEnabledAndItem0In(EItemCategory.getNameByValue(category), true, itemHouseDecoRepository.findItemIndexListByKind(EItemHouseDeco.getNameByValue(part)));
            break;

        case RECIPE: {
            List<Integer> itemIndexList = part == EItemRecipe.CHAR_ITEM.getValue() ?
                itemRecipeRepository.findItemIndexListByKindAndForPlayer(EItemRecipe.getNameByValue(part), EItemChar.getNameByValue(player)) :
                itemRecipeRepository.findItemIndexListByKind(EItemRecipe.getNameByValue(part));
            productListSize = productRepository.countProductsByCategoryAndEnabledAndItem0In(EItemCategory.getNameByValue(category), true, itemIndexList);
        } break;
        case LOTTERY:
            productListSize = productRepository.countProductsByCategoryAndEnabledAndPriceType(EItemCategory.getNameByValue(category), true, part == 0 ? "MINT" : "GOLD");
            break;

        case ENCHANT:
            productListSize = productRepository.countProductsByCategoryAndEnabledAndItem0In(EItemCategory.getNameByValue(category), true, itemEnchantRepository.getItemIndexListByKind(EItemEnchant.getNameByValue(part)));
            break;

        default:
            productListSize = productRepository.countProductsByCategoryAndEnabled(EItemCategory.getNameByValue(category), true);
            break;
        }

        return (int) productListSize;
    }

    public List<Product> getProductList(byte category, byte part, byte player, int page) {
        List<Product> productList;

        Pageable pageWithSixElements = PageRequest.of(page == 1 ? 0 : (page - 1), 6, Sort.by("productIndex"));

        switch (EItemCategory.valueOf(category)) {

        case PARTS: {
            List<Integer> itemIndexList = part == EItemPart.SET.getValue() ?
                itemPartRepository.findItemIndexListByForPlayer(EItemChar.getNameByValue(player)) :
                itemPartRepository.findItemIndexListByForPlayerAndPartIn(EItemChar.getNameByValue(player), EItemSubPart.getNamesByValue(part));
            productList = part == EItemPart.SET.getValue() ?
                productRepository.findAllByCategoryAndEnabledAndItem0InAndItem1Not(EItemCategory.getNameByValue(category), true, itemIndexList, 0, pageWithSixElements) :
                productRepository.findAllByCategoryAndEnabledAndItem0InAndItem1Is(EItemCategory.getNameByValue(category), true, itemIndexList, 0, pageWithSixElements);
        } break;
        case HOUSE_DECO:
            productList = productRepository.findAllByCategoryAndEnabledAndItem0In(EItemCategory.getNameByValue(category), true, itemHouseDecoRepository.findItemIndexListByKind(EItemHouseDeco.getNameByValue(part)), pageWithSixElements);
            break;

        case RECIPE: {
            List<Integer> itemIndexList = part == EItemRecipe.CHAR_ITEM.getValue() ?
                itemRecipeRepository.findItemIndexListByKindAndForPlayer(EItemRecipe.getNameByValue(part), EItemChar.getNameByValue(player)) :
                itemRecipeRepository.findItemIndexListByKind(EItemRecipe.getNameByValue(part));
            productList = productRepository.findAllByCategoryAndEnabledAndItem0In(EItemCategory.getNameByValue(category), true, itemIndexList, pageWithSixElements);
        } break;
        case LOTTERY:
            productList = productRepository.findAllByCategoryAndEnabledAndPriceType(EItemCategory.getNameByValue(category), true, part == 0 ? "MINT" : "GOLD", pageWithSixElements);
            break;

        case ENCHANT:
            productList = productRepository.findAllByCategoryAndEnabledAndItem0In(EItemCategory.getNameByValue(category), true, itemEnchantRepository.getItemIndexListByKind(EItemEnchant.getNameByValue(part)), pageWithSixElements);
            break;

        default:
            productList = productRepository.findAllByCategoryAndEnabled(EItemCategory.getNameByValue(category), true, pageWithSixElements);
            break;
        }

        return productList;
    }

    public Player createNewPlayer(Account account, byte forPlayer) {
        ItemChar itemChar = itemCharService.findByPlayerType(forPlayer);

        Player player = new Player();
        player.setAccount(account);
        player.setPlayerType(forPlayer);
        player.setFirstPlayer(false);
        player.setStrength(itemChar.getStrength());
        player.setStamina(itemChar.getStamina());
        player.setDexterity(itemChar.getDexterity());
        player.setWillpower(itemChar.getWillpower());
        player.setStatusPoints((byte) 5);

        ClothEquipment clothEquipment = new ClothEquipment();
        clothEquipment = clothEquipmentService.save(clothEquipment);

        QuickSlotEquipment quickSlotEquipment = new QuickSlotEquipment();
        quickSlotEquipment = quickSlotEquipmentService.save(quickSlotEquipment);

        SpecialSlotEquipment specialSlotEquipment = new SpecialSlotEquipment();
        specialSlotEquipment = specialSlotEquipmentService.save(specialSlotEquipment);

        ToolSlotEquipment toolSlotEquipment = new ToolSlotEquipment();
        toolSlotEquipment = toolSlotEquipmentService.save(toolSlotEquipment);

        CardSlotEquipment cardSlotEquipment = new CardSlotEquipment();
        cardSlotEquipment = cardSlotEquipmentService.save(cardSlotEquipment);

        player.setClothEquipment(clothEquipment);
        player.setQuickSlotEquipment(quickSlotEquipment);
        player.setSpecialSlotEquipment(specialSlotEquipment);
        player.setToolSlotEquipment(toolSlotEquipment);
        player.setCardSlotEquipment(cardSlotEquipment);

        Pocket pocket = new Pocket();
        pocket = pocketService.save(pocket);
        player.setPocket(pocket);

        PlayerStatistic playerStatistic = new PlayerStatistic();
        playerStatistic = playerStatisticService.save(playerStatistic);
        player.setPlayerStatistic(playerStatistic);

        return playerService.save(player);
    }
}
