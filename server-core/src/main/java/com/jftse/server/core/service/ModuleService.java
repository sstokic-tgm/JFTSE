package com.jftse.server.core.service;

import com.jftse.entities.database.model.anticheat.Module;

public interface ModuleService {
    Module save(Module module);

    Module findModuleByName(String name);

    void remove(Long moduleId);
}
