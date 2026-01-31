package com.jftse.emulator.server.core.service.impl;

import com.jftse.entities.database.model.item.ItemPart;
import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.EquippedItemStats;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.item.ItemPartRepository;
import com.jftse.entities.database.repository.player.ClothEquipmentRepository;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.ClothEquipmentService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.shared.packets.inventory.CMSGInventoryWearCloth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClothEquipmentServiceImpl implements ClothEquipmentService {
    private final ItemPartRepository itemPartRepository;
    private final ClothEquipmentRepository clothEquipmentRepository;

    private final PlayerPocketService playerPocketService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ClothEquipment save(ClothEquipment clothEquipment) {
        return clothEquipmentRepository.save(clothEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothEquipment findClothEquipmentById(Long id) {
        Optional<ClothEquipment> clothEquipment = clothEquipmentRepository.findById(id);
        return clothEquipment.orElse(null);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateCloths(Player player, CMSGInventoryWearCloth inventoryWearClothReqPacket) {
        Pocket pocket = player.getPocket();
        ClothEquipment clothEquipment = findClothEquipmentById(player.getClothEquipment().getId());

        List<Long> itemIdList = List.of(
                (long) inventoryWearClothReqPacket.getHair(),
                (long) inventoryWearClothReqPacket.getFace(),
                (long) inventoryWearClothReqPacket.getDress(),
                (long) inventoryWearClothReqPacket.getPants(),
                (long) inventoryWearClothReqPacket.getSocks(),
                (long) inventoryWearClothReqPacket.getShoes(),
                (long) inventoryWearClothReqPacket.getGloves(),
                (long) inventoryWearClothReqPacket.getRacket(),
                (long) inventoryWearClothReqPacket.getGlasses(),
                (long) inventoryWearClothReqPacket.getBag(),
                (long) inventoryWearClothReqPacket.getHat(),
                (long) inventoryWearClothReqPacket.getDye()
        );

        Map<Long, Integer> playerPockets = playerPocketService.getItemsAsPocket(itemIdList, pocket).stream()
                .collect(Collectors.toMap(PlayerPocket::getId, PlayerPocket::getItemIndex));

        clothEquipment.setHair(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getHair(), 0));
        clothEquipment.setFace(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getFace(), 0));
        clothEquipment.setDress(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getDress(), 0));
        clothEquipment.setPants(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getPants(), 0));
        clothEquipment.setSocks(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getSocks(), 0));
        clothEquipment.setShoes(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getShoes(), 0));
        clothEquipment.setGloves(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getGloves(), 0));
        clothEquipment.setRacket(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getRacket(), 0));
        clothEquipment.setGlasses(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getGlasses(), 0));
        clothEquipment.setBag(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getBag(), 0));
        clothEquipment.setHat(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getHat(), 0));
        clothEquipment.setDye(playerPockets.getOrDefault((long) inventoryWearClothReqPacket.getDye(), 0));

        save(clothEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getEquippedCloths(Player player) {
        Map<String, Integer> result = new HashMap<>();

        ClothEquipment clothEquipment = findClothEquipmentById(player.getClothEquipment().getId());

        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getHair(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("hair", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getFace(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("face", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getDress(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("dress", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getPants(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("pants", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getSocks(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("socks", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getShoes(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("shoes", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getGloves(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("gloves", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getRacket(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("racket", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getGlasses(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("glasses", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getBag(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("bag", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getHat(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("hat", item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocketByItemIndexAndCategoryAndPocket(clothEquipment.getDye(), EItemCategory.PARTS.getName(), player.getPocket());
        result.put("dye", item == null ? 0 : item.getId().intValue());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public EquippedItemStats getStatusPointsFromCloths(Player player) {
        ClothEquipment clothEquipment = findClothEquipmentById(player.getClothEquipment().getId());

        List<Integer> itemIndexList = new ArrayList<>();
        itemIndexList.add(clothEquipment.getHair());
        itemIndexList.add(clothEquipment.getFace());
        itemIndexList.add(clothEquipment.getDress());
        itemIndexList.add(clothEquipment.getPants());
        itemIndexList.add(clothEquipment.getSocks());
        itemIndexList.add(clothEquipment.getShoes());
        itemIndexList.add(clothEquipment.getGloves());
        itemIndexList.add(clothEquipment.getRacket());
        itemIndexList.add(clothEquipment.getGlasses());
        itemIndexList.add(clothEquipment.getBag());
        itemIndexList.add(clothEquipment.getHat());
        itemIndexList.add(clothEquipment.getDye());

        List<ItemPart> itemPartList = itemPartRepository.findByItemIndexIn(itemIndexList);
        List<PlayerPocket> playerPocketList = playerPocketService.getPlayerPocketItems(player.getPocket());
        playerPocketList.removeIf(playerPocket -> !itemIndexList.contains(playerPocket.getItemIndex()));

        int strength = 0;
        int stamina = 0;
        int dexterity = 0;
        int willpower = 0;
        int addHp = 0;
        int addStr = 0;
        int addSta = 0;
        int addDex = 0;
        int addWil = 0;

        for (ItemPart itemPart : itemPartList) {
            strength += itemPart.getStrength();
            stamina += itemPart.getStamina();
            dexterity += itemPart.getDexterity();
            willpower += itemPart.getWillpower();
            addHp += itemPart.getAddHp();
        }

        for (PlayerPocket playerPocket : playerPocketList) {
            addStr += playerPocket.getEnchantStr();
            addSta += playerPocket.getEnchantSta();
            addDex += playerPocket.getEnchantDex();
            addWil += playerPocket.getEnchantWil();
        }

        EquippedItemStats equippedItemStats = new EquippedItemStats();
        equippedItemStats.setStrength(strength);
        equippedItemStats.setStamina(stamina);
        equippedItemStats.setDexterity(dexterity);
        equippedItemStats.setWillpower(willpower);
        equippedItemStats.setAddHp(addHp);
        equippedItemStats.setEnchantStr(addStr);
        equippedItemStats.setEnchantSta(addSta);
        equippedItemStats.setEnchantDex(addDex);
        equippedItemStats.setEnchantWil(addWil);

        return equippedItemStats;
    }
}
