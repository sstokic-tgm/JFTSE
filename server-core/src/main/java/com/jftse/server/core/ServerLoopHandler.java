package com.jftse.server.core;

/**
 * Callback interface invoked by {@link ServerLoop} each server tick.
 * <p>Implementations typically drive game/server state forward, such as: </p>
 * <ul>
 *     <li>updating active connections/sessions</li>
 *     <li>processing scheduled tasks</li>
 *     <li>running managers (e.g., world, match, chat, etc.)</li>
 * </ul>
 *
 * <h2>Diff semantics</h2>
 * <p>
 * {@code diff} is the elapsed time in <b>milliseconds</b> since the previous tick (as measured by {@link com.jftse.server.core.util.Time}).
 * This allows implementations to perform time-based updates, ensuring consistent behavior regardless of tick rate variations.
 * </p>
 */
public interface ServerLoopHandler {
    /**
     * Called once per server tick.
     *
     * @param diff elapsed time since last tick, in milliseconds
     */
    void update(long diff);
}

