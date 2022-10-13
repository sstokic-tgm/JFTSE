package com.jftse.server.core.service.impl;

import com.jftse.entities.database.model.anticheat.Module;
import com.jftse.entities.database.repository.anticheat.ModuleRepository;
import com.jftse.server.core.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ModuleServiceImpl implements ModuleService {
    private final ModuleRepository moduleRepository;

    @Override
    public Module save(Module module) {
        return moduleRepository.save(module);
    }

    @Override
    public Module findModuleByName(String name) {
            Optional<Module> module = moduleRepository.findModuleByName(name);
            return module.orElse(null);
    }

    @Override
    public void remove(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }
}