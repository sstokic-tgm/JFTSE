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
public class ToolSlotEquipmentServiceImpl implements ToolSlotEquipmentService {
    private final ToolSlotEquipmentRepository toolSlotEquipmentRepository;
    private final PlayerPocketService playerPocketService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ToolSlotEquipment save(ToolSlotEquipment toolSlotEquipment) {
        return toolSlotEquipmentRepository.save(toolSlotEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ToolSlotEquipment findById(Long id) {
        Optional<ToolSlotEquipment> toolSlotEquipment = toolSlotEquipmentRepository.findById(id);
        return toolSlotEquipment.orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateToolSlots(Player player, List<Integer> toolSlotItems) {
        Pocket pocket = player.getPocket();
        ToolSlotEquipment toolSlotEquipment = findById(player.getToolSlotEquipment().getId());

        List<PlayerPocket> playerPockets = playerPocketService.getItemsAsPocket(
                List.of(
                        Long.valueOf(toolSlotItems.get(0)),
                        Long.valueOf(toolSlotItems.get(1)),
                        Long.valueOf(toolSlotItems.get(2)),
                        Long.valueOf(toolSlotItems.get(3)),
                        Long.valueOf(toolSlotItems.get(4))
                ),
                pocket
        );

        for (int i = 0; i < toolSlotItems.size(); i++) {
            Integer itemId = toolSlotItems.get(i);
            PlayerPocket item = playerPockets.stream()
                    .filter(p -> p.getId().intValue() == itemId)
                    .findFirst()
                    .orElse(null);

            int slotValue = item == null ? 0 : item.getId().intValue();

            switch (i) {
                case 0 -> toolSlotEquipment.setSlot1(slotValue);
                case 1 -> toolSlotEquipment.setSlot2(slotValue);
                case 2 -> toolSlotEquipment.setSlot3(slotValue);
                case 3 -> toolSlotEquipment.setSlot4(slotValue);
                case 4 -> toolSlotEquipment.setSlot5(slotValue);
            }
        }

        save(toolSlotEquipment);
    }

    @Override
    @Transactional(readOnly = true)
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
