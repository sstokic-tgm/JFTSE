package com.jftse.emulator.server.core.life.housing;

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
    private long lastUpdate = 0;
    private long lastBiteTime = 0;
    private long aliveTime = 0;
    private boolean bitBait = false;
    private short claimedPlayerPosition = -1;

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
            this.dirX = 0;
            this.dirY = 0;
        }
        updateRotation();
    }

    public void updateRotation() {
        this.rotation = (float) Math.atan2(this.dirY, this.dirX);
    }

    public void setState(FishState state) {
        if (this.state != state) {
            this.state = state;
            if (state == FishState.BITING) {
                this.lastBiteTime = System.currentTimeMillis();
            }
        }
    }

    public void updateAliveTime() {
        if (lastUpdate > 0) {
            this.aliveTime += System.currentTimeMillis() - lastUpdate;
        }
    }

    public boolean hasReachedDestination(float threshold) {
        float dx = this.destX - this.x;
        float dy = this.destY - this.y;
        float distSq = dx * dx + dy * dy;
        if (distSq <= threshold * threshold) return true;

        float dot = (dx * dirX + dy * dirY);
        return dot <= 0;
    }

    public void stop() {
        this.speed = 0;
        setState(FishState.IDLE);
    }

    public void teleportTo(float x, float y, float z) {
        updatePosition(x, y);
        this.z = z;
        stop();
    }

    public void reset() {
        this.id = 0;
        this.model = 1;
        this.state = FishState.IDLE;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.dirX = 0;
        this.dirY = 0;
        this.destX = 0;
        this.destY = 0;
        this.rotation = 0;
        this.speed = 0;
        this.turningSpeed = 0;
        this.lastUpdate = System.currentTimeMillis();
        this.lastBiteTime = System.currentTimeMillis();
        this.aliveTime = 0;
        this.bitBait = false;
        this.claimedPlayerPosition = -1;
    }

    public String debugString() {
        long aliveMillis = aliveTime;
        long seconds = aliveMillis / 1000;
        long minutes = seconds / 60;
        String aliveFor = String.format("%d min %d sec", minutes, seconds % 60);

        return String.format("Fish[id=%d, pos=(%.2f, %.2f), dir=(%.2f, %.2f), speed=%.2f, state=%s, aliveFor=%s]",
                id, x, y, dirX, dirY, speed, state, aliveFor);
    }
}
