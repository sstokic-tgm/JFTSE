package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.QuickSlotEquipment;
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

        quickSlotEquipment = save(quickSlotEquipment);
    }

    public void updateQuickSlots(QuickSlotEquipment quickSlotEquipment, List<Integer> quickSlotItems) {
        quickSlotEquipment = findById(quickSlotEquipment.getId());

        quickSlotEquipment.setSlot1(quickSlotItems.get(0));
        quickSlotEquipment.setSlot2(quickSlotItems.get(1));
        quickSlotEquipment.setSlot3(quickSlotItems.get(2));
        quickSlotEquipment.setSlot4(quickSlotItems.get(3));
        quickSlotEquipment.setSlot5(quickSlotItems.get(4));

        quickSlotEquipment = save(quickSlotEquipment);
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
