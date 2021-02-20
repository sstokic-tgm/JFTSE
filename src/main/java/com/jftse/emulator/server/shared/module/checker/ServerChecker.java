package com.jftse.emulator.server.shared.module.checker;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.*;

@Log4j2
public abstract class ServerChecker {

    protected boolean isAlive(String host, int port) {
        boolean isAlive = false;

        SocketAddress socketAddress = new InetSocketAddress(host, port);
        Socket socket = new Socket();

        int timeout = 10000;
        try {
            socket.connect(socketAddress, timeout);
            socket.close();

            isAlive = true;

        } catch (SocketTimeoutException exception) {
            log.error("SocketTimeoutException " + host + ":" + port + ". " + exception.getMessage(), exception);
        } catch (IOException exception) {
            log.error("IOException " + host + ":" + port + ". " + exception.getMessage(), exception);
        }
         finally {
            try {
                socket.close();
            } catch (IOException exception) {
                log.error("IOException " + host + ":" + port + ". " + exception.getMessage(), exception);
            }
        }
        return isAlive;
    }
}