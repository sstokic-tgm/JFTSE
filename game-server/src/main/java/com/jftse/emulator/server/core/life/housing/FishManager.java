package com.jftse.emulator.server.core.life.housing;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.*;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.KStatus;
import com.jftse.entities.database.model.SRelationshipRoles;
import com.jftse.entities.database.model.SRelationshipTypes;
import com.jftse.entities.database.model.SRelationships;
import com.jftse.entities.database.repository.RelationshipRolesRepository;
import com.jftse.entities.database.repository.RelationshipTypesRepository;
import com.jftse.entities.database.repository.RelationshipsRepository;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.util.GameTime;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
@Service
@Getter
@Setter
public class FishManager {

    @Getter
    private static FishManager instance;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RelationshipsRepository rr;

    @Autowired
    private RelationshipRolesRepository rrRole;

    @Autowired
    private RelationshipTypesRepository rrType;

    private SecureRandom random;

    private final ConcurrentHashMap<Short, CopyOnWriteArrayList<Fish>> fishesByRoomId = new ConcurrentHashMap<>();
    private final Set<Short> scheduledRooms = ConcurrentHashMap.newKeySet();

    private final ConcurrentHashMap<Short, ConcurrentLinkedDeque<Float[]>> baitPositionsByRoom = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Short, Long> nextSpawnAttemptByRoom = new ConcurrentHashMap<>();

    public int MAX_FISH_SPAWN_COUNT_PER_ROOM;
    public int FISH_SPAWN_COUNT;

    public int INACTIVITY_TIMEOUT; // minutes
    public int SPAWN_CHANCE; // percent
    public float MIN_SPAWN_TIME; // seconds
    public float MAX_SPAWN_TIME; // seconds
    public float NORMAL_SPEED_1;
    public float NORMAL_SPEED_2;
    public float ATTACK_SPEED;
    public float SCARED_SPEED;
    public float DISENGAGE_SPEED;
    public float NORMAL_TURNING_SPEED; // degrees
    public float DISENGAGE_TURNING_SPEED; // degrees

    public int BITE_SUCCESS_CHANCE; // percent
    public float BITE_ATTEMPT_INTERVAL; // seconds
    public static float FISH_LENGTH = 6.43f;
    public float ATTACK_RADIUS; // related to fish length
    public float BAIT_TOO_CLOSE_RADIUS; // related to fish length
    public float FRIGHTENED_RADIUS; // related to fish length
    public float BAIT_DETECTION_RADIUS; // related to fish length

    public float RANDOM_POSITION_RADIUS; // 5.0 is 1 square
    public int MOVEMENT_CHANCE_1; // percent
    public int MOVEMENT_CHANCE_2; // percent

    public static final int MAX_FISH_ID = 31;
    public static final float BAIT_MATCH_EPSILON = 0.01f;

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

    private SRelationshipRoles relationRole;
    private SRelationshipTypes relationType;

    @PostConstruct
    public void init() {
        instance = this;

        random = new SecureRandom();
        random.setSeed(GameTime.getGameTimeMS());

        relationRole = rrRole.findById(5L)
                .orElseThrow(() -> new RuntimeException("Role: 'Fishing Item Drop' not found(5)"));

        relationType = rrType.findById(9L)
                .orElseThrow(() -> new RuntimeException("Type: 'Fishing: Product to Group' not found(9)"));

        initFishSettings();

        log.info("{} initialized", this.getClass().getSimpleName());
    }

    private void initFishSettings() {
        MAX_FISH_SPAWN_COUNT_PER_ROOM = configService.getValue("fish.spawn.max_per_room", 10);
        FISH_SPAWN_COUNT = configService.getValue("fish.spawn.count", 4);
        INACTIVITY_TIMEOUT = configService.getValue("fish.inactivity.timeout", 10);
        SPAWN_CHANCE = configService.getValue("fish.spawn.chance", 20);
        MIN_SPAWN_TIME = configService.getValue("fish.spawn.min_time", 6.0f);
        MAX_SPAWN_TIME = configService.getValue("fish.spawn.max_time", 8.0f);
        NORMAL_SPEED_1 = configService.getValue("fish.speed.normal1", 2.0f);
        NORMAL_SPEED_2 = configService.getValue("fish.speed.normal2", 3.0f);
        ATTACK_SPEED = configService.getValue("fish.speed.attack", 4.0f);
        SCARED_SPEED = configService.getValue("fish.speed.scared", 10.0f);
        DISENGAGE_SPEED = configService.getValue("fish.speed.disengage", 10.0f);
        NORMAL_TURNING_SPEED = configService.getValue("fish.speed.turning.normal", 3.0f);
        DISENGAGE_TURNING_SPEED = configService.getValue("fish.speed.turning.disengage", 6.0f);
        BITE_SUCCESS_CHANCE = configService.getValue("fish.bite.success.chance", 40);
        BITE_ATTEMPT_INTERVAL = configService.getValue("fish.bite.attempt.interval", 2.0f);
        ATTACK_RADIUS = configService.getValue("fish.radius.attack", 4.0f);
        BAIT_TOO_CLOSE_RADIUS = configService.getValue("fish.radius.bait.too_close", 6.0f);
        FRIGHTENED_RADIUS = configService.getValue("fish.radius.frightened", 10.0f);
        BAIT_DETECTION_RADIUS = configService.getValue("fish.radius.bait.detection", 10.0f);
        RANDOM_POSITION_RADIUS = configService.getValue("fish.radius.random_position", 10.0f);
        MOVEMENT_CHANCE_1 = configService.getValue("fish.movement.chance1", 40);
        MOVEMENT_CHANCE_2 = configService.getValue("fish.movement.chance2", 25);
    }

    public void reloadFishSettings() {
        log.info("Reloading fish settings...");
        initFishSettings();
        log.info("Fish settings reloaded successfully.");
    }

    @PreDestroy
    public void shutdown() {
        clearAllFishes();
        log.info("{} shutdown", this.getClass().getSimpleName());
    }

    public void registerRoom(short roomId) {
        CopyOnWriteArrayList<Fish> fishes = fishesByRoomId.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        baitPositionsByRoom.computeIfAbsent(roomId, k -> new ConcurrentLinkedDeque<>());
        scheduledRooms.add(roomId);

        if (fishes.size() >= MAX_FISH_SPAWN_COUNT_PER_ROOM) {
            log.warn("Cannot add fish to room {}: maximum fish count reached", roomId);
            return;
        }

        int toSpawn = Math.min(FISH_SPAWN_COUNT, MAX_FISH_SPAWN_COUNT_PER_ROOM - fishes.size());
        for (int i = 0; i < toSpawn; i++) {
            Fish fish = spawnFish(roomId, fishes);
            if (fish != null) {
                fishes.add(fish);
            }
        }

        scheduleNextSpawnAttempt(roomId, GameTime.getGameTimeMS());
    }

    public void registerBaitPosition(short roomId, float x, float y) {
        baitPositionsByRoom
                .computeIfAbsent(roomId, k -> new ConcurrentLinkedDeque<>())
                .offer(new Float[]{x, y});
    }

    public void removeBaitPosition(short roomId, float x, float y) {
        ConcurrentLinkedDeque<Float[]> baitPositions = baitPositionsByRoom.get(roomId);
        if (baitPositions == null || baitPositions.isEmpty()) {
            return;
        }

        baitPositions.removeIf(baitPosition ->
                baitPosition != null &&
                        Math.abs(baitPosition[0] - x) <= BAIT_MATCH_EPSILON &&
                        Math.abs(baitPosition[1] - y) <= BAIT_MATCH_EPSILON);
    }

    private Fish spawnFish(short roomId, List<Fish> fishes) {
        short nextFishId = safeGetNextFishId(fishes);
        if (nextFishId == -1) {
            log.warn("No free fish id found for room {}, skipping spawn", roomId);
            return null;
        }

        Fish fish = new Fish();
        fish.setId(nextFishId);
        fish.setModel((byte) 0);
        fish.setState(FishState.IDLE);

        Point spawnPosition = pickSpawnPosition(fishes);
        fish.setX((float) spawnPosition.x);
        fish.setY((float) spawnPosition.y);
        fish.setSpawnX((float) spawnPosition.x);
        fish.setSpawnY((float) spawnPosition.y);

        fish.setZ(-6.5f);
        fish.setRotation(0.0f);
        fish.setSpeed(NORMAL_SPEED_1);
        fish.setTurningSpeed(NORMAL_TURNING_SPEED);
        fish.setLastCorrectionTime(GameTime.getGameTimeMS());
        fish.setLastUpdate(GameTime.getGameTimeMS());
        fish.setLastActivityTime(GameTime.getGameTimeMS());

        fish.setGroup(random.nextInt(4));
        fish.setRewardProductIndex(pickRandomReward(fish.getGroup()));

        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);

        broadcast(clients, new S2CInitFishPacket(fish));

        fish.setModel((byte) 1);

        // start with a first random movement
        float[] randomPosition = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
        fish.setState(FishState.MOVING);
        fish.moveTo(randomPosition[0], randomPosition[1], NORMAL_SPEED_1);

        S2CInitFishWithDetailsPacket initFishDetails = new S2CInitFishWithDetailsPacket(
                fish.getId(),
                fish.getModel(),
                (byte) fish.getState().getValue(),
                0.0f,
                fish.getZ(),
                0.0f,
                fish.getX(),
                fish.getY(),
                fish.getDirX(),
                fish.getDirY(),
                fish.getDestX(),
                fish.getDestY(),
                fish.getSpeed(),
                0.0f,
                0.0f,
                (short) 0
        );
        broadcast(clients, initFishDetails);

        log.info("Spawned fish with ID {} in room {} at ({}, {})", fish.getId(), roomId, fish.getSpawnX(), fish.getSpawnY());
        return fish;
    }

    private Point pickSpawnPosition(List<Fish> fishes) {
        Set<Point> usedSpawns = new HashSet<>();
        for (Fish f : fishes) {
            usedSpawns.add(new Point((int) f.getSpawnX(), (int) f.getSpawnY()));
        }

        List<Point> available = new ArrayList<>();
        for (Point point : FISH_SPAWN_POSITIONS) {
            if (!usedSpawns.contains(point)) {
                available.add(point);
            }
        }

        if (!available.isEmpty()) {
            return available.get(random.nextInt(available.size()));
        }

        // fallback only when all distinct spawn points are already in use
        return FISH_SPAWN_POSITIONS.get(random.nextInt(FISH_SPAWN_POSITIONS.size()));
    }

    private short safeGetNextFishId(List<Fish> fishes) {
        Set<Short> fishIds = new HashSet<>();
        for (Fish fish : fishes) {
            fishIds.add(fish.getId());
        }

        short nextId = 0;
        int attempts = 0;

        while (fishIds.contains(nextId)) {
            nextId++;
            attempts++;

            if (nextId > MAX_FISH_ID) {
                nextId = 0;
            }

            if (attempts > MAX_FISH_ID) {
                return -1;
            }
        }

        return nextId;
    }

    public void removeFish(short roomId, Fish fish) {
        List<Fish> fishes = fishesByRoomId.get(roomId);
        if (fishes == null) {
            return;
        }

        fishes.remove(fish);
    }

    public List<Fish> getFishes(short roomId) {
        return fishesByRoomId.getOrDefault(roomId, new CopyOnWriteArrayList<>());
    }

    public Fish getClaimedFish(short roomId, short playerPosition) {
        List<Fish> fishes = getFishes(roomId);
        if (fishes.isEmpty()) {
            return null;
        }

        for (Fish fish : fishes) {
            if (fish.getClaimedPlayerPosition() == playerPosition &&
                    fish.getState() == FishState.BITING &&
                    fish.isBitBait()) {
                return fish;
            }
        }

        return null;
    }

    public void clearFishes(short roomId) {
        fishesByRoomId.remove(roomId);
        baitPositionsByRoom.remove(roomId);
        nextSpawnAttemptByRoom.remove(roomId);
        scheduledRooms.remove(roomId);
    }

    public void clearAllFishes() {
        fishesByRoomId.clear();
        baitPositionsByRoom.clear();
        nextSpawnAttemptByRoom.clear();
        scheduledRooms.clear();
    }

    public void update(long diff) {
        for (short roomId : scheduledRooms) {
            tick(roomId, diff);
        }
    }

    private void tick(short roomId, long diff) {
        CopyOnWriteArrayList<Fish> fishes = fishesByRoomId.get(roomId);
        if (fishes == null) {
            scheduledRooms.remove(roomId);
            return;
        }

        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);
        final long currentTime = GameTime.getGameTimeMS();
        final float deltaSeconds = diff / 1000.0f;

        List<Fish> inactiveFishes = new ArrayList<>();

        for (Fish fish : fishes) {
            if (fish == null) {
                continue;
            }

            FishState stateBeforeUpdate = fish.getState();
            float destXBeforeUpdate = fish.getDestX();
            float destYBeforeUpdate = fish.getDestY();

            fish.updateAliveTime();
            fish.setLastUpdate(currentTime);

            if (fish.getLastActivityTime() + (long) INACTIVITY_TIMEOUT * 60 * 1000 < currentTime) {
                log.info("Fish {} in room {} has been inactive for too long, removing it.", fish.getId(), roomId);
                inactiveFishes.add(fish);
                continue;
            }

            switch (fish.getState()) {
                case MOVING -> handleMovingFish(roomId, fish, clients, currentTime, deltaSeconds);
                case IDLE -> handleIdleFish(fish, clients, currentTime);
                case FRIGHTENED -> handleFrightenedFish(fish, clients, deltaSeconds);
                case ATTACKING -> handleAttackingFish(fish, clients, deltaSeconds);
                case BITING -> handleBitingFish(roomId, fish, clients, currentTime);
                case CAUGHT, ODD -> {
                }
            }

            boolean movementOrderChangedThisTick =
                    fish.getState() == FishState.MOVING
                            && (stateBeforeUpdate != fish.getState()
                            || destXBeforeUpdate != fish.getDestX()
                            || destYBeforeUpdate != fish.getDestY());

            if (!movementOrderChangedThisTick &&
                    (fish.getState() == FishState.MOVING || fish.getState() == FishState.IDLE)) {
                tryAcquireNearbyBait(roomId, fish, clients);
            }

            if (fish.getState() != FishState.IDLE && fish.getState() != FishState.MOVING) {
                log.debug(fish.debugString());
            }
        }

        for (Fish inactiveFish : inactiveFishes) {
            removeFish(roomId, inactiveFish);
            broadcast(clients, new S2CDestroyFishPacket(inactiveFish.getId()));
        }

        handleSpawnLogic(roomId, fishes, currentTime);
    }

    private void handleMovingFish(short roomId, Fish fish, List<FTClient> clients, long currentTime, float deltaSeconds) {
        float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
        float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;

        float distFromOrigin = distance(newX, newY, fish.getSpawnX(), fish.getSpawnY());
        if (distFromOrigin > RANDOM_POSITION_RADIUS + FISH_LENGTH) {
            if (currentTime - fish.getLastCorrectionTime() > 3000) {
                fish.setLastCorrectionTime(currentTime);

                FishState previousState = fish.getState();
                fish.stopMovement();

                float[] randomPosition = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
                fish.setState(FishState.MOVING);
                fish.setSpeed(NORMAL_SPEED_1);
                fish.setTurningSpeed(NORMAL_TURNING_SPEED);
                fish.moveTo(randomPosition[0], randomPosition[1], NORMAL_SPEED_1);

                broadcast(clients,
                        new S2CFishStopPacket(fish.getId(), (byte) previousState.getValue()),
                        new S2CFishMovePacket(
                                fish.getId(),
                                (byte) fish.getState().getValue(),
                                fish.getDestX(),
                                fish.getDestY(),
                                fish.getSpeed()
                        ));
                return;
            }

            // keep simulating until correction is allowed
            fish.updatePosition(newX, newY);

            if (fish.hasReachedDestination(0.8f)) {
                fish.updatePosition(fish.getDestX(), fish.getDestY());

                FishState previousState = fish.getState();
                fish.stopMovement();
                fish.setState(FishState.IDLE);
                broadcast(clients, new S2CFishStopPacket(fish.getId(), (byte) previousState.getValue()));
            }
            return;
        }

        fish.updatePosition(newX, newY);

        if (fish.hasReachedDestination(0.8f)) {
            fish.updatePosition(fish.getDestX(), fish.getDestY());

            FishState previousState = fish.getState();
            fish.stopMovement();
            fish.setState(FishState.IDLE);
            broadcast(clients, new S2CFishStopPacket(fish.getId(), (byte) previousState.getValue()));
        }
    }

    private void handleIdleFish(Fish fish, List<FTClient> clients, long currentTime) {
        if (currentTime < fish.getNextIdleMoveCheckTime()) {
            return;
        }

        long delay = 1500L;
        delay += random.nextInt((int) (4000L - 1500L));
        fish.setNextIdleMoveCheckTime(currentTime + delay);

        boolean aggressive = fish.isAggressiveMovementPattern();
        int chance = aggressive ? MOVEMENT_CHANCE_2 : MOVEMENT_CHANCE_1;
        float speed = aggressive ? NORMAL_SPEED_2 : NORMAL_SPEED_1;

        fish.setAggressiveMovementPattern(!aggressive);

        if (random.nextInt(100) < chance) {
            float[] next = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());

            fish.setState(FishState.MOVING);
            fish.setSpeed(speed);
            fish.setTurningSpeed(NORMAL_TURNING_SPEED);
            fish.moveTo(next[0], next[1], speed);

            broadcast(clients,
                    new S2CFishMovePacket(
                            fish.getId(),
                            (byte) fish.getState().getValue(),
                            fish.getDestX(),
                            fish.getDestY(),
                            fish.getSpeed()
                    ));
        }
    }

    private void handleFrightenedFish(Fish fish, List<FTClient> clients, float deltaSeconds) {
        float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
        float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;
        fish.updatePosition(newX, newY);

        if (fish.hasReachedDestination(1.0f)) {
            fish.updatePosition(fish.getDestX(), fish.getDestY());

            FishState previousState = fish.getState();
            fish.stopMovement();

            fish.setState(FishState.MOVING);
            fish.setSpeed(DISENGAGE_SPEED);
            fish.setTurningSpeed(DISENGAGE_TURNING_SPEED);
            fish.moveTo(fish.getSpawnX(), fish.getSpawnY(), DISENGAGE_SPEED);

            broadcast(clients,
                    new S2CFishStopPacket(fish.getId(), (byte) previousState.getValue()),
                    new S2CFishMovePacket(
                            fish.getId(),
                            (byte) fish.getState().getValue(),
                            fish.getDestX(),
                            fish.getDestY(),
                            fish.getSpeed()
                    ));
        }
    }

    private void handleAttackingFish(Fish fish, List<FTClient> clients, float deltaSeconds) {
        float newX = fish.getX() + fish.getDirX() * fish.getSpeed() * deltaSeconds;
        float newY = fish.getY() + fish.getDirY() * fish.getSpeed() * deltaSeconds;
        fish.updatePosition(newX, newY);

        if (fish.hasReachedDestination(1.0f)) {
            fish.updatePosition(fish.getDestX(), fish.getDestY());

            FishState previousState = fish.getState();
            fish.stopMovement();

            fish.setState(FishState.BITING);
            fish.setSpeed(0.0f);
            fish.setTurningSpeed(0.0f);

            broadcast(clients,
                    new S2CFishStopPacket(fish.getId(), (byte) previousState.getValue()),
                    new S2CFishMovePacket(
                            fish.getId(),
                            (byte) FishState.ATTACKING.getValue(),
                            fish.getX(),
                            fish.getY(),
                            fish.getSpeed()
                    ));
        }
    }

    private short getPlayerPosByFish(Fish fish, List<FTClient> clients) {
        return clients.stream()
                .filter(c -> c.getRoomPlayer() != null &&
                        c.getRoomPlayer().getUsedRod().get())
                .map(FTClient::getRoomPlayer)
                .filter(rp -> rp.getBaitX() + FISH_LENGTH >= fish.getX() &&
                        rp.getBaitX() - FISH_LENGTH <= fish.getX() &&
                        rp.getBaitY() + FISH_LENGTH >= fish.getY() &&
                        rp.getBaitY() - FISH_LENGTH <= fish.getY())
                .map(RoomPlayer::getPosition)
                .findFirst()
                .orElse((short) -1);
    }

    private void handleBitingFish(short roomId, Fish fish, List<FTClient> clients, long currentTime) {
        if (!baitExists(roomId, fish.getDestX(), fish.getDestY()) && !fish.isBitBait()) {
            fish.stopMovement();
            fish.setState(FishState.IDLE);
            broadcast(clients, new S2CFishStopPacket(fish.getId(), (byte) FishState.IDLE.getValue()));
            return;
        }

        if (fish.getLastBiteTime() + (long) (BITE_ATTEMPT_INTERVAL * 1000) > currentTime) {
            return;
        }

        if (canFishAttackBait(roomId, fish, fish.getDestX(), fish.getDestY()) && !fish.isBitBait()) {
            short playerPos = getPlayerPosByFish(fish, clients);

            if (playerPos != -1) {
                fish.setBitBait(true);
                fish.setLastBiteTime(currentTime);
                fish.setClaimedPlayerPosition(playerPos);
                removeBaitPosition(roomId, fish.getDestX(), fish.getDestY());

                broadcast(clients,
                        new S2CFishStopPacket(fish.getId(), (byte) FishState.BITING.getValue()),
                        new S2CFishingBarPacket(
                                playerPos,
                                fish.getId(),
                                fish.getFishingBarSpeed(),
                                (byte) fish.getGroup()));
            } else {
                log.warn("Fish {} reached BITING in room {} but no matching player was found", fish.getId(), roomId);

                fish.stopMovement();
                fish.setBitBait(false);
                fish.setClaimedPlayerPosition((short) -1);
                fish.setState(FishState.MOVING);
                fish.setSpeed(NORMAL_SPEED_1);
                fish.setTurningSpeed(NORMAL_TURNING_SPEED);

                float[] next = getRandomDirectionFrom(fish.getSpawnX(), fish.getSpawnY());
                fish.moveTo(next[0], next[1], NORMAL_SPEED_1);

                broadcast(clients,
                        new S2CFishMovePacket(
                                fish.getId(),
                                (byte) fish.getState().getValue(),
                                fish.getDestX(),
                                fish.getDestY(),
                                fish.getSpeed()
                        ));
            }
        }
    }

    private void tryAcquireNearbyBait(short roomId, Fish fish, List<FTClient> clients) {
        ConcurrentLinkedDeque<Float[]> baitPositions = baitPositionsByRoom.get(roomId);
        if (baitPositions == null || baitPositions.isEmpty()) {
            return;
        }

        Float[] selectedBait = null;
        float closestDist = Float.MAX_VALUE;

        for (Float[] baitPosition : baitPositions) {
            if (baitPosition == null) {
                continue;
            }

            float dist = distance(fish.getX(), fish.getY(), baitPosition[0], baitPosition[1]);

            if (dist < BAIT_TOO_CLOSE_RADIUS) {
                continue;
            }

            if (dist <= BAIT_DETECTION_RADIUS && dist < closestDist) {
                closestDist = dist;
                selectedBait = baitPosition;
            }
        }

        if (selectedBait == null) {
            return;
        }

        fish.setState(FishState.ATTACKING);
        fish.setSpeed(ATTACK_SPEED);
        fish.setTurningSpeed(NORMAL_TURNING_SPEED);
        fish.moveTo(selectedBait[0], selectedBait[1], ATTACK_SPEED);

        broadcast(clients,
                new S2CFishMovePacket(
                        fish.getId(),
                        (byte) FishState.MOVING.getValue(),
                        fish.getDestX(),
                        fish.getDestY(),
                        fish.getSpeed()
                ));
    }

    private void handleSpawnLogic(short roomId, List<Fish> fishes, long currentTime) {
        if (fishes.size() >= MAX_FISH_SPAWN_COUNT_PER_ROOM) {
            return;
        }

        long nextAllowedAttempt = nextSpawnAttemptByRoom.getOrDefault(roomId, 0L);
        if (currentTime < nextAllowedAttempt) {
            return;
        }

        scheduleNextSpawnAttempt(roomId, currentTime);

        if (random.nextInt(100) >= SPAWN_CHANCE) {
            return;
        }

        Fish newFish = spawnFish(roomId, fishes);
        if (newFish != null) {
            fishes.add(newFish);
        }
    }

    private void scheduleNextSpawnAttempt(short roomId, long currentTime) {
        long delay = (long) ((MIN_SPAWN_TIME + random.nextFloat() * (MAX_SPAWN_TIME - MIN_SPAWN_TIME)) * 1000L);
        nextSpawnAttemptByRoom.put(roomId, currentTime + delay);
    }

    public void frightenFishes(short roomId, float baitX, float baitY) {
        final List<FTClient> clients = GameManager.getInstance().getClientsInRoom(roomId);
        List<Fish> fishes = getFishes(roomId);
        if (fishes.isEmpty()) {
            return;
        }

        for (Fish fish : fishes) {
            if (fish == null || fish.getState() == FishState.CAUGHT || fish.isBitBait()) {
                continue;
            }

            if (fish.getState() == FishState.BITING) {
                continue;
            }

            float dist = distance(fish.getX(), fish.getY(), baitX, baitY);
            if (dist <= FRIGHTENED_RADIUS) {
                fish.setState(FishState.FRIGHTENED);
                fish.setSpeed(SCARED_SPEED);
                fish.setTurningSpeed(DISENGAGE_TURNING_SPEED);

                float[] fleeDirection = getFleeDirectionFrom(fish.getX(), fish.getY(), baitX, baitY);
                fish.moveTo(fleeDirection[0], fleeDirection[1], SCARED_SPEED);

                broadcast(clients,
                        new S2CFishMovePacket(
                                fish.getId(),
                                (byte) fish.getState().getValue(),
                                fish.getDestX(),
                                fish.getDestY(),
                                fish.getSpeed()
                        ));
            }
        }
    }

    private boolean canFishAttackBait(short roomId, Fish fish, float baitX, float baitY) {
        float dist = distance(fish.getX(), fish.getY(), baitX, baitY);
        return baitExists(roomId, baitX, baitY) &&
                dist <= ATTACK_RADIUS &&
                random.nextInt(100) < BITE_SUCCESS_CHANCE;
    }

    private boolean baitExists(short roomId, float baitX, float baitY) {
        ConcurrentLinkedDeque<Float[]> baitPositions = baitPositionsByRoom.get(roomId);
        if (baitPositions == null || baitPositions.isEmpty()) {
            return false;
        }

        return baitPositions.stream().anyMatch(p ->
                p != null &&
                        p[0] + FISH_LENGTH >= baitX &&
                        p[0] - FISH_LENGTH <= baitX &&
                        p[1] + FISH_LENGTH >= baitY &&
                        p[1] - FISH_LENGTH <= baitY);
    }

    private float[] getRandomDirectionFrom(float originX, float originY) {
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        float radius = (RANDOM_POSITION_RADIUS + FISH_LENGTH) * (0.5f + random.nextFloat() * 0.5f);
        float dx = (float) Math.cos(angle) * radius;
        float dy = (float) Math.sin(angle) * radius;
        return new float[]{originX + dx, originY + dy};
    }

    private float[] getFleeDirectionFrom(float fishX, float fishY, float baitX, float baitY) {
        float dx = fishX - baitX;
        float dy = fishY - baitY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist == 0) {
            dist = 1;
        }

        dx /= dist;
        dy /= dist;

        float fleeDistance = (RANDOM_POSITION_RADIUS + FISH_LENGTH) * (0.5f + random.nextFloat() * 0.5f);
        float offsetAngle = (float) ((random.nextFloat() - 0.5f) * Math.PI / 6.0);

        float angle = (float) Math.atan2(dy, dx) + offsetAngle;
        float fx = fishX + (float) Math.cos(angle) * fleeDistance;
        float fy = fishY + (float) Math.sin(angle) * fleeDistance;

        return new float[]{fx, fy};
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private Long pickRandomReward(int group) {
        List<SRelationships> rewards = rr.findAllByRoleAndRelationship(relationRole, relationType);
        if (rewards.isEmpty()) {
            log.warn("No fishing rewards found for role: {} and type: {}", relationRole.getName(), relationType.getName());
            return null;
        }

        rewards.removeIf(r ->
                !r.getStatus().getId().equals(KStatus.ACTIVE) ||
                        r.getId_t().intValue() != group);

        if (rewards.isEmpty()) {
            log.warn("No active fishing rewards found for group {}", group);
            return null;
        }

        double averageWeight = calculateAverageWeight(rewards);
        return selectItemRewardByWeight(rewards, averageWeight);
    }

    private Long selectItemRewardByWeight(List<SRelationships> rewards, double defaultWeight) {
        List<Double> weights = rewards.stream()
                .map(reward -> reward.getWeight() != null ? reward.getWeight() : defaultWeight)
                .toList();

        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight <= 0) {
            return rewards.get(random.nextInt(rewards.size())).getId_f();
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (int i = 0; i < rewards.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue < cumulativeWeight) {
                return rewards.get(i).getId_f();
            }
        }

        return rewards.getLast().getId_f();
    }

    private double calculateAverageWeight(List<SRelationships> rewards) {
        double totalWeight = 0.0;
        int totalItems = 0;

        for (SRelationships reward : rewards) {
            Double weight = reward.getWeight();
            if (weight != null) {
                totalWeight += weight;
                totalItems++;
            }
        }

        return totalItems > 0 ? totalWeight / totalItems : 20.0;
    }

    private void broadcast(List<FTClient> clients, IPacket... packets) {
        if (clients == null || clients.isEmpty()) {
            return;
        }

        for (FTClient client : clients) {
            FTConnection connection = client.getConnection();
            if (connection != null) {
                connection.sendTCP(packets);
            }
        }
    }
}
