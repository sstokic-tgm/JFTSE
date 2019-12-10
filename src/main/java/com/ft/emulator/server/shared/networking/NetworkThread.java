package com.ft.emulator.server.shared.networking;

import com.ft.emulator.server.shared.module.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

public abstract class NetworkThread {

    protected int port;

    protected ServerSocket serverSocket;

    protected InputStream inputStream;
    protected OutputStream outputStream;

    protected boolean stopped = false;

    protected NetworkThread(int port) {

        this.port = port;
    }

    public abstract void listenerThread();
    public abstract void receivingThread(Client client);

    public void start() throws IOException, Exception {

        try {

	    new Thread(() -> listenerThread()).start();
	}
        catch(Exception e) {

            stop();
            throw e;
	}
    }

    public void stop() {

        this.stopped = true;

        try {

	    this.serverSocket.close();
	}
        catch(IOException ioe) { }
    }
}