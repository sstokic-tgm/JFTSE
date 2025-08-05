package com.jftse.emulator.server.core.life.housing;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.*;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

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
    private final ConcurrentLinkedDeque<Float[]> baitPositions = new ConcurrentLinkedDeque<>();

    public static final int MAX_FISH_SPAWN_COUNT_PER_ROOM = 10;
    public static final int FISH_SPAWN_COUNT = 4;

    public static final int INACTIVITY_TIMEOUT = 10; // minutes
    public static final float MIN_SPAWN_TIME = 6.0f; // seconds
    public static final float MAX_SPAWN_TIME = 8.0f; // seconds
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
    public static final float FRIGHTENED_RADIUS = 10.0f; // related to fish length
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
            executor.scheduleAtFixedRate(() -> tick(roomId), 5, 1, TimeUnit.SECONDS);
        }
    }

    public void registerBaitPosition(float x, float y) {
        baitPositions.offer(new Float[] { x, y });
    }

    public void removeBaitPosition(float x, float y) {
        baitPositions.removeIf(baitPosition -> baitPosition[0].equals(x) && baitPosition[1].equals(y));
    }

    // Modify spawnFish to avoid using the same spawn position more than once
    private Fish spawnFish(short roomId) {
        List<Fish> fishes = fishesByRoomId.getOrDefault(roomId, new ArrayList<>());

        short nextFishId = safeGetNextFishId(fishes);
        if (nextFishId == -1) {
            nextFishId = (short) (random.nextInt(MAX_FISH_ID + 1));
        }

        final short fishId = nextFishId;
        Fish fish = fishes.stream()
                .filter(f -> f.getId() == fishId)
                .findFirst()
                .orElseGet(Fish::new);
        fish.setId(fishId);
        fish.setModel((byte) 0);

        // Ensure fish doesn't spawn at same location
        Set<Point> usedSpawns = new HashSet<>();
        for (Fish f : fishes) {
            usedSpawns.add(new Point((int) f.getSpawnX(), (int) f.getSpawnY()));
        }

        Point spawnPosition = FISH_SPAWN_POSITIONS.stream()
                .filter(p -> !usedSpawns.contains(p))
                .findAny()
                .orElse(FISH_SPAWN_POSITIONS.get(random.nextInt(FISH_SPAWN_POSITIONS.size()))); // fallback if all used

        fish.setX((float) spawnPosition.x);
        fish.setY((float) spawnPosition.y);
        fish.setSpawnX((float) spawnPosition.x);
        fish.setSpawnY((float) spawnPosition.y);

        fish.setZ(-6.5f);
        fish.setRotation(0.0f);
        fish.setSpeed(NORMAL_SPEED_1);
        fish.setTurningSpeed(NORMAL_TURNING_SPEED);
        fish.setLastCorrectionTime(System.currentTimeMillis());

        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);

        S2CInitFishPacket initFish = new S2CInitFishPacket(fish);
        broadcast(initFish, clients);

        fish.setModel((byte) 1);

        // we must initially move a bit the fish so the details packet works
        fish.setState(FishState.MOVING);
        float[] randomPosition = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
        fish.moveTo(randomPosition[0], randomPosition[1], NORMAL_SPEED_1);

        S2CInitFishWithDetailsPacket initFishDetails = new S2CInitFishWithDetailsPacket(
                fish.getId(), fish.getModel(), (byte) fish.getState().getValue(),
                0.0f, fish.getZ(), 0.0f, fish.getX(), fish.getY(),
                fish.getDirX(), fish.getDirY(), fish.getDestX(), fish.getDestY(),
                fish.getSpeed(), 0.0f, 0.0f, (short) 0
        );
        broadcast(initFishDetails, clients);


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

    public Fish getClaimedFish(short roomId, short playerPosition) {
        List<Fish> fishes = getFishes(roomId);
        if (fishes == null || fishes.isEmpty()) {
            return null;
        }

        for (Fish fish : fishes) {
            if (fish.getClaimedPlayerPosition() == playerPosition && fish.getState() == FishState.BITING && fish.isBitBait()) {
                return fish;
            }
        }
        return null;
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
        List<Fish> fishes = fishesByRoomId.get(roomId);
        if (fishes == null || fishes.isEmpty()) {
            scheduledRooms.remove(roomId);
            return;
        }

        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);
        long currentTime = System.currentTimeMillis();

        List<Fish> inactiveFishes = new ArrayList<>();
        for (Fish fish : fishes) {
            if (fish == null) continue;

            fish.updateAliveTime();
            float deltaSeconds = (currentTime - fish.getLastUpdate()) / 1000.0f;
            fish.setLastUpdate(currentTime);

            if (fish.getLastActivityTime() + INACTIVITY_TIMEOUT * 60 * 1000 < currentTime) {
                log.info("Fish " + fish.getId() + " in room " + roomId + " has been inactive for too long, removing it.");
                inactiveFishes.add(fish);
                continue;
            }

            switch (fish.getState()) {
                case MOVING -> {
                    float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
                    float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;

                    // Clamp to spawn radius
                    float distFromOrigin = distance(newX, newY, fish.getSpawnX(), fish.getSpawnY());
                    if (distFromOrigin > RANDOM_POSITION_RADIUS) {
                        if (currentTime - fish.getLastCorrectionTime() > 3000) {
                            fish.setLastCorrectionTime(currentTime);
                            fish.stop();
                            S2CFishStopPacket stopPacket = new S2CFishStopPacket(fish.getId(), (byte) fish.getState().getValue());
                            broadcast(stopPacket, clients);

                            float[] randomPosition = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
                            newX = randomPosition[0];
                            newY = randomPosition[1];
                            fish.setState(FishState.IDLE);
                            fish.setSpeed(NORMAL_SPEED_1);
                            fish.setTurningSpeed(NORMAL_TURNING_SPEED);
                            fish.moveTo(newX, newY, NORMAL_SPEED_1);

                            S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) fish.getState().getValue(), newX, newY, fish.getSpeed());
                            broadcast(movePacket, clients);
                        }
                        break;
                    }

                    fish.updatePosition(newX, newY);

                    S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) fish.getState().getValue(), fish.getX(), fish.getY(), fish.getSpeed());
                    broadcast(movePacket, clients);

                    if (fish.hasReachedDestination(0.8f)) {
                        fish.stop();
                        S2CFishStopPacket stopPacket = new S2CFishStopPacket(fish.getId(), (byte) fish.getState().getValue());
                        broadcast(stopPacket, clients);
                    }
                }
                case IDLE -> {
                    if (random.nextInt(100) < MOVEMENT_CHANCE_1) {
                        float[] next = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
                        fish.setState(FishState.MOVING);
                        fish.moveTo(next[0], next[1], NORMAL_SPEED_1);
                    }
                }
                case FRIGHTENED -> {
                    if (fish.hasReachedDestination(1.0f)) {
                        fish.stop();
                        S2CFishStopPacket stopPacket = new S2CFishStopPacket(fish.getId(), (byte) fish.getState().getValue());
                        broadcast(stopPacket, clients);

                        fish.setState(FishState.MOVING);
                        fish.setSpeed(DISENGAGE_SPEED);
                        fish.setTurningSpeed(DISENGAGE_TURNING_SPEED);
                        fish.moveTo(fish.getSpawnX(), fish.getSpawnY(), DISENGAGE_SPEED);
                        break;
                    }

                    // Update position if not yet at destination
                    float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
                    float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;
                    fish.updatePosition(newX, newY);

                    S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) fish.getState().getValue(), fish.getX(), fish.getY(), fish.getSpeed());
                    broadcast(movePacket, clients);
                }
                case ATTACKING -> {
                    if (fish.hasReachedDestination(1.0f)) {
                        fish.stop();
                        S2CFishStopPacket stopPacket = new S2CFishStopPacket(fish.getId(), (byte) FishState.ATTACKING.getValue());
                        broadcast(stopPacket, clients);

                        fish.setState(FishState.BITING);
                        fish.setSpeed(0.0f);
                        fish.setTurningSpeed(0.0f);

                        break;
                    }

                    float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
                    float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;
                    fish.updatePosition(newX, newY);

                    S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) FishState.MOVING.getValue(), fish.getX(), fish.getY(), fish.getSpeed());
                    broadcast(movePacket, clients);
                }
                case BITING -> {
                    if (!baitExists(fish.getDestX(), fish.getDestY()) && !fish.isBitBait()) {
                        fish.setState(FishState.IDLE);
                        fish.setSpeed(NORMAL_SPEED_1);
                        fish.setTurningSpeed(NORMAL_TURNING_SPEED);
                        S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) fish.getState().getValue(), fish.getX(), fish.getY(), fish.getSpeed());
                        broadcast(movePacket, clients);
                        continue;
                    }

                    if (fish.getLastBiteTime() + BITE_ATTEMPT_INTERVAL * 1000 > currentTime) {
                        continue; // Still in biting cooldown
                    }

                    if (canFishAttackBait(fish, fish.getDestX(), fish.getDestY()) && !fish.isBitBait()) {
                        S2CFishMovePacket movePacket = new S2CFishMovePacket(fish.getId(), (byte) fish.getState().getValue(), fish.getX(), fish.getY(), fish.getSpeed());
                        broadcast(movePacket, clients);

                        short playerPos = clients.stream()
                                .filter(c -> c.getRoomPlayer() != null && c.getRoomPlayer().getUsedRod().get())
                                .map(FTClient::getRoomPlayer)
                                .filter(rp -> rp.getBaitX() + FISH_LENGTH >= fish.getX() &&
                                              rp.getBaitX() - FISH_LENGTH <= fish.getX() &&
                                              rp.getBaitY() + FISH_LENGTH >= fish.getY() &&
                                              rp.getBaitY() - FISH_LENGTH <= fish.getY())
                                .map(RoomPlayer::getPosition)
                                .findFirst()
                                .orElse((short) -1);
                        FTClient client = clients.stream()
                                .filter(c -> c.getRoomPlayer() != null && c.getRoomPlayer().getPosition() == playerPos)
                                .findFirst()
                                .orElse(null);
                        if (playerPos != -1 && client != null) {
                            fish.setBitBait(true);
                            fish.setLastBiteTime(currentTime);
                            removeBaitPosition(fish.getDestX(), fish.getDestY());
                            fish.setClaimedPlayerPosition(playerPos);

                            S2CFishingBarPacket initMiniGame = new S2CFishingBarPacket(playerPos, fish.getId(), 1.3f, (byte) 0);
                            broadcast(initMiniGame, clients);
                        } else {
                            log.warn("Fish " + fish.getId() + " bit bait but no player found in room " + roomId);
                            fish.reset();
                        }
                    }
                }
            }

            if (!baitPositions.isEmpty() && (fish.getState() == FishState.MOVING || fish.getState() == FishState.IDLE)) {
                final ConcurrentLinkedDeque<Float[]> baitPositions = this.baitPositions;
                for (final Float[] baitPosition : baitPositions) {
                    if (baitPosition != null && isFishNearBait(fish, baitPosition[0], baitPosition[1])) {
                        fish.setState(FishState.ATTACKING);
                        fish.setSpeed(ATTACK_SPEED);
                        fish.setTurningSpeed(NORMAL_TURNING_SPEED);
                        fish.moveTo(baitPosition[0], baitPosition[1], ATTACK_SPEED);
                    }
                }
            }
        }

        // handle spawn logic
        if (fishes.size() < MAX_FISH_SPAWN_COUNT_PER_ROOM && random.nextInt(101) < 20) { // 20% chance to spawn a new fish
            final float spawnTime = MIN_SPAWN_TIME + random.nextFloat() * (MAX_SPAWN_TIME - MIN_SPAWN_TIME);
            if (fishes.stream().noneMatch(f -> f.getLastUpdate() + spawnTime * 1000 > currentTime)) {
                Fish newFish = spawnFish(roomId);
                fishes.add(newFish);
            }
        }

        for (Fish inactiveFish : inactiveFishes) {
            removeFish(roomId, inactiveFish);
            S2CDestroyFishPacket destroyFish = new S2CDestroyFishPacket(inactiveFish.getId());
            broadcast(destroyFish, clients);
        }
    }

    public void frightenFishes(short roomId, float baitX, float baitY) {
        List<Fish> fishes = getFishes(roomId);
        if (fishes == null || fishes.isEmpty()) {
            return;
        }

        for (Fish fish : fishes) {
            if (fish == null || fish.getState() == FishState.CAUGHT) {
                continue;
            }

            float dist = distance(fish.getX(), fish.getY(), baitX, baitY);
            if (dist <= FRIGHTENED_RADIUS) {
                fish.setState(FishState.FRIGHTENED);
                fish.setSpeed(SCARED_SPEED);
                fish.setTurningSpeed(DISENGAGE_TURNING_SPEED);

                float[] fleeDirection = getFleeDirectionFrom(fish.getX(), fish.getY(), baitX, baitY);
                fish.moveTo(fleeDirection[0], fleeDirection[1], SCARED_SPEED);
            }
        }
    }

    private boolean isFishNearBait(Fish fish, float baitX, float baitY) {
        return distance(fish.getX(), fish.getY(), baitX, baitY) <= BAIT_DETECTION_RADIUS;
    }

    private boolean canFishAttackBait(Fish fish, float baitX, float baitY) {
        return baitExists(baitX, baitY) && distance(fish.getX(), fish.getY(), baitX, baitY) <= ATTACK_RADIUS && random.nextInt(101) < BITE_SUCCESS_CHANCE;
    }

    private boolean baitExists(float baitX, float baitY) {
        return baitPositions.stream().anyMatch(p -> p[0] + FISH_LENGTH >= baitX &&
                p[0] - FISH_LENGTH <= baitX &&
                p[1] + FISH_LENGTH >= baitY &&
                p[1] - FISH_LENGTH <= baitY);
    }

    private float[] getRandomDirectionFrom(float originX, float originY) {
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        float radius = RANDOM_POSITION_RADIUS * (0.5f + random.nextFloat() * 0.5f); // 50–100% of range
        float dx = (float) Math.cos(angle) * radius;
        float dy = (float) Math.sin(angle) * radius;
        return new float[] { originX + dx, originY + dy };
    }

    private float[] getFleeDirectionFrom(float fishX, float fishY, float baitX, float baitY) {
        float dx = fishX - baitX;
        float dy = fishY - baitY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist == 0) dist = 1;
        dx /= dist;
        dy /= dist;

        float fleeDistance = RANDOM_POSITION_RADIUS * (0.5f + random.nextFloat() * 0.5f); // 50–100%
        float offsetAngle = (float) ((random.nextFloat() - 0.5f) * Math.PI / 6.0); // +/- 15 degrees

        float angle = (float) Math.atan2(dy, dx) + offsetAngle;
        float fx = fishX + (float) Math.cos(angle) * fleeDistance;
        float fy = fishY + (float) Math.sin(angle) * fleeDistance;

        return new float[] { fx, fy };
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
