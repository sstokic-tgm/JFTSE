package com.jftse.b2b.service.impl;

import com.jftse.entities.database.model.item.ItemPart;
import com.jftse.entities.database.model.player.ClothEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.StatusPointsAddedDto;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.repository.item.ItemPartRepository;
import com.jftse.entities.database.repository.player.ClothEquipmentRepository;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.service.ClothEquipmentService;
import com.jftse.server.core.service.PlayerPocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ClothEquipmentServiceImpl implements ClothEquipmentService {
    private final ItemPartRepository itemPartRepository;
    private final ClothEquipmentRepository clothEquipmentRepository;

    private final PlayerPocketService playerPocketService;

    @Override
    public ClothEquipment save(ClothEquipment clothEquipment) {
        return clothEquipmentRepository.save(clothEquipment);
    }

    @Override
    public ClothEquipment findClothEquipmentById(Long id) {
        Optional<ClothEquipment> clothEquipment = clothEquipmentRepository.findById(id);
        return clothEquipment.orElse(null);
    }

    @Override
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
    public StatusPointsAddedDto getStatusPointsFromCloths(Player player) {
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

        byte strength = 0;
        byte stamina = 0;
        byte dexterity = 0;
        byte willpower = 0;
        int addHp = 0;

        for (ItemPart itemPart : itemPartList) {
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
}