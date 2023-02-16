package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.SpecialSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.player.SpecialSlotEquipmentRepository;
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
    private final PlayerPocketService playerPocketService;

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

        save(specialSlotEquipment);
    }

    public void updateSpecialSlots(Player player, List<Integer> specialSlotItems) {
        Pocket pocket = player.getPocket();
        SpecialSlotEquipment specialSlotEquipment = findById(player.getSpecialSlotEquipment().getId());

        PlayerPocket item = playerPocketService.getItemAsPocket((long) specialSlotItems.get(0), pocket);
        specialSlotEquipment.setSlot1(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) specialSlotItems.get(1), pocket);
        specialSlotEquipment.setSlot2(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) specialSlotItems.get(2), pocket);
        specialSlotEquipment.setSlot3(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) specialSlotItems.get(3), pocket);
        specialSlotEquipment.setSlot4(item == null ? 0 : item.getId().intValue());

        save(specialSlotEquipment);
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
