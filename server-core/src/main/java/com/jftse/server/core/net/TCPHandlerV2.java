package com.jftse.server.core.net;

import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.shared.packets.SMSGDisconnectMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

/**
 * Base Netty inbound handler for JFTSE TCP connections.
 * <p>
 * This handler wires Netty channel lifecycle events to your connection abstraction:
 * it retrieves the {@link Connection} instance from the channel attributes, attaches the
 * {@link ChannelHandlerContext}, and delegates to subclass hooks for higher-level behavior
 * (connected/disconnected/packet/exception).
 * </p>
 *
 * <h2>How the Connection is resolved</h2>
 * <p>
 * Each channel is expected to have its {@link Connection} instance stored as a Netty channel attribute,
 * keyed by {@link #CONNECTION_ATTRIBUTE_KEY}. Typically, a pipeline initializer creates the connection
 * and sets it on the channel before this handler runs.
 * </p>
 *
 * <h2>Disconnection semantics</h2>
 * <p>
 * {@link #channelInactive(ChannelHandlerContext)} marks the connection as closing via
 * {@link Connection#wantsToCloseConnection()} and only calls {@link #disconnected(Connection)}
 * if {@link Connection#getClient()} is non-null. This avoids "disconnected" callbacks for half-open /
 * not-yet-established sessions (e.g. failed handshake).
 * </p>
 *
 * <h2>Packet dispatch helper</h2>
 * <p>
 * {@link #packetProcessed(Connection, IPacket)} provides a standard packet dispatch path using
 * {@link PacketRegistry}. Subclasses can choose to call it directly from {@link #packetReceived(Connection, IPacket)}
 * or implement custom routing/filters before dispatch.
 * </p>
 *
 * <h2>Exception handling</h2>
 * <p>
 * Exceptions are logged. If the error is a {@link DataAccessException}, a disconnect message is attempted
 * before closing the channel.
 * </p>
 *
 * @param <T> the concrete connection type handled by this Netty handler
 * @see Connection
 * @see Client
 * @see IPacket
 * @see PacketHandler
 * @see PacketRegistry
 */
public abstract class TCPHandlerV2<T extends Connection<? extends Client<T>>> extends SimpleChannelInboundHandler<IPacket> {
    /**
     * Netty channel attribute key used to fetch the {@link Connection} instance for the current channel.
     */
    protected final AttributeKey<T> CONNECTION_ATTRIBUTE_KEY;

    private final Logger log = LogManager.getLogger(getClass());

    /**
     * Creates a handler that resolves its connection instance from a Netty channel attribute.
     *
     * @param connectionAttributeKey attribute key under which the {@link Connection} is stored
     */
    protected TCPHandlerV2(final AttributeKey<T> connectionAttributeKey) {
        this.CONNECTION_ATTRIBUTE_KEY = connectionAttributeKey;
    }

    /**
     * Called by Netty when the channel becomes active.
     * <p>
     * Attaches the {@link ChannelHandlerContext} to the connection and delegates to {@link #connected(Connection)}.
     * </p>
     *
     * @param ctx ChannelHandlerContext for the active channel
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();
        if (connection == null) {
            log.warn("({}) Channel active but no connection found in channel attributes. Closing channel.", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        connection.setChannelHandlerContext(ctx);
        log.info("({}) Channel Active", connection.getIPString());
        connected(connection);
    }

    /**
     * Called by Netty when an {@link IPacket} arrives.
     * Delegates to {@link #packetReceived(Connection, IPacket)}.
     *
     * @param ctx    ChannelHandlerContext for the channel
     * @param packet received packet
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket packet) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();
        if (connection == null) {
            log.warn("({}) Packet received but no connection in channel attributes. Dropping packet and closing.", ctx.channel().remoteAddress());
            ctx.close();
            return;
        }

        packetReceived(connection, packet);
    }

    /**
     * Called by Netty when the channel becomes inactive.
     * <p>
     * Marks the connection as closing and calls {@link #disconnected(Connection)} only if the connection
     * has an associated client (i.e. session was fully established).
     * </p>
     *
     * @param ctx ChannelHandlerContext for the inactive channel
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();
        if (connection == null) {
            log.warn("({}) Channel inactive but no connection found in channel attributes.", ctx.channel().remoteAddress());
            return;
        }

        log.info("({}) Channel Inactive", connection.getIPString());
        // channelInactive is only called once anyway
        connection.wantsToCloseConnection();
        disconnected0(connection);
    }

    /**
     * Called by Netty when an exception occurs in the pipeline.
     * <p>
     * Logs common network failures and delegates to {@link #exceptionCaught(Connection, Throwable)} for custom behavior.
     * If the exception is a {@link DataAccessException}, a disconnect packet is attempted before closing the channel.
     * </p>
     *
     * @param ctx   ChannelHandlerContext where the exception occurred
     * @param cause caught Throwable
     */
    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();
        final String remoteAddress = connection != null ? connection.getIPString() : String.valueOf(ctx.channel().remoteAddress());

        boolean isReadTimeout = cause instanceof ReadTimeoutException;

        Throwable root = rootCause(cause);
        boolean isIoError = root instanceof java.io.IOException;
        boolean isResetLike = isIoError && looksLikeReset(root);

        boolean isCodecError = cause instanceof DecoderException || cause instanceof EncoderException ||
                root instanceof DecoderException || root instanceof EncoderException;

        boolean isDBError = cause instanceof DataAccessException || root instanceof DataAccessException;

        if (isReadTimeout) {
            log.warn("({}) Read timeout.", remoteAddress);
        } else if (isResetLike) {
            log.warn("({}) Connection lost: {}", remoteAddress, safeMsg(root));
        } else if (isIoError) {
            log.warn("({}) I/O error: {}", remoteAddress, safeMsg(root), cause);
        } else {
            log.error("({}) exceptionCaught: {}", remoteAddress, safeMsg(cause), cause);
        }

        try {
            exceptionCaught(connection, cause);
        } catch (Throwable t) {
            // never let subclass errors keep the channel alive or hide the original issue
            log.error("({}) exceptionCaught hook threw: {}", remoteAddress, safeMsg(t), t);
        }

        boolean shouldClose = isReadTimeout || isResetLike || isCodecError || isIoError || isDBError;

        if (isDBError && connection != null && ctx.channel().isActive()) {
            SMSGDisconnectMessage dcPacket = SMSGDisconnectMessage.builder()
                    .result((byte) 0)
                    .build();

            connection.sendTCP(dcPacket).addListener((f) -> {
                if (!f.isSuccess()) {
                    log.warn("({}) Failed to send disconnect packet to client before closing connection: {}", remoteAddress, safeMsg(f.cause()), f.cause());
                }
                connection.close();
            });
            return;
        }

        if (shouldClose) {
            if (connection != null) {
                connection.close();
            } else {
                ctx.close();
            }
        }
    }

    /**
     * Traverses the cause chain to find the root cause of an exception,
     * which is often more informative for network errors.
     *
     * @param t the throwable to analyze
     * @return the root cause throwable
     */
    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur;
    }

    /**
     * Heuristic to determine if an IOException is likely a connection reset or similar network failure,
     * based on common message patterns. This is not foolproof but can help reduce noise in
     * logs by categorizing expected disconnects separately from other I/O errors.
     *
     * @param t the throwable to analyze
     * @return true if the message suggests a connection reset or similar network issue, false otherwise
     */
    private boolean looksLikeReset(Throwable t) {
        if (t instanceof java.nio.channels.ClosedChannelException) return true;
        if (t instanceof java.net.NoRouteToHostException) return true;
        if (t instanceof java.net.SocketTimeoutException) return true;
        if (t instanceof java.net.SocketException) return true;

        String msg = t.getMessage();
        if (msg == null) return false;

        return msg.contains("Connection reset")
                || msg.contains("Broken pipe")
                || msg.contains("Connection timed out")
                || msg.contains("No route to host")
                || msg.contains("Connection refused");
    }

    /**
     * Safely extracts a message from a throwable for logging, falling back to the class name if the message is null.
     *
     * @param t the throwable to extract the message from
     * @return the throwable's message if available, otherwise the simple class name of the throwable
     */
    private String safeMsg(Throwable t) {
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    /**
     * Internal disconnect guard.
     * Only calls {@link #disconnected(Connection)} if the connection has a non-null client,
     * indicating the session was fully established.
     *
     * @param connection disconnecting connection
     */
    protected void disconnected0(T connection) {
        if (connection.getClient() != null) { // only call disconnected if the connection was fully established
            log.info("({}) Client disconnected", connection.getIPString());
            disconnected(connection);
        }
    }

    /**
     * Helper for standard packet dispatch via {@link PacketRegistry}.
     * <p>
     * Subclasses typically call this from {@link #packetReceived(Connection, IPacket)} after any
     * protocol-specific validation, throttling, queueing, or decoding.
     * </p>
     *
     * @param connection connection that received the packet
     * @param packet     received packet
     */
    protected void packetProcessed(T connection, IPacket packet) {
        try {
            PacketHandler<T, IPacket> handler = PacketRegistry.getHandler(packet.getPacketId());
            if (handler != null) {
                handler.handle(connection, packet);
            } else {
                log.warn("No handler for packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId());
                handlerNotFound(connection, packet);
            }
        } catch (Exception e) {
            log.error("Error processing packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId(), e);
        }
    }

    /**
     * Hook invoked when a packet has no registered handler.
     * Default implementation does nothing; override for diagnostics or protocol enforcement.
     *
     * @param connection connection that received the unhandled packet
     * @param packet     unhandled packet
     */
    protected void handlerNotFound(T connection, IPacket packet) {
        // Default implementation does nothing. Subclasses can override to provide custom behavior.
    }

    /**
     * Called when the transport becomes active (channel established).
     */
    protected abstract void connected(T connection);

    /**
     * Called when the established session disconnects.
     */
    protected abstract void disconnected(T connection);

    /**
     * Called when a packet is received on the channel.
     */
    protected abstract void packetReceived(T connection, IPacket packet);

    /**
     * Called after Netty reports an exception. Implementations may add cleanup or domain-specific actions.
     *
     * @throws Exception if you want to propagate an error to Netty's pipeline
     */
    protected abstract void exceptionCaught(T connection, Throwable cause) throws Exception;
}
