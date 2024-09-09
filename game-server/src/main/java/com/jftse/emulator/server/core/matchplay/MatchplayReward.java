package com.jftse.emulator.server.core.matchplay;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class MatchplayReward {
    private List<PlayerReward> playerRewards;

    private Map<Byte, ItemReward> slotRewards;
    private List<ItemReward> itemRewards; // display only

    public MatchplayReward(List<PlayerReward> playerRewards) {
        this.playerRewards = playerRewards;
        this.slotRewards = new HashMap<>(4);
        this.itemRewards = new ArrayList<>();
    }

    public MatchplayReward() {
        this.playerRewards = new ArrayList<>();
        this.slotRewards = new HashMap<>(4);
        this.itemRewards = new ArrayList<>();
    }

    public void addPlayerReward(PlayerReward playerReward) {
        playerRewards.add(playerReward);
    }

    public void addItemRewards(List<ItemReward> itemRewards) {
        this.itemRewards.addAll(itemRewards);
    }

    private void addSlotReward(byte slot, ItemReward itemReward) {
        slotRewards.put(slot, itemReward);
    }

    public PlayerReward getPlayerReward(int playerPosition) {
        return playerRewards.stream().filter(x -> x.getPlayerPosition() == playerPosition).findFirst().orElse(null);
    }

    public static double calculateAverageWeight(List<ItemReward> itemRewards) {
        double totalWeight = 0.0;
        int totalItems = 0;

        for (ItemReward itemReward : itemRewards) {
            Double weight = itemReward.getWeight();
            if (weight != null) {
                totalWeight += weight;
                totalItems++;
            }
        }

        return (totalItems > 0) ? totalWeight / totalItems : 20.0; // default weight
    }

    public static List<ItemReward> selectItemRewardsByWeight(List<ItemReward> rewards, int n, double defaultWeight) {
        List<ItemReward> selectedRewards = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        for (ItemReward reward : rewards) {
            weights.add(reward.getWeight() != null ? reward.getWeight() : defaultWeight);
        }

        for (int i = 0; i < n; i++) {
            ItemReward selectedReward = getItemRewardByWeight(rewards, weights);
            selectedRewards.add(selectedReward);
        }
        return selectedRewards;
    }

    private static ItemReward getItemRewardByWeight(List<ItemReward> rewards, List<Double> weights) {
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = Math.random() * totalWeight;

        double cumulativeWeight = 0.0;
        for (int i = 0; i < rewards.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue < cumulativeWeight) {
                return rewards.get(i);
            }
        }
        return rewards.getLast(); // fallback
    }

    public void assignItemRewardsToSlots(List<ItemReward> selectedRewards) {
        Collections.shuffle(selectedRewards);
        for (int i = 0; i < selectedRewards.size(); i++) {
            addSlotReward((byte) i, selectedRewards.get(i));
        }
    }

    public ItemReward getSlotReward(byte slot) {
        return slotRewards.get(slot);
    }

    @Getter
    @Setter
    public static class ItemReward {
        private int productIndex;
        private int productAmount;
        private Double weight;
        private AtomicBoolean claimed;
        private short claimedPlayerPosition;

        public ItemReward(int productIndex, int productAmount, Double weight) {
            this.productIndex = productIndex;
            this.productAmount = productAmount;
            this.weight = weight;
            this.claimed = new AtomicBoolean(false);
            this.claimedPlayerPosition = -1;
        }
    }
}
