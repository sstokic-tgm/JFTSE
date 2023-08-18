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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Log4j2
public class FruitManager {
    private final List<FruitDrop> fruitDrops;
    private final Random random;

    private FruitTree fruitTree;

    public FruitManager() {
        fruitDrops = new ArrayList<>();
        random = new Random();
    }

    public void init(short x, short y) {
        fruitTree = new FruitTree(x, y);

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
                    fruitDrops.stream()
                            .filter(fd -> fd.getSong() == song)
                            .findFirst()
                            .ifPresent(fd -> fd.addProbability(itemProbability));
                }

            } catch (DocumentException e) {
                log.error("Failed to load fruit drop data", e);
            }
        } else {
            log.error("FruitingItem.xml not found");
        }
    }

    public void addFruitDrop(FruitDrop fruitDrop) {
        fruitDrops.add(fruitDrop);
    }

    public FruitTree pickRandomItem(int song, int score) {
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

        int totalProbability = fruitDrops.stream().filter(fd -> fd.getSong() == song && fd.getScore() <= score).mapToInt(FruitDrop::getProbability).sum();
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
}
