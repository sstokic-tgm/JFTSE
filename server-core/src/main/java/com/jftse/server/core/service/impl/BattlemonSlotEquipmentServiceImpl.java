package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.BattlemonSlotEquipment;
import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.repository.player.BattlemonSlotEquipmentRepository;
import com.jftse.server.core.service.BattlemonSlotEquipmentService;
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
public class BattlemonSlotEquipmentServiceImpl implements BattlemonSlotEquipmentService {
    private final BattlemonSlotEquipmentRepository battlemonSlotEquipmentRepository;

    @Override
    public BattlemonSlotEquipment save(BattlemonSlotEquipment battlemonSlotEquipment) {
        return battlemonSlotEquipmentRepository.save(battlemonSlotEquipment);
    }

    @Override
    public BattlemonSlotEquipment findById(Long id) {
        Optional<BattlemonSlotEquipment> battlemonSlotEquipment = battlemonSlotEquipmentRepository.findById(id);
        return battlemonSlotEquipment.orElse(null);
    }

    @Override
    public void updateBattlemonSlots(BattlemonSlotEquipment battlemonSlotEquipment, Integer battlemonSlotId) {
        battlemonSlotEquipment = findById(battlemonSlotEquipment.getId());

        if (battlemonSlotEquipment.getSlot1().equals(battlemonSlotId))
            battlemonSlotEquipment.setSlot1(0);
        else if (battlemonSlotEquipment.getSlot2().equals(battlemonSlotId))
            battlemonSlotEquipment.setSlot2(0);

        battlemonSlotEquipment = save(battlemonSlotEquipment);
    }

    @Override
    public void updateBattlemonSlots(BattlemonSlotEquipment battlemonSlotEquipment, List<Integer> battlemonSlotItems) {
        battlemonSlotEquipment = findById(battlemonSlotEquipment.getId());

        battlemonSlotEquipment.setSlot1(battlemonSlotItems.get(0));
        battlemonSlotEquipment.setSlot2(battlemonSlotItems.get(1));

        battlemonSlotEquipment = save(battlemonSlotEquipment);
    }

    @Override
    public List<Integer> getEquippedBattlemonSlots(Player player) {
        List<Integer> result = new ArrayList<>();

        BattlemonSlotEquipment battlemonSlotEquipment = null;// findById(player.getBattlemonSlotEquipment().getId());

        if (battlemonSlotEquipment != null) {
            result.add(battlemonSlotEquipment.getSlot1());
            result.add(battlemonSlotEquipment.getSlot2());
        }

        return result;
    }
}
