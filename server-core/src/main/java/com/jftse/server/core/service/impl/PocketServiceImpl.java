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
public class PocketServiceImpl implements PocketService {
    private final PocketRepository pocketRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Pocket save(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    @Override
    @Transactional(readOnly = true)
    public Pocket findById(Long pocketId) {
        Optional<Pocket> pocket = pocketRepository.findById(pocketId);
        return pocket.orElse(null);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Pocket incrementPocketBelongings(Pocket pocket) {
        return incrementPocketBelongings(pocket.getId());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Pocket incrementPocketBelongings(Long pocketId) {
        Optional<Pocket> tmpPocket = pocketRepository.findById(pocketId);
        if (tmpPocket.isPresent()) {
            Pocket pocket = tmpPocket.get();

            pocket.setBelongings(pocket.getBelongings() + 1);
            return save(pocket);
        }
        else {
            return null;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Pocket decrementPocketBelongings(Pocket pocket) {
        return decrementPocketBelongings(pocket.getId());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Pocket decrementPocketBelongings(Long pocketId) {
        Optional<Pocket> tmpPocket = pocketRepository.findById(pocketId);
        if (tmpPocket.isPresent()) {
            Pocket pocket = tmpPocket.get();

            pocket.setBelongings(pocket.getBelongings() - 1);
            return save(pocket);
        }
        else {
            return null;
        }
    }
}
