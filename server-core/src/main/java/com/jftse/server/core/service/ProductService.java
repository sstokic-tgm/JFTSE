package com.jftse.server.core.service;

import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.player.Player;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Product findProductByProductItemIndex(int productItemIndex);

    Product findProductByItemAndCategoryAndEnabledIsTrue(int itemIndex, String category);

    Product findProductByItemAndCategory(int itemIndex, String category);

    List<Integer> findAllProductIndexesByCategoryAndItemIndexList(String category, List<Integer> itemIndexList);

    Map<Product, Byte> findProductsByItemList(Map<Integer, Byte> itemList);

    Product findProductByName(String name, String category);

    List<Product> findProductsByItemList(List<Integer> itemList);

    int getProductListSize(byte category, byte part, byte player);

    List<Product> getProductList(byte category, byte part, byte player, int page);

    Player createNewPlayer(Account account, byte forPlayer);
}
