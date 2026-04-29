package com.jftse.emulator.server.core;

import com.jftse.emulator.server.core.constants.BallHitAction;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Log4j2
public class AnimationDebugStats {
    private static final int RECENT_PLAYER_ANIMATION_LIMIT = 80;

    private final Map<Integer, LongAdder> playerAnimationTypes = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> playerAnimationTransitions = new ConcurrentHashMap<>();

    private final Map<Integer, LongAdder> hitActs = new ConcurrentHashMap<>();
    private final Map<Integer, LongAdder> powerLevels = new ConcurrentHashMap<>();
    private final Map<Integer, LongAdder> speeds = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> ballCombinations = new ConcurrentHashMap<>();

    private final Deque<Integer> recentPlayerAnimations = new ArrayDeque<>();

    private int lastPlayerAnimation = -1;

    public synchronized void recordPlayerAnimation(byte animationTypeRaw) {
        int animationType = Byte.toUnsignedInt(animationTypeRaw);

        increment(playerAnimationTypes, animationType);

        if (lastPlayerAnimation != -1 && lastPlayerAnimation != animationType) {
            String transition = describePlayerAnimation(lastPlayerAnimation)
                    + " -> "
                    + describePlayerAnimation(animationType);

            increment(playerAnimationTransitions, transition);
        }

        lastPlayerAnimation = animationType;

        recentPlayerAnimations.addLast(animationType);
        while (recentPlayerAnimations.size() > RECENT_PLAYER_ANIMATION_LIMIT) {
            recentPlayerAnimations.removeFirst();
        }
    }

    public synchronized void recordBallAnimation(byte hitActRaw, byte powerLevelRaw, short speedRaw) {
        int hitAct = Byte.toUnsignedInt(hitActRaw);
        int powerLevel = Byte.toUnsignedInt(powerLevelRaw);

        increment(hitActs, hitAct);
        increment(powerLevels, powerLevel);
        increment(speeds, (int) speedRaw);

        String combination = BallHitAction.describe(hitAct)
                + "(hitAct=" + hitAct + ")"
                + " powerLevel=" + powerLevel
                + " speed=" + describeSpeed(speedRaw);

        increment(ballCombinations, combination);

        log.info(
                "Ball event: {}, recentPlayerAnimations={}",
                combination,
                formatRecentPlayerAnimations()
        );
    }

    public synchronized void logSummary(int playerId) {
        log.info("""
                        
                        Animation debug summary for playerId={}
                          Player animation types:
                            {}
                          Player animation transitions:
                            {}
                          Ball hit actions:
                            {}
                          Ball power levels:
                            {}
                          Ball speeds:
                            {}
                          Ball combinations:
                            {}
                          Recent player animations:
                            {}
                        """,
                playerId,
                formatPlayerAnimationMap(playerAnimationTypes),
                formatStringCounterMap(playerAnimationTransitions),
                formatHitActMap(hitActs),
                formatIntCounterMap(powerLevels),
                formatSpeedMap(speeds),
                formatStringCounterMap(ballCombinations),
                formatRecentPlayerAnimations()
        );
    }

    public synchronized void reset() {
        playerAnimationTypes.clear();
        playerAnimationTransitions.clear();
        hitActs.clear();
        powerLevels.clear();
        speeds.clear();
        ballCombinations.clear();
        recentPlayerAnimations.clear();
        lastPlayerAnimation = -1;
    }

    private void increment(Map<Integer, LongAdder> map, int key) {
        map.computeIfAbsent(key, ignored -> new LongAdder()).increment();
    }

    private void increment(Map<String, LongAdder> map, String key) {
        map.computeIfAbsent(key, ignored -> new LongAdder()).increment();
    }

    private String formatHitActMap(Map<Integer, LongAdder> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey()
                        + "(" + BallHitAction.describe(e.getKey()) + ")"
                        + "=" + e.getValue().sum())
                .toList()
                .toString();
    }

    private String formatPlayerAnimationMap(Map<Integer, LongAdder> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> describePlayerAnimation(e.getKey()) + "=" + e.getValue().sum())
                .toList()
                .toString();
    }

    private String formatIntCounterMap(Map<Integer, LongAdder> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue().sum())
                .toList()
                .toString();
    }

    private String formatSpeedMap(Map<Integer, LongAdder> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> describeSpeed(e.getKey().shortValue()) + "=" + e.getValue().sum())
                .toList()
                .toString();
    }

    private String formatStringCounterMap(Map<String, LongAdder> map) {
        return map.entrySet().stream()
                .sorted(
                        Comparator
                                .<Map.Entry<String, LongAdder>>comparingLong(e -> e.getValue().sum())
                                .reversed()
                                .thenComparing(Map.Entry::getKey)
                )
                .map(e -> e.getKey() + "=" + e.getValue().sum())
                .toList()
                .toString();
    }

    private String formatRecentPlayerAnimations() {
        return recentPlayerAnimations.stream()
                .map(this::describePlayerAnimation)
                .toList()
                .toString();
    }

    private String describePlayerAnimation(int value) {
        int group = value & 0xF0;
        int low = value & 0x0F;

        return value
                + "(group=" + group
                + ", low=" + low
                + ")";
    }

    private String describeSpeed(short speedRaw) {
        return speedRaw + "(" + String.format("%.2f", speedRaw / 100.0f) + ")";
    }
}
