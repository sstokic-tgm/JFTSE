package com.ft.emulator.server.game.core.service;

import com.ft.emulator.server.database.model.item.ItemPart;
import com.ft.emulator.server.database.model.player.ClothEquipment;
import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.database.model.player.StatusPointsAddedDto;
import com.ft.emulator.server.database.model.pocket.PlayerPocket;
import com.ft.emulator.server.database.repository.item.ItemPartRepository;
import com.ft.emulator.server.database.repository.player.ClothEquipmentRepository;
import com.ft.emulator.server.game.core.packet.packets.inventory.C2SInventoryWearClothReqPacket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ClothEquipmentService {
    private final ItemPartRepository itemPartRepository;
    private final ClothEquipmentRepository clothEquipmentRepository;

    private final PlayerPocketService playerPocketService;

    public ClothEquipment save(ClothEquipment clothEquipment) {
        return clothEquipmentRepository.save(clothEquipment);
    }

    public ClothEquipment findClothEquipmentById(Long id) {
        Optional<ClothEquipment> clothEquipment = clothEquipmentRepository.findById(id);
        return clothEquipment.orElse(null);
    }

    public void updateCloths(ClothEquipment clothEquipment, C2SInventoryWearClothReqPacket inventoryWearClothReqPacket) {
        clothEquipment = findClothEquipmentById(clothEquipment.getId());

        PlayerPocket item = playerPocketService.findById((long) inventoryWearClothReqPacket.getHair());
        clothEquipment.setHair(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getFace());
        clothEquipment.setFace(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getDress());
        clothEquipment.setDress(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getPants());
        clothEquipment.setPants(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getSocks());
        clothEquipment.setSocks(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getShoes());
        clothEquipment.setShoes(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getGloves());
        clothEquipment.setGloves(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getRacket());
        clothEquipment.setRacket(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getGlasses());
        clothEquipment.setGlasses(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getBag());
        clothEquipment.setBag(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getHat());
        clothEquipment.setHat(item == null ? 0 : item.getItemIndex());

        item = playerPocketService.findById((long) inventoryWearClothReqPacket.getDye());
        clothEquipment.setDye(item == null ? 0 : item.getItemIndex());

        clothEquipment = save(clothEquipment);
    }

    public Map<String, Integer> getEquippedCloths(Player player) {
        Map<String, Integer> result = new HashMap<>();

        ClothEquipment clothEquipment = findClothEquipmentById(player.getClothEquipment().getId());

        PlayerPocket item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getHair(), player.getPocket());
        result.put("hair", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getFace(), player.getPocket());
        result.put("face", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getDress(), player.getPocket());
        result.put("dress", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getPants(), player.getPocket());
        result.put("pants", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getSocks(), player.getPocket());
        result.put("socks", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getShoes(), player.getPocket());
        result.put("shoes", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getGloves(), player.getPocket());
        result.put("gloves", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getRacket(), player.getPocket());
        result.put("racket", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getGlasses(), player.getPocket());
        result.put("glasses", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getBag(), player.getPocket());
        result.put("bag", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getHat(), player.getPocket());
        result.put("hat", item == null ? 0 : (int) item.getId().longValue());

        item = playerPocketService.getItemAsPocketByItemIndex(clothEquipment.getDye(), player.getPocket());
        result.put("dye", item == null ? 0 : (int) item.getId().longValue());

        return result;
    }

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
