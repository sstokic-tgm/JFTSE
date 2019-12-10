package com.ft.emulator.server.game.inventory;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.Service;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.character.StatusPointsAddedDto;
import com.ft.emulator.server.database.model.item.ItemPart;
import com.ft.emulator.server.database.model.pocket.CharacterPlayerPocket;
import com.ft.emulator.server.database.model.pocket.Pocket;
import com.ft.emulator.server.game.item.EItemCategory;
import com.ft.emulator.server.game.server.packets.inventory.C2SInventoryWearClothReqPacket;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryImpl extends Service {

    private GenericModelDao<CharacterPlayerPocket> characterPlayerPocketDao;
    private GenericModelDao<Pocket> pocketDao;

    public InventoryImpl(EntityManagerFactory entityManagerFactory) {

	super(entityManagerFactory);

	characterPlayerPocketDao = new GenericModelDao<>(entityManagerFactory, CharacterPlayerPocket.class);
	pocketDao = new GenericModelDao<>(entityManagerFactory, Pocket.class);
    }

    public Pocket incrementPocketBelongings(Pocket pocket) throws ValidationException {

        pocket = pocketDao.find(pocket.getId());

        pocket.setBelongings(pocket.getBelongings() + 1);
        return pocketDao.save(pocket);
    }

    public Pocket decrementPocketBelongings(Pocket pocket) throws ValidationException {

	pocket = pocketDao.find(pocket.getId());

        pocket.setBelongings(pocket.getBelongings() - 1);
        return pocketDao.save(pocket);
    }

    public CharacterPlayerPocket getItemAsPocket(Long itemPocketId, Pocket playerPocket) {

	Map<String, Object> filter = new HashMap<>();

	filter.put("id", itemPocketId);
	filter.put("pocket", playerPocket);

	return characterPlayerPocketDao.find(filter);
    }

    public CharacterPlayerPocket getItemAsPocketByItemIndex(Long itemIndex, Pocket playerPocket) {

        Map<String, Object> filter = new HashMap<>();
        filter.put("itemIndex", itemIndex);
        filter.put("pocket", playerPocket);

        return characterPlayerPocketDao.find(filter);
    }

    public List<CharacterPlayerPocket> getInventoryItems(Pocket playerPocket) {

	Map<String, Object> filter = new HashMap<>();
	filter.put("pocket", playerPocket);
	return characterPlayerPocketDao.getList(filter);
    }

    public Integer getItemSellPrice(CharacterPlayerPocket item) {

	EntityManager em = entityManagerFactory.createEntityManager();

	Integer itemCount = item.getItemCount();
	// in case of durable, instant etc. items
	itemCount = itemCount >= 50 ? 1 : itemCount;

        Integer sellPrice = null;
        if(item.getCategory().equals(EItemCategory.MATERIAL.getName())) {

	    List<Integer> sellPriceResult = em.createQuery("SELECT im.sellPrice FROM ItemMaterial im WHERE im.itemIndex = :itemIndex ", Integer.class)
		    .setParameter("itemIndex", item.getItemIndex())
		    .getResultList();

	    sellPrice = sellPriceResult.get(0)  * itemCount;
	}
        else if(item.getCategory().equals(EItemCategory.ENCHANT.getName())) {

	    List<Integer> sellPriceResult = em.createQuery("SELECT ie.sellPrice FROM ItemEnchant ie WHERE ie.itemIndex = :itemIndex ", Integer.class)
		    .setParameter("itemIndex", item.getItemIndex())
		    .getResultList();

	    sellPrice = sellPriceResult.get(0) * itemCount;
	}
	// everything else buy price / 2
	else {

	    List<Integer> sellPriceResult = em.createQuery("SELECT p.price0 FROM Product p WHERE p.item0 = :itemIndex AND p.category = :category ", Integer.class)
		    .setParameter("itemIndex", item.getItemIndex())
		    .setParameter("category", item.getCategory())
		    .getResultList();

	    sellPrice = (int)Math.ceil((sellPriceResult.get(0) * itemCount) / 2);
	}

        em.close();

	return sellPrice;
    }

    public void updateCloths(CharacterPlayer characterPlayer, C2SInventoryWearClothReqPacket inventoryWearClothReqPacket) {

	CharacterPlayerPocket item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getHair());
	characterPlayer.setHair(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getFace());
	characterPlayer.setFace(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getDress());
	characterPlayer.setDress(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getPants());
	characterPlayer.setPants(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getSocks());
	characterPlayer.setSocks(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getShoes());
	characterPlayer.setShoes(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getGloves());
	characterPlayer.setGloves(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getRacket());
	characterPlayer.setRacket(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getGlasses());
	characterPlayer.setGlasses(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getBag());
	characterPlayer.setBag(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getHat());
	characterPlayer.setHat(item == null ? 0 : Math.toIntExact(item.getItemIndex()));

	item = characterPlayerPocketDao.find((long)inventoryWearClothReqPacket.getDye());
	characterPlayer.setDye(item == null ? 0 : Math.toIntExact(item.getItemIndex()));
    }

    public Map<String, Integer> getEquippedCloths(CharacterPlayer characterPlayer) {

        Map<String, Integer> result = new HashMap<>();
        Map<String, Object> filter = new HashMap<>();

        filter.put("itemIndex", (long)characterPlayer.getHair());
	CharacterPlayerPocket item = characterPlayerPocketDao.find(filter);
	result.put("hair", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getFace());
	item = characterPlayerPocketDao.find(filter);
	result.put("face", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getDress());
	item = characterPlayerPocketDao.find(filter);
	result.put("dress", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getPants());
	item = characterPlayerPocketDao.find(filter);
	result.put("pants", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getSocks());
	item = characterPlayerPocketDao.find(filter);
	result.put("socks", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getShoes());
	item = characterPlayerPocketDao.find(filter);
	result.put("shoes", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getGloves());
	item = characterPlayerPocketDao.find(filter);
	result.put("gloves", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getRacket());
	item = characterPlayerPocketDao.find(filter);
	result.put("racket", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getGlasses());
	item = characterPlayerPocketDao.find(filter);
	result.put("glasses", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getBag());
	item = characterPlayerPocketDao.find(filter);
	result.put("bag", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getHat());
	item = characterPlayerPocketDao.find(filter);
	result.put("hat", (item == null ? 0 : Math.toIntExact(item.getId())));

	filter.put("itemIndex", (long)characterPlayer.getDye());
	item = characterPlayerPocketDao.find(filter);
	result.put("dye", (item == null ? 0 : Math.toIntExact(item.getId())));

        return result;
    }

    public StatusPointsAddedDto getStatusPointsFromCloths(CharacterPlayer characterPlayer) {

	List<Long> itemIndexList = new ArrayList<>();
	itemIndexList.add(characterPlayer.getHair().longValue());
	itemIndexList.add(characterPlayer.getFace().longValue());
	itemIndexList.add(characterPlayer.getDress().longValue());
	itemIndexList.add(characterPlayer.getPants().longValue());
	itemIndexList.add(characterPlayer.getSocks().longValue());
	itemIndexList.add(characterPlayer.getShoes().longValue());
	itemIndexList.add(characterPlayer.getGloves().longValue());
	itemIndexList.add(characterPlayer.getRacket().longValue());
	itemIndexList.add(characterPlayer.getGlasses().longValue());
	itemIndexList.add(characterPlayer.getBag().longValue());
	itemIndexList.add(characterPlayer.getHat().longValue());
	itemIndexList.add(characterPlayer.getDye().longValue());

	EntityManager em = entityManagerFactory.createEntityManager();

	List<ItemPart> itemPartList = em.createQuery("FROM ItemPart WHERE itemIndex IN :itemIndexList", ItemPart.class)
		.setParameter("itemIndexList", itemIndexList)
		.getResultList();

	em.close();

	byte strength = 0;
	byte stamina = 0;
	byte dexterity = 0;
	byte willpower = 0;
	int addHp = 0;

	for(ItemPart itemPart : itemPartList) {
	    strength += itemPart.getStrength();
	    stamina += itemPart.getStamina();
	    dexterity += itemPart.getDexterity();
	    willpower += itemPart.getWillpower();
	    addHp += itemPart.getAddHp();
	}

	StatusPointsAddedDto statusPointsAddedDto = new StatusPointsAddedDto();
	statusPointsAddedDto.setStrength(strength);
	statusPointsAddedDto.setStamina(stamina);
	statusPointsAddedDto.setDexterity(dexterity);
	statusPointsAddedDto.setWillpower(willpower);
	statusPointsAddedDto.setAddHp(addHp);

	return statusPointsAddedDto;
    }

    public void removeItemFromInventory(Long itemPocketId) {
	characterPlayerPocketDao.remove(itemPocketId);
    }
}