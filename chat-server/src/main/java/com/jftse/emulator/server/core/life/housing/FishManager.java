package com.jftse.emulator.server.core.life.housing;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CFishMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CFishStopPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CInitFishPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CInitFishWithDetailsPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@Getter
@Setter
public class FishManager {
    @Getter
    private static FishManager instance;

    private ScheduledExecutorService executor;
    private SecureRandom random;

    private final ConcurrentHashMap<Short, List<Fish>> fishesByRoomId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Short, Boolean> scheduledRooms = new ConcurrentHashMap<>();

    public static final int MAX_FISH_SPAWN_COUNT_PER_ROOM = 7;
    public static final int FISH_SPAWN_COUNT = 2;

    public static final float MIN_SPAWN_TIME = 3.0f; // seconds
    public static final float MAX_SPAWN_TIME = 4.0f; // seconds
    public static final float NORMAL_SPEED_1 = 2.0f;
    public static final float NORMAL_SPEED_2 = 3.0f;
    public static final float ATTACK_SPEED = 4.0f;
    public static final float SCARED_SPEED = 10.0f;
    public static final float DISENGAGE_SPEED = 10.0f;
    public static final float NORMAL_TURNING_SPEED = 3.0f; // degrees
    public static final float DISENGAGE_TURNING_SPEED = 6.0f; // degrees

    public static final int BITE_SUCCESS_CHANCE = 40; // percent
    public static final float BITE_ATTEMPT_INTERVAL = 2.0f; // seconds
    public static final float FISH_LENGTH = 6.43f;
    public static final float ATTACK_RADIUS = 4.0f; // related to fish length
    public static final float BAIT_TOO_CLOSE_RADIUS = 6.0f; // related to fish length
    public static final float FISH_FRIGHTENED_RADIUS = 10.0f; // related to fish length
    public static final float BAIT_DETECTION_RADIUS = 10.0f; // related to fish length

    public static final float RANDOM_POSITION_RADIUS = 10.0f; // 5.0 is 1 square
    public static final int MOVEMENT_CHANCE_1 = 40; // percent
    public static final int MOVEMENT_CHANCE_2 = 25; // percent

    public static final int MAX_FISH_ID = 31;

    private static final List<Point> FISH_SPAWN_POSITIONS = List.of(
            new Point(220, -320),
            new Point(200, -310),
            new Point(180, -320),
            new Point(230, -340),
            new Point(240, -350),
            new Point(220, -350),
            new Point(310, -340),
            new Point(320, -350)
    );

    @PostConstruct
    public void init() {
        instance = this;

        executor = Executors.newScheduledThreadPool(5);
        random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        clearAllFishes();

        log.info(this.getClass().getSimpleName() + " shutdown");
    }

    public void registerRoom(short roomId) {
        if (fishesByRoomId.containsKey(roomId) && fishesByRoomId.get(roomId).size() > MAX_FISH_SPAWN_COUNT_PER_ROOM) {
            log.warn("Cannot add fish to room " + roomId + ": maximum fish count reached");
            return;
        }
        fishesByRoomId.computeIfAbsent(roomId, k -> new ArrayList<>()).add(spawnFish(roomId));

        List<Fish> fishes = fishesByRoomId.get(roomId);
        for (int j = 0; j < FISH_SPAWN_COUNT - 1; j++) {
            if (fishes.size() < MAX_FISH_SPAWN_COUNT_PER_ROOM) {
                fishes.add(spawnFish(roomId));
            }
        }

        if (!scheduledRooms.containsKey(roomId)) {
            scheduledRooms.put(roomId, true);
            executor.scheduleAtFixedRate(() -> tick(roomId), 0, 1, TimeUnit.SECONDS);
        }
    }

    private Fish spawnFish(short roomId) {
        List<Fish> fishes = fishesByRoomId.getOrDefault(roomId, new ArrayList<>());

        short nextFishId = safeGetNextFishId(fishes);
        if (nextFishId == -1) {
            // pick a random one(0-31), we resync
            nextFishId = (short) (random.nextInt(MAX_FISH_ID + 1));
        }

        final short fishId = nextFishId;
        Fish fish = fishes.stream()
                .filter(f -> f.getId() == fishId)
                .findFirst()
                .orElseGet(Fish::new);
        fish.setId(fishId);
        fish.setModel((byte) 0);

        // pick a random spawn position
        Point spawnPosition = FISH_SPAWN_POSITIONS.get(random.nextInt(FISH_SPAWN_POSITIONS.size()));

        fish.setX((float) spawnPosition.x);
        fish.setY((float) spawnPosition.y);
        fish.setSpawnX((float) spawnPosition.x);
        fish.setSpawnY((float) spawnPosition.y);

        fish.setZ(-6.5f);
        fish.setRotation(0.0f);
        fish.setSpeed(NORMAL_SPEED_1);
        fish.setTurningSpeed(NORMAL_TURNING_SPEED);

        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);

        S2CInitFishPacket initFish = new S2CInitFishPacket(fish);
        broadcast(initFish, clients);

        final float spawnTime = MIN_SPAWN_TIME + random.nextFloat() * (MAX_SPAWN_TIME - MIN_SPAWN_TIME);
        executor.schedule(() -> {
            fish.setModel((byte) 1);
            S2CInitFishWithDetailsPacket initFishDetails = new S2CInitFishWithDetailsPacket(
                    fish.getId(), fish.getModel(), (byte) fish.getState().getValue(),
                    0.0f, fish.getZ(), 0.0f, fish.getX(), fish.getY(),
                    fish.getDirX(), fish.getDirY(), fish.getDestX(), fish.getDestY(),
                    fish.getSpeed(), 0.0f, 0.0f, (short) 0
            );

            broadcast(initFishDetails, clients);
        }, (int) spawnTime, TimeUnit.SECONDS);

        log.info("Spawned fish with ID " + fishId + " in room " + roomId);
        return fish;
    }

    private short safeGetNextFishId(List<Fish> fishes) {
        List<Short> fishIds = fishes.stream().map(Fish::getId).toList();
        short nextId = 0;
        int attempts = 0;
        while (fishIds.contains(nextId)) {
            nextId++;
            attempts++;
            if (nextId > MAX_FISH_ID) {
                nextId = 0;
            }

            if (attempts > MAX_FISH_ID) {
                nextId = -1;
                break;
            }
        }
        return nextId;
    }

    public void removeFish(short roomId, Fish fish) {
        List<Fish> fishes = fishesByRoomId.get(roomId);
        if (fishes != null) {
            fishes.remove(fish);
            if (fishes.isEmpty()) {
                fishesByRoomId.remove(roomId);
                scheduledRooms.remove(roomId);
            }
        }
    }

    public List<Fish> getFishes(short roomId) {
        return fishesByRoomId.getOrDefault(roomId, new ArrayList<>());
    }

    public void clearFishes(short roomId) {
        fishesByRoomId.remove(roomId);
        scheduledRooms.remove(roomId);
    }

    public void clearAllFishes() {
        fishesByRoomId.clear();
        scheduledRooms.clear();
    }

    private void tick(short roomId) {
        final List<Fish> fishes = fishesByRoomId.get(roomId);
        if (fishes == null || fishes.isEmpty()) {
            scheduledRooms.remove(roomId);
            return;
        }

        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);
        long currentTime = System.currentTimeMillis();

        for (Fish fish : fishes) {
            if (fish == null) continue;

            fish.updateAliveTime();
            float deltaSeconds = (currentTime - fish.getLastUpdate()) / 1000.0f;
            fish.setLastUpdate(currentTime);

            switch (fish.getState()) {
                case MOVING -> {
                    float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
                    float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;

                    // Clamp to spawn radius
                    float distFromOrigin = distance(newX, newY, fish.getSpawnX(), fish.getSpawnY());
                    if (distFromOrigin > RANDOM_POSITION_RADIUS) {
                        fish.stop();
                        S2CFishStopPacket stopPacket = new S2CFishStopPacket(fish.getId(), (byte) fish.getState().getValue());
                        broadcast(stopPacket, clients);
                        continue;
                    }

                    fish.updatePosition(newX, newY);

                    if (fish.hasReachedDestination(0.8f)) {
                        fish.stop();
                        S2CFishStopPacket stopPacket = new S2CFishStopPacket(fish.getId(), (byte) fish.getState().getValue());
                        broadcast(stopPacket, clients);
                    }
                }
                case IDLE -> {
                    if (random.nextInt(100) < MOVEMENT_CHANCE_1) {
                        float[] next = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
                        fish.moveTo(next[0], next[1], NORMAL_SPEED_1);

                        S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) fish.getState().getValue(), fish.getX(), fish.getY(), NORMAL_SPEED_1);
                        broadcast(movePacket, clients);
                    }
                }
            }
            log.debug(fish.debugString());
        }

        // handle spawn logic
        if (fishes.size() < MAX_FISH_SPAWN_COUNT_PER_ROOM && random.nextInt(101) < 20) { // 20% chance to spawn a new fish
            final float spawnTime = MIN_SPAWN_TIME + random.nextFloat() * (MAX_SPAWN_TIME - MIN_SPAWN_TIME);
            if (fishes.stream().noneMatch(f -> f.getLastUpdate() + spawnTime * 1000 > currentTime)) {
                Fish newFish = spawnFish(roomId);
                fishes.add(newFish);
            }
        }
    }

    private float[] getRandomDirectionFrom(float originX, float originY) {
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        float radius = RANDOM_POSITION_RADIUS * (0.5f + random.nextFloat() * 0.5f); // 50–100% of range
        float dx = (float) Math.cos(angle) * radius;
        float dy = (float) Math.sin(angle) * radius;
        return new float[] { originX + dx, originY + dy };
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void broadcast(Packet packet, List<FTClient> clients) {
        if (clients == null || clients.isEmpty()) {
            return;
        }

        for (FTClient client : clients) {
            FTConnection connection = client.getConnection();
            if (connection != null) {
                connection.sendTCP(packet);
            }
        }
    }
}
