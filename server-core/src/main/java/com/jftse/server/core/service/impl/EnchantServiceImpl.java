package com.jftse.server.core.service.impl;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.entities.database.model.item.ItemEnchant;
import com.jftse.entities.database.model.item.ItemEnchantLevel;
import com.jftse.entities.database.model.item.ItemPart;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.item.ItemEnchantLevelRepository;
import com.jftse.entities.database.repository.item.ItemEnchantRepository;
import com.jftse.entities.database.repository.item.ItemPartRepository;
import com.jftse.entities.database.repository.pocket.PlayerPocketRepository;
import com.jftse.server.core.item.EElementalKind;
import com.jftse.server.core.service.EnchantService;
import com.jftse.server.core.service.PocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class EnchantServiceImpl implements EnchantService {
    private final ItemEnchantRepository itemEnchantRepository;
    private final ItemEnchantLevelRepository itemEnchantLevelRepository;
    private final ItemPartRepository itemPartRepository;
    private final PlayerPocketRepository playerPocketRepository;
    private final PocketService pocketService;

    @Override
    public PlayerPocket getPlayerPocket(int playerPocketId) {
        return playerPocketRepository.findById((long) playerPocketId).orElse(null);
    }

    private ItemPart getItemPart(int playerPocketId) {
        PlayerPocket pp = playerPocketRepository.findById((long) playerPocketId).orElse(null);
        if (pp == null) {
            return null;
        }

        List<ItemPart> itemPartList = itemPartRepository.findByItemIndexIn(List.of(pp.getItemIndex()));
        return itemPartList.isEmpty() ? null : itemPartList.getFirst();
    }

    @Override
    public boolean isValidPlayerPocketId(int playerPocketId, Pocket pocket) {
        if (pocket == null) {
            return false;
        }

        PlayerPocket pp = playerPocketRepository.findById((long) playerPocketId).orElse(null);
        if (pp == null) {
            return false;
        }

        Pocket p = pocketService.findById(pp.getPocket().getId());
        return p != null && p.getId().equals(pocket.getId());
    }

    @Override
    public boolean isEnchantable(int playerPocketId) {
        PlayerPocket pp = playerPocketRepository.findById((long) playerPocketId).orElse(null);
        ItemPart ip = getItemPart(playerPocketId);
        if (pp == null || ip == null) {
            return false;
        }
        byte str = (byte) (ip.getStrength() + pp.getEnchantStr());
        byte sta = (byte) (ip.getStamina() + pp.getEnchantSta());
        byte dex = (byte) (ip.getDexterity() + pp.getEnchantDex());
        byte wil = (byte) (ip.getWillpower() + pp.getEnchantWil());

        return ip.getMaxStrength() > str || ip.getMaxStamina() > sta || ip.getMaxDexterity() > dex || ip.getMaxWillpower() > wil;
    }

    @Override
    public boolean isElemental(int playerPocketId) {
        ItemPart ip = getItemPart(playerPocketId);
        if (ip == null) {
            return false;
        }
        return ip.getEnchantElement();
    }

    @Override
    public boolean isMaxEnchantLevel(int playerPocketId, boolean forElemental, EElementalKind elementalKind) {
        PlayerPocket pp = playerPocketRepository.findById((long) playerPocketId).orElse(null);
        ItemPart ip = getItemPart(playerPocketId);
        if (pp == null || ip == null) {
            return false;
        }

        final boolean isMaxElemental = ip.getEnchantElement() && pp.getEnchantLevel() == 9 && pp.getEnchantElement().equals(elementalKind.getValue());
        final boolean isMaxStat = isMaxStat(elementalKind, ip, pp);

        if (forElemental) {
            return isMaxElemental;
        }
        return isMaxStat;
    }

    private boolean isMaxStat(EElementalKind elementalKind, ItemPart ip, PlayerPocket pp) {
        int ek = elementalKind.getValue();
        return switch (ek) {
            case 1 -> ip.getMaxStrength() > 0 && (pp.getEnchantStr().byteValue() + ip.getStrength()) == ip.getMaxStrength();
            case 2 -> ip.getMaxStamina() > 0 && (pp.getEnchantSta().byteValue() + ip.getStamina()) == ip.getMaxStamina();
            case 3 -> ip.getMaxDexterity() > 0 && (pp.getEnchantDex().byteValue() + ip.getDexterity()) == ip.getMaxDexterity();
            case 4 -> ip.getMaxWillpower() > 0 && (pp.getEnchantWil().byteValue() + ip.getWillpower()) == ip.getMaxWillpower();
            default -> false;
        };
    }

    private ItemEnchant getItemEnchant(int playerPocketId, Pocket pocket) {
        PlayerPocket pp = playerPocketRepository.findById((long) playerPocketId).orElse(null);
        if (pp == null) {
            return null;
        }

        Pocket p = pocketService.findById(pp.getPocket().getId());
        if (p == null || !p.getId().equals(pocket.getId())) {
            return null;
        }

        List<ItemEnchant> itemEnchantList = itemEnchantRepository.findByItemIndex(pp.getItemIndex());
        return itemEnchantList.isEmpty() ? null : itemEnchantList.getFirst();
    }

    @Override
    public ItemEnchant getItemEnchant(int playerPocketId) {
        PlayerPocket pp = playerPocketRepository.findById((long) playerPocketId).orElse(null);
        if (pp == null) {
            return null;
        }

        Pocket p = pocketService.findById(pp.getPocket().getId());
        if (p == null) {
            return null;
        }

        List<ItemEnchant> itemEnchantList = itemEnchantRepository.findByItemIndex(pp.getItemIndex());
        return itemEnchantList.isEmpty() ? null : itemEnchantList.getFirst();
    }

    @Override
    public ItemEnchantLevel getItemEnchantLevel(String elementalKind, int grade) {
        return itemEnchantLevelRepository.findByGradeAndElementalKind(grade, elementalKind).orElse(null);
    }

    @Override
    public boolean hasJewel(int playerPocketId, Pocket pocket) {
        ItemEnchant ie = getItemEnchant(playerPocketId, pocket);
        return ie != null && ie.getKind().equals("JEWEL");
    }

    @Override
    public boolean hasElemental(int playerPocketId, Pocket pocket) {
        ItemEnchant ie = getItemEnchant(playerPocketId, pocket);
        return ie != null && ie.getKind().equals("ELEMENTAL");
    }


}
