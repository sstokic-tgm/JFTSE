package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.core.manager.ServerManager;
import com.jftse.emulator.server.core.packet.packets.S2CServerNoticePacket;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerNoticeHandler extends AbstractHandler {
    private Packet packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        log.debug("[analysis purpose] received server notice packet: " + BitKit.toString(packet.getData(), 0, packet.getDataLength()));

        if (ServerManager.getInstance().isServerNoticeIsSet()) {
            S2CServerNoticePacket serverNoticePacket = new S2CServerNoticePacket(ServerManager.getInstance().getServerNoticeMessage());
            connection.sendTCP(serverNoticePacket);
            log.debug("[analysis purpose] sent server notice: " + ServerManager.getInstance().getServerNoticeMessage());
        }
    }
}
