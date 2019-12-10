package com.ft.emulator.server.game.shop;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.game.item.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopImpl extends Service {

    private GenericModelDao<Product> productDao;

    public ShopImpl(EntityManagerFactory entityManagerFactory) {

        super(entityManagerFactory);

        productDao = new GenericModelDao<>(entityManagerFactory, Product.class);
    }

    public Product getProduct(Long itemId) {

	Map<String, Object> filter = new HashMap<>();
	filter.put("productIndex", (long)itemId);
	return productDao.find(filter);
    }

    public List<Product> getProductList(byte category, byte part, byte character) {

	List<Product> productList;

	switch (EItemCategory.valueOf(category)) {

	case PARTS:
	    productList = getProductPartList(category, part, character);
	    break;

	case HOUSE_DECO:
	    productList = getProductHouseDecoList(category, part);
	    break;

	case RECIPE:
	    productList = getProductRecipeList(category, part, character);
	    break;

	case LOTTERY:
	    productList = getProductLotteryList(category, part);
	    break;

	case ENCHANT:
	    productList = getProductEnchantList(category, part);
	    break;

	default:
	    productList = getProductList(category);
	    break;
	}

	return productList;
    }

    private List<Product> getProductList(byte category) {

	EntityManager em = entityManagerFactory.createEntityManager();

	String sql = "FROM Product p WHERE p.category = :category AND p.enabled = :enabled ";
	List<Product> productList = em.createQuery(sql, Product.class)
		.setParameter("category", EItemCategory.getNameByValue(category))
		.setParameter("enabled", true)
		.getResultList();

	em.close();

	return productList;
    }

    private List<Product> getProductPartList(byte category, byte part, byte character) {

	EntityManager em = entityManagerFactory.createEntityManager();

	String sql = "FROM Product p WHERE p.category = :category AND p.enabled = :enabled ";
	sql += part == EItemPart.SET.getValue() ? "AND p.item1 != 0 AND p.item0 IN :itemPartIndexList " : "AND p.item1 = 0 AND p.item0 IN :itemPartIndexList ";

	String sql2 = "SELECT ip.itemIndex FROM ItemPart ip WHERE ip.forCharacter = :forCharacter " + (part == EItemPart.SET.getValue() ? "" : "AND ip.part IN :part ");
	TypedQuery<Long> itemPartIndexQuery = em.createQuery(sql2, Long.class)
		.setParameter("forCharacter", EItemChar.getNameByValue(character));
	if(part != EItemPart.SET.getValue()) {
	    itemPartIndexQuery.setParameter("part", EItemSubPart.getNamesByValue(part));
	}
	List<Long> itemPartIndexList = itemPartIndexQuery.getResultList();

	List<Product> productList = em.createQuery(sql, Product.class)
		.setParameter("category", EItemCategory.getNameByValue(category))
		.setParameter("enabled", true)
		.setParameter("itemPartIndexList", itemPartIndexList)
		.getResultList();

	em.close();

	return productList;
    }

    private List<Product> getProductHouseDecoList(byte category, byte part) {

	EntityManager em = entityManagerFactory.createEntityManager();

	String sql = "FROM Product p WHERE p.category = :category AND p.enabled = :enabled ";
	sql += "AND p.item0 IN :itemHouseDecoIndexList ";

	String sql2 = "SELECT ihd.itemIndex FROM ItemHouseDeco ihd WHERE ihd.kind = :kind ";
	List<Long> itemHouseDecoIndexList = em.createQuery(sql2, Long.class)
		.setParameter("kind", EItemHouseDeco.getNameByValue(part))
		.getResultList();

	List<Product> productList = em.createQuery(sql, Product.class)
		.setParameter("category", EItemCategory.getNameByValue(category))
		.setParameter("enabled", true)
		.setParameter("itemHouseDecoIndexList", itemHouseDecoIndexList)
		.getResultList();

	em.close();

	return productList;
    }

    private List<Product> getProductRecipeList(byte category, byte part, byte character) {

	EntityManager em = entityManagerFactory.createEntityManager();

	String sql = "FROM Product p WHERE p.category = :category AND p.enabled = :enabled ";
	sql += "AND p.item0 IN :itemRecipeIndexList ";

	String sql2 = "SELECT ir.itemIndex FROM ItemRecipe ir WHERE ir.kind = :kind " + (part == EItemRecipe.CHAR_ITEM.getValue() ? "AND forCharacter = :forCharacter " : "");
	TypedQuery<Long> itemRecipeIndexQuery = em.createQuery(sql2, Long.class)
		.setParameter("kind", EItemRecipe.getNameByValue(part));
	if(part == EItemRecipe.CHAR_ITEM.getValue()) {
	    itemRecipeIndexQuery.setParameter("forCharacter", EItemChar.getNameByValue(character));
	}
	List<Long> itemRecipeIndexList = itemRecipeIndexQuery.getResultList();

	List<Product> productList = em.createQuery(sql, Product.class)
		.setParameter("category", EItemCategory.getNameByValue(category))
		.setParameter("enabled", true)
		.setParameter("itemRecipeIndexList", itemRecipeIndexList)
		.getResultList();

	em.close();

	return productList;
    }

    private List<Product> getProductLotteryList(byte category, byte part) {

	EntityManager em = entityManagerFactory.createEntityManager();

	String sql = "FROM Product p WHERE p.category = :category AND p.enabled = :enabled ";
	sql += "AND p.priceType = :priceType ";

	List<Product> productList = em.createQuery(sql, Product.class)
		.setParameter("category", EItemCategory.getNameByValue(category))
		.setParameter("enabled", true)
		.setParameter("priceType", part == 0 ? "MINT" : "GOLD")
		.getResultList();

	em.close();

	return productList;
    }

    private List<Product> getProductEnchantList(byte category, byte part) {

	EntityManager em = entityManagerFactory.createEntityManager();

	String sql = "FROM Product p WHERE p.category = :category AND p.enabled = :enabled ";
	sql += "AND p.item0 IN :itemEnchantIndexList ";

	String sql2 = "SELECT ie.itemIndex FROM ItemEnchant ie WHERE ie.kind = :kind ";
	List<Long> itemEnchantIndexList = em.createQuery(sql2, Long.class)
		.setParameter("kind", EItemEnchant.getNameByValue(part))
		.getResultList();

	List<Product> productList = em.createQuery(sql, Product.class)
		.setParameter("category", EItemCategory.getNameByValue(category))
		.setParameter("enabled", true)
		.setParameter("itemEnchantIndexList", itemEnchantIndexList)
		.getResultList();

	em.close();

	return productList;
    }
}