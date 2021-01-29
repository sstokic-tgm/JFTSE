package com.ft.emulator.server.shared.module.checker;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.*;

@Log4j2
public abstract class ServerChecker {

    protected boolean isAlive(String host, int port) {
        boolean isAlive = false;
        byte[] readBuffer = new byte[1024];

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        Socket socket = new Socket();

        int timeout = 10000;
        try {
            socket.connect(socketAddress, timeout);

            int bytesRead = socket.getInputStream().read(readBuffer);
            if (bytesRead == -1)
                throw new SocketException("Connection is closed.");

            Packet requestDisconnectPacket = new Packet(PacketID.C2SDisconnectRequest);
            socket.getOutputStream().write(requestDisconnectPacket.getRawPacket());

            bytesRead = socket.getInputStream().read(readBuffer);
            if (bytesRead == -1)
                throw new SocketException("Connection is closed.");

            socket.close();

            isAlive = true;

        } catch (SocketTimeoutException exception) {
            log.error("SocketTimeoutException " + host + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            log.error("IOException " + host + ":" + port + ". " + exception.getMessage());
        }
        return isAlive;
    }
}