package com.jftse.emulator.server.core.client;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.item.ItemPart;
import com.jftse.entities.database.model.player.*;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.item.EItemCategory;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.shared.PlayerLoadType;
import com.jftse.server.core.util.Time;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class FTPlayer {
    private final ServiceManager sm;
    private final JdbcUtil jdbcUtil;

    @Getter(lombok.AccessLevel.NONE)
    private Player entity;

    private long id;
    private long accountId;
    private long pocketId;
    private long playerStatisticId;
    @Setter private Long guildMemberId;
    @Setter private GuildView guild;
    @Setter private Long coupleId;
    @Setter private String coupleName = "";
    @Setter private PlayerStatisticView playerStatistic;

    private String name;
    private int level;
    private int expPoints;
    private int gold;
    private int couplePoints;
    private int playerType;
    private int strength;
    private int stamina;
    private int dexterity;
    private int willpower;
    private int statusPoints;

    private EquippedItemParts itemPartsPPId;
    private EquippedItemParts itemPartsItemIndex;

    @Setter private EquippedQuickSlots quickSlots;
    @Setter private EquippedToolSlots toolSlots;
    @Setter private EquippedSpecialSlots specialSlots;
    @Setter private EquippedCardSlots cardSlots;
    @Setter private EquippedPetSlots petSlots;

    private EquippedItemStats itemStats;

    private PlayerLoadType loadType;

    private static final int UPDATE_INTERVAL_MINUTES = 1;
    private static final long UPDATE_INTERVAL_NS = 1_000_000_000L * 60 * UPDATE_INTERVAL_MINUTES;

    @Getter(lombok.AccessLevel.NONE)
    private long lastUpdateTime;

    private FTPlayer() {
        this.sm = ServiceManager.getInstance();
        this.jdbcUtil = sm.getJdbcUtil();
    }

    private FTPlayer(Player player) {
        this();

        this.entity = player;
        this.lastUpdateTime = Time.getNSTime();

        this.id = player.getId();
        this.accountId = player.getAccount().getId();

        this.name = player.getName();
        this.level = player.getLevel();
        this.expPoints = player.getExpPoints();
        this.gold = player.getGold();
        this.couplePoints = player.getCouplePoints();
        this.playerType = player.getPlayerType();
        this.strength = player.getStrength();
        this.stamina = player.getStamina();
        this.dexterity = player.getDexterity();
        this.willpower = player.getWillpower();
        this.statusPoints = player.getStatusPoints();

        this.pocketId = player.getPocket().getId();
        this.playerStatisticId = player.getPlayerStatistic().getId();
    }

    public static FTPlayer init(Player player) {
        return new FTPlayer(player);
    }

    private void handleLoadType() {
        switch (loadType) {
            case FULL_EQUIPMENT -> {
                loadItemParts(getPlayer());
                quickSlots = EquippedQuickSlots.of(getPlayer());
                toolSlots = EquippedToolSlots.of(getPlayer());
                specialSlots = EquippedSpecialSlots.of(getPlayer());
                cardSlots = EquippedCardSlots.of(getPlayer());
            }
            case EQUIPPED_ITEM_PARTS -> loadItemParts(getPlayer());
            case EQUIPPED_QUICK_SLOTS -> quickSlots = EquippedQuickSlots.of(getPlayer());
            case EQUIPPED_TOOL_SLOTS -> toolSlots = EquippedToolSlots.of(getPlayer());
            case EQUIPPED_SPECIAL_SLOTS -> specialSlots = EquippedSpecialSlots.of(getPlayer());
            case EQUIPPED_CARD_SLOTS -> cardSlots = EquippedCardSlots.of(getPlayer());
        }
    }

    public static FTPlayer initWithFullEquipment(Player player) {
        FTPlayer ftPlayer = initWithEquippedItemParts(player);
        ftPlayer.quickSlots = EquippedQuickSlots.of(player);
        ftPlayer.toolSlots = EquippedToolSlots.of(player);
        ftPlayer.specialSlots = EquippedSpecialSlots.of(player);
        ftPlayer.cardSlots = EquippedCardSlots.of(player);
        ftPlayer.loadType = PlayerLoadType.FULL_EQUIPMENT;
        return ftPlayer;
    }

    public static FTPlayer initWithEquippedItemParts(Player player) {
        FTPlayer ftPlayer = init(player);
        ftPlayer.loadItemParts(player);
        ftPlayer.loadType = PlayerLoadType.EQUIPPED_ITEM_PARTS;
        return ftPlayer;
    }

    public static FTPlayer initWithEquippedQuickSlots(Player player) {
        FTPlayer ftPlayer = init(player);
        ftPlayer.quickSlots = EquippedQuickSlots.of(player);
        ftPlayer.loadType = PlayerLoadType.EQUIPPED_QUICK_SLOTS;
        return ftPlayer;
    }

    public static FTPlayer initWithEquippedToolSlots(Player player) {
        FTPlayer ftPlayer = init(player);
        ftPlayer.toolSlots = EquippedToolSlots.of(player);
        ftPlayer.loadType = PlayerLoadType.EQUIPPED_TOOL_SLOTS;
        return ftPlayer;
    }

    public static FTPlayer initWithEquippedSpecialSlots(Player player) {
        FTPlayer ftPlayer = init(player);
        ftPlayer.specialSlots = EquippedSpecialSlots.of(player);
        ftPlayer.loadType = PlayerLoadType.EQUIPPED_SPECIAL_SLOTS;
        return ftPlayer;
    }

    public static FTPlayer initWithEquippedCardSlots(Player player) {
        FTPlayer ftPlayer = init(player);
        ftPlayer.cardSlots = EquippedCardSlots.of(player);
        ftPlayer.loadType = PlayerLoadType.EQUIPPED_CARD_SLOTS;
        return ftPlayer;
    }

    public void loadItemParts(Player player) {
        ClothEquipment eq = player.getClothEquipment();
        this.itemPartsItemIndex = EquippedItemParts.of(
                this.id,
                eq.getHair(),
                eq.getFace(),
                eq.getDress(),
                eq.getPants(),
                eq.getSocks(),
                eq.getShoes(),
                eq.getGloves(),
                eq.getRacket(),
                eq.getGlasses(),
                eq.getBag(),
                eq.getHat(),
                eq.getDye()
        );

        List<Integer> indexes = this.itemPartsItemIndex.toList();

        List<PlayerPocket> playerPocketList = jdbcUtil.execute(em -> {
            return em.createQuery("SELECT pp FROM PlayerPocket pp WHERE pp.pocket.id = :pocketId AND pp.category = :category AND pp.itemIndex IN :itemIndexes", PlayerPocket.class)
                    .setParameter("pocketId", pocketId)
                    .setParameter("category", EItemCategory.PARTS.getName())
                    .setParameter("itemIndexes", indexes)
                    .getResultList();
        });

        Map<Integer, Integer> indexToPlayerPocketId = playerPocketList.stream()
                .collect(Collectors.toMap(
                        PlayerPocket::getItemIndex,
                        pp -> pp.getId().intValue(),
                        (e, r) -> e));

        this.itemPartsPPId = EquippedItemParts.of(
                this.id,
                indexToPlayerPocketId.getOrDefault(eq.getHair(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getFace(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getDress(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getPants(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getSocks(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getShoes(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getGloves(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getRacket(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getGlasses(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getBag(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getHat(), 0),
                indexToPlayerPocketId.getOrDefault(eq.getDye(), 0)
        );
        loadItemStats(indexes, playerPocketList);
    }

    private void loadItemStats(List<Integer> itemIndexList, List<PlayerPocket> playerPocketList) {
        List<ItemPart> itemPartList = jdbcUtil.execute(em -> {
            return em.createQuery("SELECT ip FROM ItemPart ip WHERE ip.itemIndex IN :itemIndexes", ItemPart.class)
                    .setParameter("itemIndexes", itemIndexList)
                    .getResultList();
        });

        int strength = 0;
        int stamina = 0;
        int dexterity = 0;
        int willpower = 0;
        int addHp = 0;
        int enchantStr = 0;
        int enchantSta = 0;
        int enchantDex = 0;
        int enchantWil = 0;

        for (ItemPart itemPart : itemPartList) {
            strength += itemPart.getStrength();
            stamina += itemPart.getStamina();
            dexterity += itemPart.getDexterity();
            willpower += itemPart.getWillpower();
            addHp += itemPart.getAddHp();
        }

        for (PlayerPocket playerPocket : playerPocketList) {
            enchantStr += playerPocket.getEnchantStr();
            enchantSta += playerPocket.getEnchantSta();
            enchantDex += playerPocket.getEnchantDex();
            enchantWil += playerPocket.getEnchantWil();
        }

        EquippedItemStats itemStats = new EquippedItemStats();
        itemStats.setStrength(strength);
        itemStats.setStamina(stamina);
        itemStats.setDexterity(dexterity);
        itemStats.setWillpower(willpower);
        itemStats.setAddHp(addHp);
        itemStats.setEnchantStr(enchantStr);
        itemStats.setEnchantSta(enchantSta);
        itemStats.setEnchantDex(enchantDex);
        itemStats.setEnchantWil(enchantWil);
        this.itemStats = itemStats;
    }

    public void loadQuickSlots() {
        QuickSlotEquipment eq = sm.getQuickSlotEquipmentService().findById(getQuickSlots().id());
        this.quickSlots = new EquippedQuickSlots(
                eq.getId(),
                eq.getSlot1(),
                eq.getSlot2(),
                eq.getSlot3(),
                eq.getSlot4(),
                eq.getSlot5()
        );
    }

    public void loadToolSlots() {
        ToolSlotEquipment eq = sm.getToolSlotEquipmentService().findById(getToolSlots().id());
        this.toolSlots = new EquippedToolSlots(
                eq.getId(),
                eq.getSlot1(),
                eq.getSlot2(),
                eq.getSlot3(),
                eq.getSlot4(),
                eq.getSlot5()
        );
    }

    public void loadSpecialSlots() {
        SpecialSlotEquipment eq = sm.getSpecialSlotEquipmentService().findById(getSpecialSlots().id());
        this.specialSlots = new EquippedSpecialSlots(
                eq.getId(),
                eq.getSlot1(),
                eq.getSlot2(),
                eq.getSlot3(),
                eq.getSlot4()
        );
    }

    public void loadCardSlots() {
        CardSlotEquipment eq = sm.getCardSlotEquipmentService().findById(getCardSlots().id());
        this.cardSlots = new EquippedCardSlots(
                eq.getId(),
                eq.getSlot1(),
                eq.getSlot2(),
                eq.getSlot3(),
                eq.getSlot4()
        );
    }

    public void loadPetSlots() {
        BattlemonSlotEquipment eq = sm.getBattlemonSlotEquipmentService().findById(getPetSlots().id());
        this.petSlots = new EquippedPetSlots(
                eq.getId(),
                eq.getSlot1(),
                eq.getSlot2()
        );
    }

    public Player getPlayerRef() {
        return sm.getPlayerService().getPlayerRef(this.id);
    }

    public Player getPlayer() {
        if (this.lastUpdateTime + UPDATE_INTERVAL_NS < Time.getNSTime()) {
            // fetch with equipment since at this point, this method gets called after init/handshake which requires full equipment data
            update(sm.getPlayerService().findWithEquipmentById(this.id));
        }
        return this.entity;
    }

    public void update(Player player) {
        if (this.id != player.getId()) {
            throw new IllegalArgumentException("Player ID mismatch during update.");
        }

        syncDetails(player);
        handleLoadType();
    }

    public void sync(Player player) {
        if (this.id != player.getId()) {
            throw new IllegalArgumentException("Player ID mismatch during sync.");
        }

        syncDetails(player);
    }

    private void syncDetails(Player player) {
        this.entity = player;
        this.lastUpdateTime = Time.getNSTime();

        this.name = this.entity.getName();
        this.level = this.entity.getLevel();
        this.expPoints = this.entity.getExpPoints();
        this.gold = this.entity.getGold();
        this.couplePoints = this.entity.getCouplePoints();
        this.playerType = this.entity.getPlayerType();
        this.strength = this.entity.getStrength();
        this.stamina = this.entity.getStamina();
        this.dexterity = this.entity.getDexterity();
        this.willpower = this.entity.getWillpower();
        this.statusPoints = this.entity.getStatusPoints();
    }

    public void syncGold(int newGold) {
        this.gold = newGold;
        this.entity.setGold(newGold);
        this.lastUpdateTime = Time.getNSTime();
    }

    public void syncExpPoints(int newExpPoints) {
        this.expPoints = newExpPoints;
        this.entity.setExpPoints(newExpPoints);
        this.lastUpdateTime = Time.getNSTime();
    }

    public void syncLevel(int newLevel) {
        this.level = newLevel;
        this.entity.setLevel((byte) newLevel);
        this.lastUpdateTime = Time.getNSTime();
    }

    public void syncLevelAndExpPoints(int newLevel, int newExpPoints) {
        this.level = newLevel;
        this.expPoints = newExpPoints;
        this.entity.setLevel((byte) newLevel);
        this.entity.setExpPoints(newExpPoints);
        this.lastUpdateTime = Time.getNSTime();
    }

    public void syncStats(int strength, int stamina, int dexterity, int willpower, int statusPoints) {
        this.strength = strength;
        this.stamina = stamina;
        this.dexterity = dexterity;
        this.willpower = willpower;
        this.statusPoints = statusPoints;
        this.entity.setStrength((byte) strength);
        this.entity.setStamina((byte) stamina);
        this.entity.setDexterity((byte) dexterity);
        this.entity.setWillpower((byte) willpower);
        this.entity.setStatusPoints((byte) statusPoints);
        this.lastUpdateTime = Time.getNSTime();
    }

    public void syncStatusPoints(int statusPoints) {
        this.statusPoints = statusPoints;
        this.entity.setStatusPoints((byte) statusPoints);
        this.lastUpdateTime = Time.getNSTime();
    }

    public void syncCouplePoints(int newCouplePoints) {
        this.couplePoints = newCouplePoints;
        this.entity.setCouplePoints(newCouplePoints);
        this.lastUpdateTime = Time.getNSTime();
    }
}
