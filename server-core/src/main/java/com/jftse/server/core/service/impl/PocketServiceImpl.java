package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.pocket.PocketRepository;
import com.jftse.server.core.service.PocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class PocketServiceImpl implements PocketService {
    private final PocketRepository pocketRepository;

    @Override
    public Pocket save(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    @Override
    public Pocket findById(Long pocketId) {
        Optional<Pocket> pocket = pocketRepository.findById(pocketId);
        return pocket.orElse(null);
    }

    @Override
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

    @Override
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
