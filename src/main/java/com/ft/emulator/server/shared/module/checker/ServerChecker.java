package com.ft.emulator.server.shared.module.checker;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

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
            log.error("SocketTimeoutException " + host + ":" + port + ". " + exception.getMessage());
        } catch (IOException exception) {
            log.error("IOException - Unable to connect to " + host + ":" + port + ". " + exception.getMessage());
        }
        return isAlive;
    }
}