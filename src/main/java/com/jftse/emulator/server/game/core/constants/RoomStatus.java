package com.jftse.emulator.server.game.core.constants;

public class RoomStatus {
    public final static int Idle = 0;
    public final static int StartingGame = 1;
    public final static int StartCancelled = 2;
    public final static int InitializingGame = 4;
    public final static int AnimationSkipped = 5;
    public final static int Running = 6;
    public final static int NotRunning = -1;
}
