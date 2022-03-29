package com.jftse.emulator.server.core.service;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.CardSlotEquipment;
import com.jftse.emulator.server.database.repository.player.CardSlotEquipmentRepository;
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

        cardSlotEquipment = save(cardSlotEquipment);
    }

    public void updateCardSlots(CardSlotEquipment cardSlotEquipment, List<Integer> cardSlotItems) {
        cardSlotEquipment = findById(cardSlotEquipment.getId());

        cardSlotEquipment.setSlot1(cardSlotItems.get(0));
        cardSlotEquipment.setSlot2(cardSlotItems.get(1));
        cardSlotEquipment.setSlot3(cardSlotItems.get(2));
        cardSlotEquipment.setSlot4(cardSlotItems.get(3));

        cardSlotEquipment = save(cardSlotEquipment);
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
