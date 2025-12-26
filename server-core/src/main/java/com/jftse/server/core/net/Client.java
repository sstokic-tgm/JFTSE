package com.jftse.server.core.net;

/**
 * Base type representing a remote peer connected to the server.
 * <p>
 * A {@code Client} is the high-level, domain-facing representation of a connected endpoint.
 * It typically stores identification/state used by the server (e.g. account, session, room),
 * while the underlying transport details live in the associated {@link Connection}.
 * </p>
 *
 * <h2>Connection relationship</h2>
 * <p>
 * This class participates in a strongly-typed, bidirectional association with {@link Connection}.
 * The generic constraint ensures that a concrete client type can only be paired with its
 * corresponding connection type at compile time.
 * </p>
 *
 * <h2>Endpoint information</h2>
 * <p>
 * {@link #ip} and {@link #port} describe the remote endpoint as observed by the server.
 * They may be populated from the transport layer (e.g. Netty's remote address) during connection setup.
 * </p>
 *
 * <h2>Thread-safety</h2>
 * <p>
 * This class is a simple data holder and is not synchronized. If instances are mutated or read
 * across multiple threads, external coordination is required.
 * </p>
 *
 * @param <T> the concrete connection type associated with this client
 * @see Connection
 */
public abstract class Client<T extends Connection<? extends Client<T>>> {
    /**
     * Transport connection backing this client.
     * Typically set during accept/handshake/bootstrap.
     */
    protected T connection;

    /**
     * Remote peer IP address (string form).
     */
    protected String ip;
    /**
     * Remote peer port number.
     */
    protected int port;

    /**
     * Returns the transport connection associated with this client.
     *
     * @return the connection instance, or {@code null} if not attached yet
     */
    public T getConnection() {
        return connection;
    }

    /**
     * Attaches a transport connection to this client.
     * <p>
     * Usually called once during connection bootstrap.
     * </p>
     *
     * @param connection a compatible connection instance
     */
    public void setConnection(T connection) {
        this.connection = connection;
    }

    /**
     * Returns the remote peer IP address (string form).
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets the remote peer IP address (string form).
     *
     * @param ip remote IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Returns the remote peer port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the remote peer port.
     *
     * @param port remote port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
