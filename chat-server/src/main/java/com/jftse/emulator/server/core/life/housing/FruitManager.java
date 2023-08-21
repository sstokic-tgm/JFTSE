package com.jftse.emulator.server.core.life.housing;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.item.ItemMaterial;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class FruitManager {
    private final List<FruitDrop> fruitDrops;
    private final Random random;

    public final static double MINIMUM_FRUIT_SPAWN_TIME = 33.0; // seconds
    public final static double MAXIMUM_FRUIT_SPAWN_TIME = 34.0; // seconds
    public final static int MAXIMUM_FRUITS_PER_TREE = 3;

    private FruitTree fruitTree;
    private final List<FruitTree> fruitTrees;

    public FruitManager() {
        fruitDrops = new ArrayList<>();
        fruitTrees = new ArrayList<>();
        random = new Random();
    }

    public boolean init(short x, short y) {
        Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        fruitTree = fruitTrees.stream()
                .filter(ft -> ft.getX() == x && ft.getY() == y)
                .findFirst()
                .orElse(null);

        if (fruitTree == null) {
            fruitTree = new FruitTree(x, y);
        } else {
            if (fruitTree.getLastFruitPickTime() != null) {
                long timeDifferenceMillis = currentTime.getTimeInMillis() - fruitTree.getLastFruitPickTime().getTimeInMillis();
                double timeDifferenceSeconds = timeDifferenceMillis / 1000.0;

                int fruitsToAdd = (int) (timeDifferenceSeconds / MINIMUM_FRUIT_SPAWN_TIME);
                if (fruitsToAdd > 0) {
                    int maxFruitsToAdd = Math.min((int) (timeDifferenceSeconds / MAXIMUM_FRUIT_SPAWN_TIME), MAXIMUM_FRUITS_PER_TREE);
                    fruitTree.setAvailableFruits(Math.min(fruitTree.getAvailableFruits() + maxFruitsToAdd, MAXIMUM_FRUITS_PER_TREE));
                }
            }

            if (fruitTree.getAvailableFruits() == 0) {
                return false;
            }
        }

        if (!fruitTrees.contains(fruitTree)) {
            fruitTrees.add(fruitTree);
        }

        if (!fruitDrops.isEmpty()) {
            fruitDrops.clear();
        }

        Optional<Path> fruitDropPath = ResourceUtil.getPath("housing/FruitingItem.xml");
        if (fruitDropPath.isPresent()) {
            Path path = fruitDropPath.get();

            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            try {
                Document document = reader.read(path.toFile());

                List<Node> itemCountNodes = document.selectNodes("/Tables/ItemCount");
                List<Node> itemProbabilityNodes = document.selectNodes("/Tables/ItemProbability");

                for (Node itemCountNode : itemCountNodes) {
                    int song = Integer.parseInt(itemCountNode.valueOf("@Song"));
                    int score = Integer.parseInt(itemCountNode.valueOf("@Score"));
                    int probability = Integer.parseInt(itemCountNode.valueOf("@Probability"));
                    int itemCount = Integer.parseInt(itemCountNode.valueOf("@ItemCount"));

                    FruitDrop fruitDrop = new FruitDrop(song, score, probability, itemCount);
                    addFruitDrop(fruitDrop);
                }

                for (Node itemProbabilityNode : itemProbabilityNodes) {
                    int song = Integer.parseInt(itemProbabilityNode.valueOf("@Song"));
                    int score = Integer.parseInt(itemProbabilityNode.valueOf("@Score"));
                    int probability = Integer.parseInt(itemProbabilityNode.valueOf("@Probability"));
                    int itemIndex = Integer.parseInt(itemProbabilityNode.valueOf("@ItemIndex"));

                    ItemMaterial item = ServiceManager.getInstance().getItemMaterialService().findByItemIndex(itemIndex).orElse(null);

                    ItemProbability itemProbability = new ItemProbability(score, probability, item);
                    for (FruitDrop fruitDrop : fruitDrops) {
                        if (fruitDrop.getSong() == song) {
                            fruitDrop.addProbability(itemProbability);
                        }
                    }
                }

            } catch (DocumentException e) {
                log.error("Failed to load fruit drop data", e);
                return false;
            }
        } else {
            log.error("FruitingItem.xml not found");
            return false;
        }
        return true;
    }

    public void addFruitDrop(FruitDrop fruitDrop) {
        fruitDrops.add(fruitDrop);
    }

    public FruitTree pickRandomItem(int song, int score) {
        fruitTree.setLastFruitPickTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        fruitTree.setAvailableFruits(fruitTree.getAvailableFruits() - 1);

        List<ItemProbability> applicableItems = findApplicableItems(song, score);

        if (applicableItems.isEmpty()) {
            return fruitTree; // No item drop for the given song and score
        }

        ItemProbability selectedProbability = rollItemProbability(applicableItems, score);
        if (selectedProbability == null) {
            return fruitTree; // No item drop for the given song and score
        }

        FruitReward fruitReward = new FruitReward(selectedProbability.getItem(), selectedProbability.getQuantity());
        fruitTree.setFruitReward(fruitReward);
        return fruitTree;
    }

    private List<ItemProbability> findApplicableItems(int song, int score) {
        List<ItemProbability> applicableItems = new ArrayList<>();

        int totalProbability = fruitDrops.stream()
                .filter(fd -> fd.getSong() == song && fd.getScore() <= score)
                .mapToInt(FruitDrop::getProbability)
                .sum();
        int randomValue = random.nextInt(totalProbability) + 1;

        for (FruitDrop fruitDrop : fruitDrops) {
            if (fruitDrop.getSong() == song && fruitDrop.getScore() <= score) {
                randomValue -= fruitDrop.getProbability();
                if (randomValue <= 0) {
                    List<ItemProbability> itemProbabilities = fruitDrop.getItemProbabilities();
                    itemProbabilities.forEach(ip -> ip.setQuantity(fruitDrop.getItemCount()));

                    applicableItems.addAll(itemProbabilities);
                    break;
                }
            }
        }

        return applicableItems;
    }

    private List<ItemProbability> findApplicableItems2(int song, int score) {
        List<ItemProbability> applicableItems = new ArrayList<>();

        final int maxScore = 100;
        final int difference = maxScore - score;
        final int lowerBound = Math.max(0, score - difference);

        List<FruitDrop> applicableFruitDrops = fruitDrops.stream()
                .filter(fd -> fd.getSong() == song && fd.getScore() <= score && fd.getScore() >= lowerBound)
                .collect(Collectors.toList());

        int totalAdjustedProbability = applicableFruitDrops.stream()
                .mapToInt(FruitDrop::getProbability)
                .sum();
        int randomValue = random.nextInt(totalAdjustedProbability) + 1;

        for (FruitDrop fruitDrop : applicableFruitDrops) {
            randomValue -= fruitDrop.getProbability();
            if (randomValue <= 0) {
                List<ItemProbability> itemProbabilities = fruitDrop.getItemProbabilities();
                log.info(itemProbabilities.size());
                itemProbabilities.forEach(ip -> ip.setQuantity(fruitDrop.getItemCount()));

                applicableItems.addAll(itemProbabilities);
                break;
            }
        }

        return applicableItems;
    }

    private List<ItemProbability> findApplicableItems3(int song, int score) {
        List<ItemProbability> applicableItems = new ArrayList<>();

        List<FruitDrop> applicableFruitDrops = fruitDrops.stream()
                .filter(fd -> fd.getSong() == song && fd.getScore() <= score)
                .collect(Collectors.toList());

        int maxPossibleProbability = applicableFruitDrops.stream()
                .mapToInt(FruitDrop::getProbability)
                .max()
                .orElse(0);

        int totalAdjustedProbability = applicableFruitDrops.stream()
                .mapToInt(fd -> (int) (fd.getProbability() * (1.0 - (fd.getScore() / 100.0) + (fd.getProbability() / maxPossibleProbability))))
                .sum();

        int randomValue = random.nextInt(totalAdjustedProbability) + 1;

        for (FruitDrop fruitDrop : applicableFruitDrops) {
            int adjustedProbability = (int) (fruitDrop.getProbability() * (1.0 - (fruitDrop.getScore() / 100.0) + (fruitDrop.getProbability() / maxPossibleProbability)));
            randomValue -= adjustedProbability;
            if (randomValue <= 0) {
                List<ItemProbability> itemProbabilities = fruitDrop.getItemProbabilities();
                itemProbabilities.forEach(ip -> ip.setQuantity(fruitDrop.getItemCount()));

                applicableItems.addAll(itemProbabilities);
                break;
            }
        }

        return applicableItems;
    }

    private ItemProbability rollItemProbability(List<ItemProbability> probabilities, int score) {
        int totalProbability = probabilities.stream().filter(ip -> ip.getScore() <= score).mapToInt(ItemProbability::getProbability).sum();
        int randomValue = random.nextInt(totalProbability) + 1;

        for (ItemProbability probability : probabilities) {
            randomValue -= probability.getProbability();
            if (randomValue <= 0) {
                return probability;
            }
        }

        return null;
    }

    public FruitTree getFruitTree() {
        return fruitTree;
    }

    public List<FruitTree> getFruitTrees() {
        return fruitTrees;
    }
}
