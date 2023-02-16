package com.jftse.emulator.server.core.service;

import com.jftse.entities.database.model.anticheat.Module;
import com.jftse.entities.database.repository.anticheat.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ModuleService {
    private final ModuleRepository moduleRepository;

    public Module save(Module module) {
        return moduleRepository.save(module);
    }

    public Module findModuleByName(String name) {
            Optional<Module> module = moduleRepository.findModuleByName(name);
            return module.orElse(null);
    }

    public void remove(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }
}