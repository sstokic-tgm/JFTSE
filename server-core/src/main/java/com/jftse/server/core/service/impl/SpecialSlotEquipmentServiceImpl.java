package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.SpecialSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.player.SpecialSlotEquipmentRepository;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.service.SpecialSlotEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialSlotEquipmentServiceImpl implements SpecialSlotEquipmentService {
    private final SpecialSlotEquipmentRepository specialSlotEquipmentRepository;
    private final PlayerPocketService playerPocketService;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SpecialSlotEquipment save(SpecialSlotEquipment specialSlotEquipment) {
        return specialSlotEquipmentRepository.save(specialSlotEquipment);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialSlotEquipment findById(Long id) {
        Optional<SpecialSlotEquipment> specialSlotEquipment = specialSlotEquipmentRepository.findById(id);
        return specialSlotEquipment.orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateSpecialSlots(Player player, List<Integer> specialSlotItems) {
        Pocket pocket = player.getPocket();
        SpecialSlotEquipment specialSlotEquipment = findById(player.getSpecialSlotEquipment().getId());

        List<PlayerPocket> playerPockets = playerPocketService.getItemsAsPocket(
                List.of(
                        Long.valueOf(specialSlotItems.get(0)),
                        Long.valueOf(specialSlotItems.get(1)),
                        Long.valueOf(specialSlotItems.get(2)),
                        Long.valueOf(specialSlotItems.get(3))
                ),
                pocket
        );

        for (int i = 0; i < specialSlotItems.size(); i++) {
            Integer itemId = specialSlotItems.get(i);
            PlayerPocket item = playerPockets.stream()
                    .filter(pp -> pp.getId().intValue() == itemId)
                    .findFirst()
                    .orElse(null);

            int slotValue = item == null ? 0 : item.getId().intValue();

            switch (i) {
                case 0 -> specialSlotEquipment.setSlot1(slotValue);
                case 1 -> specialSlotEquipment.setSlot2(slotValue);
                case 2 -> specialSlotEquipment.setSlot3(slotValue);
                case 3 -> specialSlotEquipment.setSlot4(slotValue);
            }
        }

        save(specialSlotEquipment);
    }

    @Override
    @Transactional(readOnly = true)
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
