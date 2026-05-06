package com.jftse.emulator.server.core.life.housing;

import com.jftse.server.core.util.GameTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fish {

    private short id;
    private byte model;
    private FishState state = FishState.IDLE;

    private float x, y, z;
    private float spawnX, spawnY;
    private float dirX, dirY;
    private float destX, destY;
    private float rotation;
    private float speed;
    private float turningSpeed;

    private long nextIdleMoveCheckTime = 0L;
    private boolean aggressiveMovementPattern = false;

    private long lastUpdate = 0;
    private long lastBiteTime = 0;
    private long aliveTime = 0;
    private long lastCorrectionTime = 0;
    private long lastActivityTime = GameTime.getGameTimeMS();

    private boolean bitBait = false;
    private short claimedPlayerPosition = -1;

    private Long rewardProductIndex;
    private int group = 0;

    public void updatePosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void moveTo(float destX, float destY, float speed) {
        this.destX = destX;
        this.destY = destY;
        this.speed = speed;
        updateDirection(destX, destY);
    }

    public void updateDirection(float destX, float destY) {
        float dx = destX - this.x;
        float dy = destY - this.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.001f) {
            this.dirX = dx / dist;
            this.dirY = dy / dist;
        } else {
            this.dirX = 0.0f;
            this.dirY = 0.0f;
        }

        updateRotation();
    }

    public void updateRotation() {
        this.rotation = (float) Math.atan2(this.dirY, this.dirX);
    }

    public void setState(FishState state) {
        if (this.state != state) {
            this.state = state;

            long now = GameTime.getGameTimeMS();
            if (state == FishState.BITING) {
                this.lastBiteTime = now;
            }

            if (state == FishState.ATTACKING ||
                    state == FishState.FRIGHTENED ||
                    state == FishState.BITING) {
                this.lastActivityTime = now;
            }
        }
    }

    public void updateAliveTime() {
        if (lastUpdate > 0) {
            this.aliveTime += GameTime.getGameTimeMS() - lastUpdate;
        }
    }

    public boolean hasReachedDestination(float threshold) {
        float dx = this.destX - this.x;
        float dy = this.destY - this.y;
        float distSq = dx * dx + dy * dy;

        if (distSq <= threshold * threshold) {
            return true;
        }

        float dot = (dx * dirX + dy * dirY);
        return dot <= 0;
    }

    public void stopMovement() {
        this.speed = 0.0f;
        this.dirX = 0.0f;
        this.dirY = 0.0f;
        this.destX = this.x;
        this.destY = this.y;
    }

    public void teleportTo(float x, float y, float z) {
        updatePosition(x, y);
        this.z = z;
        stopMovement();
        setState(FishState.IDLE);
    }

    public void resetToSpawn(float z, float speed, float turningSpeed) {
        this.x = this.spawnX;
        this.y = this.spawnY;
        this.z = z;
        this.speed = speed;
        this.turningSpeed = turningSpeed;
        this.dirX = 0.0f;
        this.dirY = 0.0f;
        this.destX = this.x;
        this.destY = this.y;
        this.rotation = 0.0f;
        this.bitBait = false;
        this.claimedPlayerPosition = -1;
        this.lastBiteTime = 0L;
        this.lastCorrectionTime = GameTime.getGameTimeMS();
        this.lastActivityTime = GameTime.getGameTimeMS();
        this.state = FishState.IDLE;
    }

    public float getFishingBarSpeed() {
        return switch (group) {
            case 1 -> 1.0f;
            case 2 -> 0.8f;
            case 3 -> 1.1f;
            default -> 1.3f;
        };
    }

    public String debugString() {
        long aliveMillis = aliveTime;
        long seconds = aliveMillis / 1000;
        long minutes = seconds / 60;
        String aliveFor = String.format("%d min %d sec", minutes, seconds % 60);

        return String.format(
                "Fish[id=%d, pos=(%.2f, %.2f), spawn=(%.2f, %.2f), dir=(%.2f, %.2f), speed=%.2f, state=%s, aliveFor=%s]",
                id, x, y, spawnX, spawnY, dirX, dirY, speed, state, aliveFor
        );
    }
}
