package com.jftse.emulator.server.game.core.service;

import com.jftse.emulator.server.database.model.pocket.Pocket;
import com.jftse.emulator.server.database.repository.pocket.PocketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PocketService {
    private final PocketRepository pocketRepository;

    public Pocket save(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    public Pocket findById(Long pocketId) {
        Optional<Pocket> pocket = pocketRepository.findById(pocketId);
        return pocket.orElse(null);
    }

    public Pocket incrementPocketBelongings(Pocket pocket) {
        Optional<Pocket> tmpPocket = pocketRepository.findById(pocket.getId());
        if (tmpPocket.isPresent()) {
            pocket = tmpPocket.get();

            pocket.setBelongings(pocket.getBelongings() + 1);
            return save(pocket);
        }
        else {
            return pocket;
        }
    }

    public Pocket decrementPocketBelongings(Pocket pocket) {
        Optional<Pocket> tmpPocket = pocketRepository.findById(pocket.getId());
        if (tmpPocket.isPresent()) {
            pocket = tmpPocket.get();

            pocket.setBelongings(pocket.getBelongings() - 1);
            return save(pocket);
        }
        else {
            return pocket;
        }
    }
}
