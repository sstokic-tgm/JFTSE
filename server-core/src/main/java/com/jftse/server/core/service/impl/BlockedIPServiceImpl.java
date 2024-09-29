package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.ServerType;
import com.jftse.entities.database.model.log.BlockedIP;
import com.jftse.entities.database.repository.log.BlockedIPRepository;
import com.jftse.server.core.service.BlockedIPService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class BlockedIPServiceImpl implements BlockedIPService {
    private final BlockedIPRepository blockedIPRepository;

    @Override
    public BlockedIP save(BlockedIP blockedIP) {
        return blockedIPRepository.save(blockedIP);
    }

    @Override
    public void remove(BlockedIP blockedIP) {
        blockedIPRepository.deleteById(blockedIP.getId());
    }

    @Override
    public Optional<BlockedIP> findBlockedIPByIpAndServerType(String ip, ServerType serverType) {
        return blockedIPRepository.findBlockedIPByIpAndServerType(ip, serverType);
    }

    @Override
    public List<BlockedIP> findAll() {
        return blockedIPRepository.findAll();
    }
}
