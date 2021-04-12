package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.SpecialSlotEquipment;
import com.jftse.emulator.server.database.repository.player.SpecialSlotEquipmentRepository;
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
public class SpecialSlotEquipmentService {
    private final SpecialSlotEquipmentRepository specialSlotEquipmentRepository;

    public SpecialSlotEquipment save(SpecialSlotEquipment specialSlotEquipment) {
        return specialSlotEquipmentRepository.save(specialSlotEquipment);
    }

    public SpecialSlotEquipment findById(Long id) {
        Optional<SpecialSlotEquipment> specialSlotEquipment = specialSlotEquipmentRepository.findById(id);
        return specialSlotEquipment.orElse(null);
    }

    public void updateSpecialSlots(SpecialSlotEquipment specialSlotEquipment, Integer specialSlotId) {
        specialSlotEquipment = findById(specialSlotEquipment.getId());

        if (specialSlotEquipment.getSlot1().equals(specialSlotId))
            specialSlotEquipment.setSlot1(0);
        else if (specialSlotEquipment.getSlot2().equals(specialSlotId))
            specialSlotEquipment.setSlot2(0);
        else if (specialSlotEquipment.getSlot3().equals(specialSlotId))
            specialSlotEquipment.setSlot3(0);
        else if (specialSlotEquipment.getSlot4().equals(specialSlotId))
            specialSlotEquipment.setSlot4(0);

        specialSlotEquipment = save(specialSlotEquipment);
    }

    public void updateSpecialSlots(SpecialSlotEquipment specialSlotEquipment, List<Integer> specialSlotItems) {
        specialSlotEquipment = findById(specialSlotEquipment.getId());

        specialSlotEquipment.setSlot1(specialSlotItems.get(0));
        specialSlotEquipment.setSlot2(specialSlotItems.get(1));
        specialSlotEquipment.setSlot3(specialSlotItems.get(2));
        specialSlotEquipment.setSlot4(specialSlotItems.get(3));

        specialSlotEquipment = save(specialSlotEquipment);
    }

    public List<Integer> getEquippedSpecialSlots(Player player) {
        List<Integer> result = new ArrayList<>();

        SpecialSlotEquipment specialSlotEquipment = findById(player.getSpecialSlotEquipment().getId());

        result.add(specialSlotEquipment.getSlot1());
        result.add(specialSlotEquipment.getSlot2());
        result.add(specialSlotEquipment.getSlot3());
        result.add(specialSlotEquipment.getSlot4());

        return result;
    }
}
