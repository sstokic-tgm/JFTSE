package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.ToolSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.player.ToolSlotEquipmentRepository;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.ToolSlotEquipmentService;
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
public class ToolSlotEquipmentServiceImpl implements ToolSlotEquipmentService {
    private final ToolSlotEquipmentRepository toolSlotEquipmentRepository;
    private final PlayerPocketService playerPocketService;

    @Override
    public ToolSlotEquipment save(ToolSlotEquipment toolSlotEquipment) {
        return toolSlotEquipmentRepository.save(toolSlotEquipment);
    }

    @Override
    public ToolSlotEquipment findById(Long id) {
        Optional<ToolSlotEquipment> toolSlotEquipment = toolSlotEquipmentRepository.findById(id);
        return toolSlotEquipment.orElse(null);
    }

    @Override
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

        save(toolSlotEquipment);
    }

    @Override
    public void updateToolSlots(Player player, List<Integer> toolSlotItems) {
        Pocket pocket = player.getPocket();
        ToolSlotEquipment toolSlotEquipment = findById(player.getToolSlotEquipment().getId());

        PlayerPocket item = playerPocketService.getItemAsPocket((long) toolSlotItems.get(0), pocket);
        toolSlotEquipment.setSlot1(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) toolSlotItems.get(1), pocket);
        toolSlotEquipment.setSlot2(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) toolSlotItems.get(2), pocket);
        toolSlotEquipment.setSlot3(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) toolSlotItems.get(3), pocket);
        toolSlotEquipment.setSlot4(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) toolSlotItems.get(4), pocket);
        toolSlotEquipment.setSlot5(item == null ? 0 : item.getId().intValue());

        save(toolSlotEquipment);
    }

    @Override
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
