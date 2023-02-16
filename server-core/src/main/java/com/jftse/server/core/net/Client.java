package com.jftse.server.core.net;

public abstract class Client<T extends Connection<? extends Client<T>>> {
    protected T connection;

    protected String ip;
    protected int port;

    public T getConnection() {
        return connection;
    }

    public void setConnection(T connection) {
        this.connection = connection;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
