package com.jftse.emulator.server.core.handler.anticheat;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.ModuleService;
import com.jftse.emulator.server.database.model.anticheat.Module;
import com.jftse.emulator.server.networking.packet.Packet;

public class ACClientModuleHandler extends AbstractHandler {
    private Packet packet;

    private String moduleName;

    private final ModuleService moduleService;

    public ACClientModuleHandler() {
        this.moduleService = ServiceManager.getInstance().getModuleService();
    }

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        moduleName = packet.readUnicodeString();
        return true;
    }

    @Override
    public void handle() {
        Packet moduleAnswer = new Packet((char) (packet.getPacketId() + 1));

        Module module = moduleService.findModuleByName(moduleName);
        if (module == null) {
            module = new Module();
            module.setName(moduleName);
            module.setBlock(false);
            module = moduleService.save(module);

            moduleAnswer.write(0);
        }
        else {
            if (module.getBlock())
                moduleAnswer.write(1);
            else
                moduleAnswer.write(0);
        }
        connection.sendTCP(moduleAnswer);
    }
}
