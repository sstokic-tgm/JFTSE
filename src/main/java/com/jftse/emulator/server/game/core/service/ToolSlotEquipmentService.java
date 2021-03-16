package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.ToolSlotEquipment;
import com.jftse.emulator.server.database.repository.player.ToolSlotEquipmentRepository;
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
public class ToolSlotEquipmentService {
    private final ToolSlotEquipmentRepository toolSlotEquipmentRepository;

    public ToolSlotEquipment save(ToolSlotEquipment toolSlotEquipment) {
        return toolSlotEquipmentRepository.save(toolSlotEquipment);
    }

    public ToolSlotEquipment findById(Long id) {
        Optional<ToolSlotEquipment> toolSlotEquipment = toolSlotEquipmentRepository.findById(id);
        return toolSlotEquipment.orElse(null);
    }

    public void updateToolSlots(ToolSlotEquipment toolSlotEquipment, Integer toolSlotId) {
        toolSlotEquipment = findById(toolSlotEquipment.getId());

        if (toolSlotEquipment.getSlot1().equals(toolSlotId))
            toolSlotEquipment.setSlot1(0);
        else if (toolSlotEquipment.getSlot2().equals(toolSlotId))
            toolSlotEquipment.setSlot2(0);
        else if (toolSlotEquipment.getSlot3().equals(toolSlotId))
            toolSlotEquipment.setSlot3(0);
        else if (toolSlotEquipment.getSlot4().equals(toolSlotId))
            toolSlotEquipment.setSlot4(0);
        else if (toolSlotEquipment.getSlot5().equals(toolSlotId))
            toolSlotEquipment.setSlot5(0);

        toolSlotEquipment = save(toolSlotEquipment);
    }

    public void updateToolSlots(ToolSlotEquipment toolSlotEquipment, List<Integer> toolSlotItems) {
        toolSlotEquipment = findById(toolSlotEquipment.getId());

        toolSlotEquipment.setSlot1(toolSlotItems.get(0));
        toolSlotEquipment.setSlot2(toolSlotItems.get(1));
        toolSlotEquipment.setSlot3(toolSlotItems.get(2));
        toolSlotEquipment.setSlot4(toolSlotItems.get(3));
        toolSlotEquipment.setSlot5(toolSlotItems.get(4));

        toolSlotEquipment = save(toolSlotEquipment);
    }

    public List<Integer> getEquippedToolSlots(Player player) {
        List<Integer> result = new ArrayList<>();

        ToolSlotEquipment toolSlotEquipment = findById(player.getToolSlotEquipment().getId());

        result.add(toolSlotEquipment.getSlot1());
        result.add(toolSlotEquipment.getSlot2());
        result.add(toolSlotEquipment.getSlot3());
        result.add(toolSlotEquipment.getSlot4());
        result.add(toolSlotEquipment.getSlot5());

        return result;
    }
}
