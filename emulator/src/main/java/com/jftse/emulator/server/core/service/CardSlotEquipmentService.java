package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.player.Player;
import com.jftse.entities.database.model.player.CardSlotEquipment;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.player.CardSlotEquipmentRepository;
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
public class CardSlotEquipmentService {
    private final CardSlotEquipmentRepository cardSlotEquipmentRepository;
    private final PlayerPocketService playerPocketService;

    public CardSlotEquipment save(CardSlotEquipment cardSlotEquipment) {
        return cardSlotEquipmentRepository.save(cardSlotEquipment);
    }

    public CardSlotEquipment findById(Long id) {
        Optional<CardSlotEquipment> cardSlotEquipment = cardSlotEquipmentRepository.findById(id);
        return cardSlotEquipment.orElse(null);
    }

    public void updateCardSlots(CardSlotEquipment cardSlotEquipment, Integer cardSlotId) {
        cardSlotEquipment = findById(cardSlotEquipment.getId());

        if (cardSlotEquipment.getSlot1().equals(cardSlotId))
            cardSlotEquipment.setSlot1(0);
        else if (cardSlotEquipment.getSlot2().equals(cardSlotId))
            cardSlotEquipment.setSlot2(0);
        else if (cardSlotEquipment.getSlot3().equals(cardSlotId))
            cardSlotEquipment.setSlot3(0);
        else if (cardSlotEquipment.getSlot4().equals(cardSlotId))
            cardSlotEquipment.setSlot4(0);

        save(cardSlotEquipment);
    }

    public void updateCardSlots(Player player, List<Integer> cardSlotItems) {
        Pocket pocket = player.getPocket();
        CardSlotEquipment cardSlotEquipment = findById(player.getCardSlotEquipment().getId());

        PlayerPocket item = playerPocketService.getItemAsPocket((long) cardSlotItems.get(0), pocket);
        cardSlotEquipment.setSlot1(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) cardSlotItems.get(1), pocket);
        cardSlotEquipment.setSlot2(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) cardSlotItems.get(2), pocket);
        cardSlotEquipment.setSlot3(item == null ? 0 : item.getId().intValue());

        item = playerPocketService.getItemAsPocket((long) cardSlotItems.get(3), pocket);
        cardSlotEquipment.setSlot4(item == null ? 0 : item.getId().intValue());

        save(cardSlotEquipment);
    }

    public List<Integer> getEquippedCardSlots(Player player) {
        List<Integer> result = new ArrayList<>();

        CardSlotEquipment cardSlotEquipment = findById(player.getCardSlotEquipment().getId());

        result.add(cardSlotEquipment.getSlot1());
        result.add(cardSlotEquipment.getSlot2());
        result.add(cardSlotEquipment.getSlot3());
        result.add(cardSlotEquipment.getSlot4());

        return result;
    }
}
