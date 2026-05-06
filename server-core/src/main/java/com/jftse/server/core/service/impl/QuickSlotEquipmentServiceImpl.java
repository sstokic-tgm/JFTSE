package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.QuickSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.player.QuickSlotEquipmentRepository;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.QuickSlotEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuickSlotEquipmentServiceImpl implements QuickSlotEquipmentService {
    private final QuickSlotEquipmentRepository quickSlotEquipmentRepository;
    private final PlayerPocketService playerPocketService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuickSlotEquipment save(QuickSlotEquipment quickSlotEquipment) {
        return quickSlotEquipmentRepository.save(quickSlotEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public QuickSlotEquipment findById(Long id) {
        Optional<QuickSlotEquipment> quickSlotEquipment = quickSlotEquipmentRepository.findById(id);
        return quickSlotEquipment.orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateQuickSlots(Player player, List<Integer> quickSlotItems) {
        Pocket pocket = player.getPocket();
        QuickSlotEquipment quickSlotEquipment = findById(player.getQuickSlotEquipment().getId());

        List<PlayerPocket> playerPockets = playerPocketService.getItemsAsPocket(
                List.of(
                        Long.valueOf(quickSlotItems.get(0)),
                        Long.valueOf(quickSlotItems.get(1)),
                        Long.valueOf(quickSlotItems.get(2)),
                        Long.valueOf(quickSlotItems.get(3)),
                        Long.valueOf(quickSlotItems.get(4))
                        ),
                pocket
        );

        for (int i = 0; i < quickSlotItems.size(); i++) {
            Integer itemId = quickSlotItems.get(i);
            PlayerPocket item = playerPockets.stream()
                    .filter(p -> p.getId().intValue() == itemId)
                    .findFirst()
                    .orElse(null);

            int slotValue = item == null ? 0 : item.getId().intValue();

            switch (i) {
                case 0 -> quickSlotEquipment.setSlot1(slotValue);
                case 1 -> quickSlotEquipment.setSlot2(slotValue);
                case 2 -> quickSlotEquipment.setSlot3(slotValue);
                case 3 -> quickSlotEquipment.setSlot4(slotValue);
                case 4 -> quickSlotEquipment.setSlot5(slotValue);
            }
        }

        save(quickSlotEquipment);
    }

    @Override
    @Transactional(readOnly = true)
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
