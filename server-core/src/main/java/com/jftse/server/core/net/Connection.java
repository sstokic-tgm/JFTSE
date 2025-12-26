package com.jftse.server.core.net;

import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.protocol.CompositePacket;
import com.jftse.server.core.protocol.IPacket;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base transport abstraction for a remote network connection.
 * <p>
 * A {@code Connection} encapsulates the low-level transport state (Netty channel context, channel id,
 * remote address) and provides core helpers such as sending packets and closing the channel.
 * Higher-level/game-specific state should live in the corresponding {@link Client}.
 * </p>
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>{@link #setChannelHandlerContext(ChannelHandlerContext)} is typically called when the channel becomes active.</li>
 *   <li>{@link #setClient(Client)} is typically assigned once the connection is "fully established" (e.g. after handshake/login).</li>
 *   <li>Concrete connection types may expose an {@code update(...)} method that is driven by the server's main loop (e.g. {@link com.jftse.server.core.ServerLoop}).</li>
 *   <li>{@link #wantsToCloseConnection()} sets a shared "closing" flag that update loops / pumps can check to stop processing.</li>
 * </ul>
 *
 * <h2>Typed pairing</h2>
 * <p>
 * The generic bounds enforce a compile-time safe bidirectional association between a concrete connection
 * type and its matching client type.
 * </p>
 *
 * <h2>Thread-safety</h2>
 * <p>
 * The connection may be interacted with from Netty event-loop threads and/or server update threads
 * depending on the concrete implementation. This base class provides only minimal atomic state
 * ({@link #getIsClosingConnection}); other access is not synchronized.
 * </p>
 *
 * @param <T>
 *           the client type associated with this connection
 *
 * @see Client
 * @see IPacket
 * @see CompositePacket
 * @see ServerType
 */
public abstract class Connection<T extends Client<? extends Connection<T>>> {
    /**
     * Associated client instance (may be {@code null} until fully established).
     */
    protected T client;
    /**
     * Netty channel context (available after {@link #setChannelHandlerContext(ChannelHandlerContext)}).
     */
    protected ChannelHandlerContext ctx;
    /**
     * Netty channel id for this connection.
     */
    protected ChannelId id;
    /**
     * Cached remote TCP address (resolved from the Netty channel).
     */
    protected InetSocketAddress remoteAddress;

    /**
     * Flag indicating the connection is in the process of closing (or should be closed).
     * Concrete connections that perform periodic updates should check this flag and stop processing when set.
     */
    protected AtomicBoolean isClosingConnection = new AtomicBoolean(false);

    /**
     * Key used to decrypt incoming packets (protocol-specific).
     */
    protected final int decryptionKey;
    /**
     * Key used to encrypt outgoing packets (protocol-specific).
     */
    protected final int encryptionKey;
    /**
     * Server type this connection belongs to (e.g. game server, auth server).
     */
    protected final ServerType serverType;

    protected Connection(final int decryptionKey, final int encryptionKey, final ServerType serverType) {
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
        this.serverType = serverType;
    }

    /**
     * Returns the remote TCP address for this connection.
     * <p>
     * The address is cached after the first resolution from the Netty channel.
     * </p>
     *
     * @return remote {@link InetSocketAddress}, or {@code null} if the Netty context is not set
     */
    public InetSocketAddress getRemoteAddressTCP() {
        if (ctx == null)
            return null;

        if (remoteAddress == null)
            remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

        return remoteAddress;
    }

    /**
     * Convenience method for logging/debugging.
     *
     * @return string form of the remote address, or {@code "unknown"} if unavailable
     */
    public String getIPString() {
        InetSocketAddress address = getRemoteAddressTCP();
        return address != null ? address.toString() : "unknown";
    }

    /**
     * Closes the underlying Netty channel.
     *
     * @return a {@link ChannelFuture} that completes when the close operation finishes
     */
    public ChannelFuture close() {
        return ctx.close();
    }

    /**
     * Marks this connection as wanting to close.
     * <p>
     * This does not forcibly close the Netty channel by itself; it is a cooperative signal for
     * update loops / packet pumps to stop processing and/or initiate shutdown.
     * </p>
     */
    public void wantsToCloseConnection() {
        isClosingConnection.set(true);
    }

    public final int getDecryptionKey() {
        return decryptionKey;
    }

    public final int getEncryptionKey() {
        return encryptionKey;
    }

    /**
     * Attaches the Netty channel context for this connection and caches the channel id + remote address.
     * Typically invoked from a Netty handler on {@code channelActive}.
     *
     * @param ctx netty channel context
     */
    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.id = ctx.channel().id();
        this.remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    }

    /**
     * Returns the Netty channel id for this connection.
     */
    public ChannelId getId() {
        return id;
    }

    /**
     * Attaches the associated client to this connection.
     *
     * @param client client instance (domain representation)
     */
    public void setClient(T client) {
        this.client = client;
    }

    /**
     * Returns the associated client instance.
     *
     * @return client or {@code null} if not established yet
     */
    public T getClient() {
        return this.client;
    }

    /**
     * Returns the cooperative closing flag.
     * <p>
     * Concrete implementations may poll this flag in their update loop.
     * </p>
     *
     * @return atomic boolean indicating whether the connection is closing
     */
    public AtomicBoolean getIsClosingConnection() {
        return isClosingConnection;
    }

    /**
     * Returns the server type this connection belongs to.
     *
     * @return server type
     */
    public ServerType getServerType() {
        return serverType;
    }

    /**
     * Sends one or more packets over the TCP channel.
     * <p>
     * If multiple packets are provided, they are wrapped into a {@link CompositePacket} and flushed as one write.
     * </p>
     *
     * @param packets packets to send (must contain at least one packet)
     * @return a {@link ChannelFuture} for the write/flush operation
     * @throws IllegalArgumentException if {@code packets} is {@code null} or empty
     */
    public ChannelFuture sendTCP(IPacket... packets) {
        if (packets == null || packets.length == 0)
            throw new IllegalArgumentException("Packet cannot be null.");

        IPacket toSend = new CompositePacket(packets);
        return ctx.writeAndFlush(toSend);
    }
}
