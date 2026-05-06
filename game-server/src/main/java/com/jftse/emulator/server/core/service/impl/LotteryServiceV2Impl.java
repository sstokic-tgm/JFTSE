package com.jftse.emulator.server.core.service.impl;

import com.jftse.emulator.common.exception.ValidationException;
import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.RandomUtils;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.lottery.GachaOpenResult;
import com.jftse.emulator.server.core.life.lottery.LotteryResolvedEntry;
import com.jftse.emulator.server.core.service.LotteryServiceV2;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.KStatus;
import com.jftse.entities.database.model.item.Product;
import com.jftse.entities.database.model.lottery.PlayerLotteryProgress;
import com.jftse.entities.database.model.lottery.SLotteryItemPool;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.entities.database.model.pocket.Pocket;
import com.jftse.entities.database.repository.lottery.LotteryItemPoolRepository;
import com.jftse.entities.database.repository.lottery.PlayerLotteryProgressRepository;
import com.jftse.entities.database.repository.pocket.PlayerPocketRepository;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.item.EItemChar;
import com.jftse.server.core.item.EItemUseType;
import com.jftse.server.core.service.PocketService;
import com.jftse.server.core.service.ProductService;
import com.jftse.server.core.shared.ServerConfService;
import com.jftse.server.core.util.GameTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
@Log4j2
public class LotteryServiceV2Impl implements LotteryServiceV2 {
    private final ProductService productService;
    private final PlayerPocketRepository playerPocketRepository;
    private final PocketService pocketService;
    private final LotteryItemPoolRepository lotteryItemPoolRepository;
    private final PlayerLotteryProgressRepository playerLotteryProgressRepository;

    private final ConfigService configService;
    private final ServerConfService serverConfService;

    private final ConcurrentHashMap<String, List<LotteryResolvedEntry>> LOTTERY_CACHE = new ConcurrentHashMap<>();

    private long lastCacheRefreshTime = 0;
    private long CACHE_REFRESH_INTERVAL_MS;

    private int HARD_PITY_THRESHOLD;
    private int SOFT_PITY_START;
    private double MAX_SOFT_PITY_WEIGHT_MULTIPLIER;
    private int LOTTERY_RARITY_OFFSET_STEP;
    private int LOTTERY_MIN_MAX_RARITY_OFFSET;

    @PostConstruct
    public void init() {
        CACHE_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(serverConfService.get("LotteryPoolCacheRefreshInterval", Integer.class));

        HARD_PITY_THRESHOLD = configService.getValue("lottery.hard_pity.threshold", 40);
        SOFT_PITY_START = configService.getValue("lottery.soft_pity.start", 20);
        MAX_SOFT_PITY_WEIGHT_MULTIPLIER = configService.getValue("lottery.soft_pity.max_weight_multiplier", 2.0);
        LOTTERY_RARITY_OFFSET_STEP = configService.getValue("lottery.rarity.offset.step", 3);
        LOTTERY_MIN_MAX_RARITY_OFFSET = configService.getValue("lottery.rarity.offset.min_max", 5);
    }

    private String cacheKey(int playerType, int gachaIndex) {
        return playerType + ":" + gachaIndex;
    }

    private String pocketKey(Product product) {
        return product.getCategory() + ":" + product.getItem0();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<GachaOpenResult> openGacha(FTClient client, String gachaName, int count, BiConsumer<Integer, GachaOpenResult> consumer) throws ValidationException {
        FTPlayer player = client.getPlayer();

        Pocket pocket = pocketService.findById(player.getPocketId());
        if (pocket == null) {
            return failList("Pocket not found.");
        }

        Product gacha = productService.findProductByName(gachaName, EItemCategory.LOTTERY.getName());
        if (gacha == null) {
            return failList("Gacha not found.");
        }

        List<PlayerPocket> gachaPockets = playerPocketRepository.findAllByItemIndexAndCategoryAndPocket(gacha.getItem0(), EItemCategory.LOTTERY.getName(), pocket);
        PlayerPocket gachaPocket = gachaPockets.isEmpty() ? null : gachaPockets.getFirst();
        if (gachaPocket == null) {
            return failList("You do not have this gacha.");
        }

        if (count > gachaPocket.getItemCount())
            count = gachaPocket.getItemCount();
        else if (count <= 0)
            count = 1;

        int gachaIndex = gacha.getItem0();

        List<LotteryResolvedEntry> entries = resolvePoolEntries(player.getPlayerType(), gachaIndex);
        if (entries.isEmpty()) {
            return failList("No items found for this gacha index.");
        }

        PlayerLotteryProgress progress = getOrCreateProgress(player, gachaIndex);

        int maxRarity = entries.stream()
                .mapToInt(LotteryResolvedEntry::getRarityLevel)
                .max()
                .orElse(0);
        int thresholdRarity = calculateThresholdRarity(maxRarity);

        List<GachaOpenResult> results = new ArrayList<>();
        Map<String, PlayerPocket> pocketCache = new HashMap<>();
        for (int i = 0; i < count; i++) {
            gachaPocket.setItemCount(gachaPocket.getItemCount() - 1);

            int pityBefore = progress.getPityCount();

            LotteryResolvedEntry selectedEntry = pickEntryWithSoftPity(entries, pityBefore, thresholdRarity, maxRarity);
            boolean pityGuaranteed = pityBefore >= HARD_PITY_THRESHOLD - 1;
            boolean rareHit = isRareEntry(selectedEntry, thresholdRarity, maxRarity);

            AwardResult awardResult = awardEntry(selectedEntry, pocket, pocketCache);
            progress = updatePityInMemory(progress, rareHit);

            GachaOpenResult gachaOpenResult = GachaOpenResult.builder()
                    .success(true)
                    .consumedGachaRemoved(i == count - 1 && gachaPocket.getItemCount() <= 0)
                    .consumedGachaPocket(i == count - 1 && gachaPocket.getItemCount() > 0 ? gachaPocket : null)
                    .awardedItem(awardResult.awardedItem())
                    .duplicateConverted(awardResult.duplicateConverted())
                    .gachaTokensAdded(awardResult.gachaTokensAdded())
                    .pityBefore(pityBefore)
                    .pityAfter(progress.getPityCount())
                    .pityGuaranteed(pityGuaranteed)
                    .rareItemHit(rareHit)
                    .build();
            results.add(gachaOpenResult);

            if (consumer != null) {
                consumer.accept(i, gachaOpenResult);
            }
        }

        // save pity progress
        playerLotteryProgressRepository.save(progress);

        // consume gacha
        if (gachaPocket.getItemCount() <= 0) {
            pocket.setBelongings(pocket.getBelongings() - 1);
            playerPocketRepository.deleteById(gachaPocket.getId());
        } else {
            playerPocketRepository.save(gachaPocket);
        }

        // save pocket
        pocketService.save(pocket);

        return results;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public GachaOpenResult openGacha(FTClient client, long playerPocketId, int productIndex) throws ValidationException {
        FTPlayer player = client.getPlayer();

        Pocket pocket = pocketService.findById(player.getPocketId());
        if (pocket == null) {
            return fail("Pocket not found.");
        }

        Product gacha = productService.findProductByProductItemIndex(productIndex);
        if (gacha == null || !gacha.getCategory().equals(EItemCategory.LOTTERY.getName())) {
            return fail("Gacha not found.");
        }

        PlayerPocket gachaPocket = playerPocketRepository.findByIdAndPocketId(playerPocketId, pocket.getId()).orElse(null);
        if (gachaPocket == null) {
            return fail("You do not have this gacha.");
        }

        int gachaIndex = gacha.getItem0();

        List<LotteryResolvedEntry> entries = resolvePoolEntries(player.getPlayerType(), gachaIndex);
        if (entries.isEmpty()) {
            return fail("No items found for this gacha index.");
        }

        PlayerLotteryProgress progress = getOrCreateProgress(player, gachaIndex);
        int maxRarity = entries.stream()
                .mapToInt(LotteryResolvedEntry::getRarityLevel)
                .max()
                .orElse(0);
        int thresholdRarity = calculateThresholdRarity(maxRarity);

        Map<String, PlayerPocket> pocketCache = new HashMap<>();

        int pityBefore = progress.getPityCount();

        LotteryResolvedEntry selectedEntry = pickEntryWithSoftPity(entries, pityBefore, thresholdRarity, maxRarity);
        boolean pityGuaranteed = pityBefore >= HARD_PITY_THRESHOLD - 1;
        boolean rareHit = isRareEntry(selectedEntry, thresholdRarity, maxRarity);

        AwardResult awardResult = awardEntry(selectedEntry, pocket, pocketCache);
        updatePity(progress, rareHit);

        ConsumeResult consumeResult = consumeGacha(gachaPocket, pocket);
        if (!consumeResult.removed()) {
            // save pocket
            pocketService.save(pocket);
        }

        return GachaOpenResult.builder()
                .success(true)
                .consumedGachaRemoved(consumeResult.removed())
                .consumedGachaPocket(consumeResult.updatedPocket())
                .awardedItem(awardResult.awardedItem())
                .duplicateConverted(awardResult.duplicateConverted())
                .gachaTokensAdded(awardResult.gachaTokensAdded())
                .pityBefore(pityBefore)
                .pityAfter(progress.getPityCount())
                .pityGuaranteed(pityGuaranteed)
                .rareItemHit(rareHit)
                .build();
    }

    private List<LotteryResolvedEntry> resolvePoolEntries(int playerType, int gachaIndex) {
        List<LotteryResolvedEntry> result = LOTTERY_CACHE.getOrDefault(cacheKey(playerType, gachaIndex), new ArrayList<>());
        if (!result.isEmpty()) {
            final long nowMs = GameTime.getGameTimeMS();
            if (nowMs - lastCacheRefreshTime > CACHE_REFRESH_INTERVAL_MS) {
                // keep DB entries always up to date in cache because they can be change any time and we want to
                // reflect that in game without needing to restart server
                resolveDbPoolEntries(gachaIndex, result);

                HARD_PITY_THRESHOLD = configService.getValue("lottery.hard_pity.threshold", 40);
                SOFT_PITY_START = configService.getValue("lottery.soft_pity.start", 20);
                MAX_SOFT_PITY_WEIGHT_MULTIPLIER = configService.getValue("lottery.soft_pity.max_weight_multiplier", 2.0);
                LOTTERY_RARITY_OFFSET_STEP = configService.getValue("lottery.rarity.offset.step", 3);
                LOTTERY_MIN_MAX_RARITY_OFFSET = configService.getValue("lottery.rarity.offset.min_max", 5);

                lastCacheRefreshTime = nowMs;
            }

            return result;
        }

        String playerTypeName = StringUtils.firstCharToUpperCase(EItemChar.getNameByValue((byte) playerType).toLowerCase());

        // first read from XML
        try {
            InputStream lotteryItemFile = ResourceUtil.getResource("res/lottery/Ini3_Lot_" + (gachaIndex < 10 ? ("0" + gachaIndex) : gachaIndex) + ".xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(lotteryItemFile);

            List<Node> lotteryItemList = document.selectNodes("/LotteryItemList/LotteryItem_" + playerTypeName);

            for (int i = 0; i < lotteryItemList.size(); i++) {

                Node lotteryItem = lotteryItemList.get(i);

                LotteryResolvedEntry entry = new LotteryResolvedEntry();
                entry.setProductIndex(Integer.parseInt(lotteryItem.valueOf("@ShopIndex")));
                entry.setQtyMin(Integer.valueOf(lotteryItem.valueOf("@QuantityMin")));
                entry.setQtyMax(Integer.valueOf(lotteryItem.valueOf("@QuantityMax")));
                entry.setWeight(Double.valueOf(lotteryItem.valueOf("@ChansPer")));
                entry.setGachaIndex(gachaIndex);
                result.add(entry);
            }
            lotteryItemFile.close();
        } catch (DocumentException | IOException de) {
            return new ArrayList<>();
        }

        // then from DB and override if exists else add missing from DB to result list
        resolveDbPoolEntries(gachaIndex, result);

        // we must make sure that if configuration was missing from DB, to add missing the fields
        for (LotteryResolvedEntry entry : result) {
            // we can be sure about only this path missing because if product is missing, it means that whole entry is missing from DB
            // but if product is present, we can be sure that all other fields are present as well because of DB constraints
            if (entry.getProduct() == null) {
                Product product = productService.findProductByProductItemIndex(entry.getProductIndex());
                entry.setProduct(product);

                // when product is missing from DB, it means that all other fields are missing as well, so we set them to default values
                setDefaultValuesForMissingConfig(entry);

                entry.setRarityLevel(0);
                entry.setGachaTokens(null);
            }
        }

        LOTTERY_CACHE.put(cacheKey(playerType, gachaIndex), result);

        return result;
    }

    private void resolveDbPoolEntries(int gachaIndex, List<LotteryResolvedEntry> result) {
        List<SLotteryItemPool> dbEntries = lotteryItemPoolRepository.findByGachaIndexAndStatus(gachaIndex, KStatus.ACTIVE);
        for (SLotteryItemPool dbEntry : dbEntries) {
            LotteryResolvedEntry resolvedEntry = result.stream()
                    .filter(e -> e.getProductIndex() == dbEntry.getProduct().getItem0())
                    .findFirst()
                    .orElse(null);

            if (resolvedEntry != null) {
                if (dbEntry.getQtyMin() != null) resolvedEntry.setQtyMin(dbEntry.getQtyMin());
                if (dbEntry.getQtyMax() != null) resolvedEntry.setQtyMax(dbEntry.getQtyMax());
                if (dbEntry.getWeight() != null) resolvedEntry.setWeight(dbEntry.getWeight());

                resolvedEntry.setProduct(dbEntry.getProduct());
                resolvedEntry.setGachaTokens(dbEntry.getGachaTokens());
                resolvedEntry.setRarityLevel(dbEntry.getRarity() != null ? dbEntry.getRarity().getRarityLevel() : 0);
            } else {
                LotteryResolvedEntry newEntry = new LotteryResolvedEntry();
                newEntry.setProductIndex(dbEntry.getProduct().getItem0());
                newEntry.setQtyMin(dbEntry.getQtyMin() != null ? dbEntry.getQtyMin() : 1);
                newEntry.setQtyMax(dbEntry.getQtyMax() != null ? dbEntry.getQtyMax() : 1);
                newEntry.setWeight(dbEntry.getWeight() != null ? dbEntry.getWeight() : 1.0);
                newEntry.setGachaIndex(gachaIndex);
                newEntry.setProduct(dbEntry.getProduct());
                newEntry.setGachaTokens(dbEntry.getGachaTokens());
                newEntry.setRarityLevel(dbEntry.getRarity() != null ? dbEntry.getRarity().getRarityLevel() : 0);
                result.add(newEntry);
            }
        }
    }

    private PlayerLotteryProgress getOrCreateProgress(FTPlayer player, int gachaIndex) {
        Optional<PlayerLotteryProgress> optProgress = playerLotteryProgressRepository.findByPlayer_IdAndGachaIndex(player.getId(), gachaIndex);
        if (optProgress.isPresent()) {
            return optProgress.get();
        }

        PlayerLotteryProgress newProgress = new PlayerLotteryProgress();
        newProgress.setPlayer(player.getPlayerRef());
        newProgress.setGachaIndex(gachaIndex);
        return playerLotteryProgressRepository.save(newProgress);
    }

    private GachaOpenResult fail(String reason) {
        return GachaOpenResult.builder()
                .success(false)
                .failureReason(reason)
                .build();
    }

    private List<GachaOpenResult> failList(String reason) {
        return List.of(fail(reason));
    }

    private void setDefaultValuesForMissingConfig(LotteryResolvedEntry entry) {
        if (entry.getQtyMin() == null || entry.getQtyMin() <= 0) entry.setQtyMin(1);
        if (entry.getQtyMax() == null || entry.getQtyMax() <= 0) entry.setQtyMax(1);
        if (entry.getWeight() == null || Double.compare(entry.getWeight(), 0.0) <= 0) entry.setWeight(1.0);
    }

    private LotteryResolvedEntry pickEntryWithSoftPity(List<LotteryResolvedEntry> entries, int pityCount, int thresholdRarity, int maxRarity) {
        List<LotteryResolvedEntry> rareEntries = entries.stream()
                .filter(e -> isRareEntry(e, thresholdRarity, maxRarity))
                .toList();

        if (pityCount >= HARD_PITY_THRESHOLD - 1 && !rareEntries.isEmpty()) {
            return weightedPick(rareEntries, 1.0, thresholdRarity, maxRarity);
        }

        double rareMultiplier = calculateRareMultiplier(pityCount);
        return weightedPick(entries, rareMultiplier, thresholdRarity, maxRarity);
    }

    private LotteryResolvedEntry weightedPick(List<LotteryResolvedEntry> entries, double rareMultiplier, int thresholdRarity, int maxRarity) {
        double totalWeight = 0.0;
        double[] effectiveWeights = new double[entries.size()];

        for (int i = 0; i < entries.size(); i++) {
            LotteryResolvedEntry entry = entries.get(i);

            double weight = entry.getWeight() != null ? entry.getWeight() : 1.0;
            if (isRareEntry(entry, thresholdRarity, maxRarity)) {
                double scaledMultiplier;

                if (maxRarity > 0) {
                    int rarityDiff = entry.getRarityLevel() - thresholdRarity + 1;
                    int raritySpan = Math.max(1, maxRarity - thresholdRarity + 1);

                    scaledMultiplier = 1.0 + (rareMultiplier - 1.0) * (rarityDiff / (double) raritySpan);
                } else {
                    scaledMultiplier = rareMultiplier;
                }

                weight *= scaledMultiplier;
            }

            effectiveWeights[i] = Math.max(weight, 0.0);
            totalWeight += effectiveWeights[i];
        }

        if (totalWeight <= 0.0) {
            // if all weights are zero or negative, fallback to equal probability
            return entries.get(RandomUtils.random.nextInt(entries.size()));
        }

        double roll = RandomUtils.random.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (int i = 0; i < entries.size(); i++) {
            cumulative += effectiveWeights[i];
            if (roll < cumulative) {
                return entries.get(i);
            }
        }

        return entries.getFirst(); // fallback, should not happen
    }

    private boolean isRareEntry(LotteryResolvedEntry entry, int thresholdRarity, int maxRarity) {
        if (maxRarity <= 0) {
            return entry.getRarityLevel() <= 0 && entry.getWeight() != null && entry.getWeight() <= 1.0;
        }
        return entry.getRarityLevel() >= thresholdRarity;
    }

    private int calculateThresholdRarity(int maxRarity) {
        if (maxRarity <= 0) {
            return 0;
        }

        int offset = calculateThresholdRarityOffset(maxRarity);
        return Math.max(1, maxRarity - offset);
    }

    private int calculateThresholdRarityOffset(int maxRarityLevel) {
        if (maxRarityLevel < LOTTERY_MIN_MAX_RARITY_OFFSET) return 0;

        return 1 + (maxRarityLevel - LOTTERY_MIN_MAX_RARITY_OFFSET) / LOTTERY_RARITY_OFFSET_STEP;
    }

    private AwardResult awardEntry(LotteryResolvedEntry entry, Pocket pocket, Map<String, PlayerPocket> pocketCache) throws ValidationException {
        Product winningItem = entry.getProduct();
        if (winningItem == null) {
            winningItem = productService.findProductByProductItemIndex(entry.getProductIndex());
        }

        if (winningItem == null) {
            throw new ValidationException("Winning item not found for product index: " + entry.getProductIndex());
        }

        int quantity = getRewardQuantity(entry);
        String pocketKey = pocketKey(winningItem);

        PlayerPocket existingPocket = pocketCache.get(pocketKey);
        if (existingPocket == null) {
            List<PlayerPocket> pockets = playerPocketRepository.findAllByItemIndexAndCategoryAndPocket(winningItem.getItem0(), winningItem.getCategory(), pocket);
            existingPocket = pockets.isEmpty() ? null : pockets.getFirst();
            if (existingPocket != null) {
                pocketCache.put(pocketKey, existingPocket);
            }
        }

        boolean existingItem = existingPocket != null;
        boolean duplicatePart = existingItem && EItemCategory.PARTS.getName().equalsIgnoreCase(existingPocket.getCategory());

        int gachaTokensAdded = entry.getGachaTokens() != null ? entry.getGachaTokens() : 0;

        if (duplicatePart) {
            return new AwardResult(existingPocket, true, gachaTokensAdded);
        }

        PlayerPocket targetPocket = existingItem ? existingPocket : new PlayerPocket();
        int existingCount = existingItem ? targetPocket.getItemCount() : 0;

        targetPocket.setCategory(winningItem.getCategory());
        targetPocket.setItemIndex(winningItem.getItem0());
        targetPocket.setUseType(winningItem.getUseType());
        targetPocket.setItemCount(existingCount + quantity);
        targetPocket.setPocket(pocket);

        if (EItemUseType.TIME.getName().equalsIgnoreCase(targetPocket.getUseType())) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, targetPocket.getItemCount());
            targetPocket.setCreated(cal.getTime());
            targetPocket.setItemCount(1);
        }

        PlayerPocket savedPocket = playerPocketRepository.save(targetPocket);
        pocketCache.put(pocketKey, savedPocket);

        if (!existingItem) {
            pocket.setBelongings(pocket.getBelongings() + 1);
        }

        return new AwardResult(savedPocket, false, gachaTokensAdded);
    }

    private double calculateRareMultiplier(int pityCount) {
        if (pityCount < SOFT_PITY_START) {
            return 1.0;
        }

        int softPityRange = (HARD_PITY_THRESHOLD - 1) - SOFT_PITY_START;
        softPityRange = Math.max(softPityRange, 1);

        int softPityProgress = pityCount - SOFT_PITY_START;

        double multiplier = 1.0 + ((double) softPityProgress / softPityRange) * (MAX_SOFT_PITY_WEIGHT_MULTIPLIER - 1.0);
        return Math.min(multiplier, MAX_SOFT_PITY_WEIGHT_MULTIPLIER);
    }

    private int getRewardQuantity(LotteryResolvedEntry entry) {
        int min = entry.getQtyMin() != null && entry.getQtyMin() > 0 ? entry.getQtyMin() : 1;
        int max = entry.getQtyMax() != null && entry.getQtyMax() > 0 ? entry.getQtyMax() : min;

        if (max < min) {
            return min;
        }

        if (min == max) {
            return min;
        }

        return min + RandomUtils.random.nextInt(max - min + 1);
    }

    private PlayerLotteryProgress updatePity(PlayerLotteryProgress progress, boolean rareHit) {
        return playerLotteryProgressRepository.save(updatePityInMemory(progress, rareHit));
    }

    private PlayerLotteryProgress updatePityInMemory(PlayerLotteryProgress progress, boolean rareHit) {
        progress.setLifetimePullCount(progress.getLifetimePullCount() + 1L);
        progress.setLastPullAt(Instant.now());

        if (rareHit) {
            progress.setPityCount(0);
            progress.setLastResetAt(Instant.now());
        } else {
            progress.setPityCount(progress.getPityCount() + 1);
        }
        return progress;
    }

    private ConsumeResult consumeGacha(PlayerPocket gachaPocket, Pocket pocket) {
        int nextCount = gachaPocket.getItemCount() - 1;
        if (nextCount <= 0) {
            pocket.setBelongings(pocket.getBelongings() - 1);
            pocketService.save(pocket);
            playerPocketRepository.deleteById(gachaPocket.getId());

            return new ConsumeResult(true, null);
        }

        gachaPocket.setItemCount(nextCount);
        PlayerPocket savedPocket = playerPocketRepository.save(gachaPocket);
        return new ConsumeResult(false, savedPocket);
    }

    private record ConsumeResult(boolean removed, PlayerPocket updatedPocket) {
    }

    private record AwardResult(PlayerPocket awardedItem, boolean duplicateConverted, int gachaTokensAdded) {
    }
}
