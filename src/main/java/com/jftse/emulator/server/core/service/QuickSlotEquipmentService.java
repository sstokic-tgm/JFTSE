package com.jftse.emulator.server.core.service;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.database.repository.player.QuickSlotEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class QuickSlotEquipmentService {
    private final QuickSlotEquipmentRepository quickSlotEquipmentRepository;
    private final PlayerPocketService playerPocketService;

    public QuickSlotEquipment save(QuickSlotEquipment quickSlotEquipment) {
        return quickSlotEquipmentRepository.save(quickSlotEquipment);
    }

    public QuickSlotEquipment findById(Long id) {
        Optional<QuickSlotEquipment> quickSlotEquipment = quickSlotEquipmentRepository.findById(id);
        return quickSlotEquipment.orElse(null);
    }

    public void updateQuickSlots(QuickSlotEquipment quickSlotEquipment, Integer quickSlotId) {
        quickSlotEquipment = findById(quickSlotEquipment.getId());

        if (quickSlotEquipment.getSlot1().equals(quickSlotId))
            quickSlotEquipment.setSlot1(0);
        else if (quickSlotEquipment.getSlot2().equals(quickSlotId))
            quickSlotEquipment.setSlot2(0);
        else if (quickSlotEquipment.getSlot3().equals(quickSlotId))
            quickSlotEquipment.setSlot3(0);
        else if (quickSlotEquipment.getSlot4().equals(quickSlotId))
            quickSlotEquipment.setSlot4(0);
        else if (quickSlotEquipment.getSlot5().equals(quickSlotId))
            quickSlotEquipment.setSlot5(0);

        save(quickSlotEquipment);
    }

    public void updateQuickSlots(Player player, List<Integer> quickSlotItems) {
        Pocket pocket = player.getPocket();
        QuickSlotEquipment quickSlotEquipment = findById(player.getQuickSlotEquipment().getId());

        PlayerPocket item = playerPocketService.getItemAsPocket((long) quickSlotItems.get(0), pocket);
        quickSlotEquipment.setSlot1(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) quickSlotItems.get(1), pocket);
        quickSlotEquipment.setSlot2(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) quickSlotItems.get(2), pocket);
        quickSlotEquipment.setSlot3(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) quickSlotItems.get(3), pocket);
        quickSlotEquipment.setSlot4(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) quickSlotItems.get(4), pocket);
        quickSlotEquipment.setSlot5(item == null ? 0 : item.getId().intValue());

        save(quickSlotEquipment);
    }

    public List<Integer> getEquippedQuickSlots(Player player) {
        List<Integer> result = new ArrayList<>();

        QuickSlotEquipment quickSlotEquipment = findById(player.getQuickSlotEquipment().getId());

        result.add(quickSlotEquipment.getSlot1());
        result.add(quickSlotEquipment.getSlot2());
        result.add(quickSlotEquipment.getSlot3());
        result.add(quickSlotEquipment.getSlot4());
        result.add(quickSlotEquipment.getSlot5());

        return result;
    }
}
