package com.jftse.emulator.server.module;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.entities.database.model.battle.BossGuardian;
import com.jftse.entities.database.model.battle.Guardian;
import com.jftse.entities.database.model.battle.Skill;
import com.jftse.entities.database.model.battle.SkillDropRate;
import com.jftse.entities.database.model.challenge.Challenge;
import com.jftse.entities.database.model.item.*;
import com.jftse.entities.database.model.level.LevelExp;
import com.jftse.entities.database.model.tutorial.Tutorial;
import com.jftse.entities.database.repository.battle.BossGuardianRepository;
import com.jftse.entities.database.repository.battle.GuardianRepository;
import com.jftse.entities.database.repository.battle.SkillDropRateRepository;
import com.jftse.entities.database.repository.battle.SkillRepository;
import com.jftse.entities.database.repository.challenge.ChallengeRepository;
import com.jftse.entities.database.repository.item.*;
import com.jftse.entities.database.repository.level.LevelExpRepository;
import com.jftse.entities.database.repository.tutorial.TutorialRepository;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.thread.ThreadManager;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
@Component
@Order(1)
public class DbDataLoader implements CommandLineRunner {
    @Autowired
    private LevelExpRepository levelExpRepository;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private TutorialRepository tutorialRepository;
    @Autowired
    private ItemPartRepository itemPartRepository;
    @Autowired
    private ItemSpecialRepository itemSpecialRepository;
    @Autowired
    private ItemToolRepository itemToolRepository;
    @Autowired
    private ItemHouseDecoRepository itemHouseDecoRepository;
    @Autowired
    private ItemHouseRepository itemHouseRepository;
    @Autowired
    private ItemEnchantRepository itemEnchantRepository;
    @Autowired
    private ItemRecipeRepository itemRecipeRepository;
    @Autowired
    private ItemMaterialRepository itemMaterialRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SkillRepository skillRepository;
    @Autowired
    private SkillDropRateRepository skillDropRateRepository;
    @Autowired
    private GuardianRepository guardianRepository;
    @Autowired
    private BossGuardianRepository bossGuardianRepository;
    @Autowired
    private ItemCharRepository itemCharRepository;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ThreadManager threadManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading data into the database...");

        boolean dataLoaded = true;

        boolean levelExpInitialized = levelExpRepository.count() == configService.getValue("player.level.max", 60) || levelExpRepository.count() == getCountByFileAndNode("res/LevelExp.xml", "/ExpList/Exp");
        boolean mapQuestInitialized = challengeRepository.count() == getCountByFileAndNode("res/MapQuest.xml", "/Tables/Challenge") && tutorialRepository.count() == getCountByFileAndNode("res/MapQuest.xml", "/Tables/Tutorial");
        boolean itemPartInitialized = itemPartRepository.count() == getCountByFileAndNode("res/Item_Parts_Ini3.xml", "/ItemList/Item");
        boolean itemSpecialInitialized = itemSpecialRepository.count() == getCountByFileAndNode("res/Item_Special.xml", "/ItemList/Item");
        boolean itemToolInitialized = itemToolRepository.count() == getCountByFileAndNode("res/Item_Tools.xml", "/ItemList/Item");
        boolean itemHouseDecoInitialized = itemHouseDecoRepository.count() == getCountByFileAndNode("res/Item_HouseDeco.xml", "/ItemList/Item");
        boolean itemHouseInitialized = itemHouseRepository.count() == getCountByFileAndNode("res/Item_House.xml", "/ItemList/Item");
        boolean itemEnchantInitialized = itemEnchantRepository.count() == getCountByFileAndNode("res/Item_Enchant.xml","/ItemList/Item" );
        boolean itemRecipeInitialized = itemRecipeRepository.count() == getCountByFileAndNode("res/Item_Recipe_Ini3.xml", "/RecipeList/Recipe");
        boolean itemMaterialInitialized = itemMaterialRepository.count() == getCountByFileAndNode("res/Item_Material.xml", "/ItemList/Item");
        boolean itemCharInitialized = itemCharRepository.count() == getCountByFileAndNode("res/Item_Char.xml", "/ItemList/Item");
        boolean productInitialized = productRepository.count() == getCountByFileAndNode("res/Shop_Ini3.xml", "/ProductList/Product");
        boolean skillInitialized = skillRepository.count() == getCountByFileAndNode("res/FieldItem_Skills_Ini3.xml", "/Skills/Skill");
        boolean skillDropRateInitialized = skillDropRateRepository.count() == getCountByFileAndNode("res/FieldItem_DropRates_Ini3.xml", "/SkillDropRates/SkillDropRate");
        boolean guardianInitialized = guardianRepository.count() == getCountByFileAndNode("res/GuardianInfo.xml", "/GuardianList/Guardian");
        boolean bossGuardianInitialized = bossGuardianRepository.count() == getCountByFileAndNode("res/BossGuardianInfo_Ini3.xml", "/GuardianList/Guardian");

        List<Future<?>> futures = new ArrayList<>();
        if (!levelExpInitialized) {
            log.info("Initializing LevelExp...");
            Future<?> future = threadManager.submit(() -> {
                if (loadLevelExp())
                    log.info("LevelExp successfully initialized");
            });
            futures.add(future);
        } else
            dataLoaded = false;

        if (!mapQuestInitialized) {
            log.info("Initializing MapQuest...");
            Future<?> future = threadManager.submit(() -> {
                if (loadMapQuest())
                    log.info("MapQuest successfully initialized");
            });
            futures.add(future);
        } else
            dataLoaded = false;

        if (!itemPartInitialized) {
            log.info("Initializing ItemPart...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemPart())
                    log.info("ItemPart successfully initialized");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemSpecialInitialized) {
            log.info("Initializing ItemSpecial...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemSpecial())
                    log.info("ItemSpecial successfully initialized");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemToolInitialized) {
            log.info("Initializing ItemTool...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemTool())
                    log.info("ItemTool successfully initialized");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemHouseDecoInitialized) {
            log.info("Initializing ItemHouseDeco...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemHouseDeco())
                    log.info("ItemHouseDeco successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemHouseInitialized) {
            log.info("Initializing ItemHouse...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemHouse())
                    log.info("ItemHouse successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemEnchantInitialized) {
            log.info("Initializing ItemEnchant...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemEnchant())
                    log.info("ItemEnchant successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemRecipeInitialized) {
            log.info("Initializing ItemRecipe...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemRecipe())
                    log.info("ItemRecipe successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemMaterialInitialized) {
            log.info("Initializing ItemMaterial...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemMaterial())
                    log.info("ItemMaterial successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!itemCharInitialized) {
            log.info("Initializing ItemChar...");
            Future<?> future = threadManager.submit(() -> {
                if (loadItemChar())
                    log.info("ItemChar successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!productInitialized) {
            log.info("Initializing Product...");
            Future<?> future = threadManager.submit(() -> {
                if (loadProduct())
                    log.info("Product successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!skillInitialized) {
            log.info("Initializing Skill...");
            Future<?> future = threadManager.submit(() -> {
                if (loadSkill())
                    log.info("Skill successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!skillDropRateInitialized) {
            log.info("Initializing SkillDropRate...");
            Future<?> future = threadManager.submit(() -> {
                if (loadSkillDropRate())
                    log.info("SkillDropRate successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!guardianInitialized) {
            log.info("Initializing Guardian...");
            Future<?> future = threadManager.submit(() -> {
                if (loadGuardian())
                    log.info("Guardian successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        if (!bossGuardianInitialized) {
            log.info("Initializing BossGuardian...");
            Future<?> future = threadManager.submit(() -> {
                if (loadBossGuardian())
                    log.info("BossGuardian successfully initialized!");
            });
            futures.add(future);
        }
        else
            dataLoaded = false;

        futures.forEach(f -> {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage(), e);
            }
        });

        if (!dataLoaded)
            log.info("Data is up to date");

        log.info("--------------------------------------");
    }

    private int getCountByFileAndNode(String file, String xpathExpression) throws DocumentException, IOException {
        InputStream itemPartFile = ResourceUtil.getResource(file);
        SAXReader reader = new SAXReader();
        reader.setEncoding("UTF-8");
        Document document = reader.read(itemPartFile);

        int count = document.selectNodes(xpathExpression).size();
        itemPartFile.close();

        return count;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadBossGuardian() {
        try {
            InputStream itemPartFile = ResourceUtil.getResource("res/BossGuardianInfo_Ini3.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemPartFile);

            List<Node> bossGuardianList = document.selectNodes("/GuardianList/Guardian");

            long i = 0;
            for (Node skillNode : bossGuardianList) {
                i++;
                BossGuardian guardian = bossGuardianRepository.findById(i).orElse(new BossGuardian());

                guardian.setName(String.valueOf(skillNode.valueOf("@Name_en")));
                guardian.setHpBase(Integer.valueOf(skillNode.valueOf("@HPBase")));
                guardian.setHpPer(Integer.valueOf(skillNode.valueOf("@HPPer")));
                guardian.setLevel(Integer.valueOf(skillNode.valueOf("@GdLevel")));
                guardian.setBaseStr(Integer.valueOf(skillNode.valueOf("@BaseSTR")));
                guardian.setBaseSta(Integer.valueOf(skillNode.valueOf("@BaseSTA")));
                guardian.setBaseDex(Integer.valueOf(skillNode.valueOf("@BaseDEX")));
                guardian.setBaseWill(Integer.valueOf(skillNode.valueOf("@BaseWILL")));
                guardian.setAddStr(Integer.valueOf(skillNode.valueOf("@AddSTR")));
                guardian.setAddSta(Integer.valueOf(skillNode.valueOf("@AddSTA")));
                guardian.setAddDex(Integer.valueOf(skillNode.valueOf("@AddDEX")));
                guardian.setAddWill(Integer.valueOf(skillNode.valueOf("@AddWILL")));
                guardian.setRewardExp(Integer.valueOf(skillNode.valueOf("@RewardEXP")));
                guardian.setRewardGold(Integer.valueOf(skillNode.valueOf("@RewardGOLD")));
                guardian.setBtItemID(Integer.valueOf(skillNode.valueOf("@BtItemID")));
                bossGuardianRepository.save(guardian);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadGuardian() {
        try {
            InputStream itemPartFile = ResourceUtil.getResource("res/GuardianInfo.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemPartFile);

            List<Node> guardianList = document.selectNodes("/GuardianList/Guardian");

            long i = 0;
            for (Node skillNode : guardianList) {
                i++;
                Guardian guardian = guardianRepository.findById(i).orElse(new Guardian());

                guardian.setName(String.valueOf(skillNode.valueOf("@Name_en")));
                guardian.setHpBase(Integer.valueOf(skillNode.valueOf("@HPBase")));
                guardian.setHpPer(Integer.valueOf(skillNode.valueOf("@HPPer")));
                guardian.setLevel(Integer.valueOf(skillNode.valueOf("@GdLevel")));
                guardian.setBaseStr(Integer.valueOf(skillNode.valueOf("@BaseSTR")));
                guardian.setBaseSta(Integer.valueOf(skillNode.valueOf("@BaseSTA")));
                guardian.setBaseDex(Integer.valueOf(skillNode.valueOf("@BaseDEX")));
                guardian.setBaseWill(Integer.valueOf(skillNode.valueOf("@BaseWILL")));
                guardian.setAddStr(Integer.valueOf(skillNode.valueOf("@AddSTR")));
                guardian.setAddSta(Integer.valueOf(skillNode.valueOf("@AddSTA")));
                guardian.setAddDex(Integer.valueOf(skillNode.valueOf("@AddDEX")));
                guardian.setAddWill(Integer.valueOf(skillNode.valueOf("@AddWILL")));
                guardian.setRewardExp(Integer.valueOf(skillNode.valueOf("@RewardEXP")));
                guardian.setRewardGold(Integer.valueOf(skillNode.valueOf("@RewardGOLD")));
                guardian.setBtItemID(Integer.valueOf(skillNode.valueOf("@BtItemID")));
                guardianRepository.save(guardian);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadSkillDropRate() {
        try {
            InputStream itemPartFile = ResourceUtil.getResource("res/FieldItem_DropRates_Ini3.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemPartFile);

            List<Node> skillDropRateList = document.selectNodes("/SkillDropRates/SkillDropRate");

            long i = 0;
            for (Node node : skillDropRateList) {
                i++;
                SkillDropRate skillDropRate = skillDropRateRepository.findById(i).orElse(new SkillDropRate());
                skillDropRate.setFromLevel(Integer.valueOf(node.valueOf("FromLevel")));
                skillDropRate.setToLevel(Integer.valueOf(node.valueOf("ToLevel")));

                StringBuilder dropRates = new StringBuilder();
                dropRates.append(node.valueOf("ItemDrop0"));
                for (int j = 1; j < 64; j++) {
                    dropRates.append(String.format(",%s", node.valueOf(String.format("ItemDrop%s", j))));
                }

                skillDropRate.setDropRates(dropRates.toString());
                skillDropRateRepository.save(skillDropRate);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadSkill() {
        try {
            InputStream itemPartFile = ResourceUtil.getResource("res/FieldItem_Skills_Ini3.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemPartFile);

            List<Node> skillList = document.selectNodes("/Skills/Skill");

            long i = 0;
            for (Node skillNode : skillList) {
                i++;
                Skill skill = skillRepository.findById(i).orElse(new Skill());
                skill.setName(String.valueOf(skillNode.valueOf("Name")));
                skill.setTexID(Integer.valueOf(skillNode.valueOf("TexID")));
                skill.setIconID(Integer.valueOf(skillNode.valueOf("IconID")));
                skill.setElemental(Integer.valueOf(skillNode.valueOf("Elemental")));
                skill.setShotType(Integer.valueOf(skillNode.valueOf("ShotType")));
                skill.setShotCnt(Integer.valueOf(skillNode.valueOf("ShotCnt")));
                skill.setChantTime(Double.valueOf(skillNode.valueOf("ChantTime")));
                skill.setRandMaxTime(Double.valueOf(skillNode.valueOf("RandMaxTime")));
                skill.setPlayTime(Double.valueOf(skillNode.valueOf("PlayTime")));
                skill.setVibration(String.valueOf(skillNode.valueOf("Vibration")));
                skill.setSoundShotID(Integer.valueOf(skillNode.valueOf("SoundShotID")));
                skill.setSoundHitID(Integer.valueOf(skillNode.valueOf("SoundHitID")));
                skill.setDamage(Integer.valueOf(skillNode.valueOf("Damage")));
                skill.setDamageRate(Integer.valueOf(skillNode.valueOf("DamageRate")));
                skill.setDamageInfo(String.valueOf(skillNode.valueOf("DamageInfo")));
                skill.setProperty(Integer.valueOf(skillNode.valueOf("Property")));
                skill.setTargeting(Integer.valueOf(skillNode.valueOf("Targeting")));
                skill.setTPosition(String.valueOf(skillNode.valueOf("TPosition")));
                skill.setRadius(Double.valueOf(skillNode.valueOf("Radius")));
                skill.setShotSpeed(Double.valueOf(skillNode.valueOf("ShotSpeed")));
                skill.setShotRot(Double.valueOf(skillNode.valueOf("ShotRot")));
                skill.setExplosion(Integer.valueOf(skillNode.valueOf("Explosion")));
                skill.setCoolingTime(Double.valueOf(skillNode.valueOf("CoolingTime")));
                skill.setGdCoolingTime(Double.valueOf(skillNode.valueOf("GdCoolingTime")));
                skill.setAddEftTime0(Double.valueOf(this.valueOf(skillNode, "AddEftTime_0", "0")));
                skill.setAddEftTime1(Double.valueOf( this.valueOf(skillNode, "AddEftTime_1", "0")));
                skill.setAddSspAttTime0(Double.valueOf(this.valueOf(skillNode, "AddSspAttTime_0", "0")));
                skill.setAddSspAttTime1(Double.valueOf(this.valueOf(skillNode, "AddSspAttTime_1", "0")));

                skillRepository.save(skill);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadLevelExp() {

        try {

            InputStream levelExpFile = ResourceUtil.getResource("res/LevelExp.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(levelExpFile);

            List<Node> expList = document.selectNodes("/ExpList/Exp");

            for (int i = 0; i < expList.size(); i++) {

                if (i == configService.getValue("player.level.max", 60))
                    break;

                Node exp = expList.get(i);

                LevelExp levelExp = levelExpRepository.findById((long) (i + 1)).orElse(new LevelExp());
                levelExp.setLevel((byte) i);
                levelExp.setExpValue(Integer.valueOf(exp.valueOf("@Value").trim()));

                levelExpRepository.save(levelExp);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();
            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadMapQuest() {

        try {

            InputStream mapQuestFile = ResourceUtil.getResource("res/MapQuest.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(mapQuestFile);

            List<Node> tutorialList = document.selectNodes("/Tables/Tutorial");
            List<Node> challengeList = document.selectNodes("/Tables/Challenge");

            long i = 0;
            for (Node tutorialNode : tutorialList) {
                i++;
                Tutorial tutorial = tutorialRepository.findById(i).orElse(new Tutorial());

                tutorial.setTutorialIndex(Integer.valueOf(tutorialNode.valueOf("@Index")));
                tutorial.setItemRewardRepeat(!tutorialNode.valueOf("@ItemRewardRepeat").toLowerCase().equals("no"));
                tutorial.setQuantityMin1(Integer.valueOf(tutorialNode.valueOf("@QuantityMin1")));
                tutorial.setQuantityMin2(Integer.valueOf(tutorialNode.valueOf("@QuantityMin2")));
                tutorial.setQuantityMin3(Integer.valueOf(tutorialNode.valueOf("@QuantityMin3")));
                tutorial.setQuantityMax1(Integer.valueOf(tutorialNode.valueOf("@QuantityMax1")));
                tutorial.setQuantityMax2(Integer.valueOf(tutorialNode.valueOf("@QuantityMax2")));
                tutorial.setQuantityMax3(Integer.valueOf(tutorialNode.valueOf("@QuantityMax3")));
                tutorial.setRewardExp(BitKit.fromUnsignedInt(Integer.parseInt(tutorialNode.valueOf("@RewardEXP"))));
                tutorial.setRewardGold(Integer.valueOf(tutorialNode.valueOf("@RewardGOLD")));
                tutorial.setRewardItem1(Integer.valueOf(tutorialNode.valueOf("@RewardItem1")));
                tutorial.setRewardItem2(Integer.valueOf(tutorialNode.valueOf("@RewardItem2")));
                tutorial.setRewardItem3(Integer.valueOf(tutorialNode.valueOf("@RewardItem3")));

                tutorialRepository.save(tutorial);
            }

            i = 0;
            for (Node challengeNode : challengeList) {
                i++;
                Challenge challenge = challengeRepository.findById(i).orElse(new Challenge());

                challenge.setChallengeIndex(Integer.valueOf(challengeNode.valueOf("@Index")));
                challenge.setLevel(Byte.valueOf(challengeNode.valueOf("@Level")));
                challenge.setLevelRestriction(Byte.valueOf(challengeNode.valueOf("@LevelRestriction")));

                String gameModeStr = challengeNode.valueOf("@GameMode");
                Short gameMode = null;

                if (gameModeStr.equals("BASIC"))
                    gameMode = GameMode.BASIC;
                else if (gameModeStr.equals("BATTLE"))
                    gameMode = GameMode.BATTLE;
                else if (gameModeStr.equals("GUARDIAN"))
                    gameMode = GameMode.GUARDIAN;

                challenge.setGameMode(gameMode);

                challenge.setItemRewardRepeat(!challengeNode.valueOf("@ItemRewardRepeat").toLowerCase().equals("no"));
                challenge.setQuantityMin1(Integer.valueOf(challengeNode.valueOf("@QuantityMin1")));
                challenge.setQuantityMin2(Integer.valueOf(challengeNode.valueOf("@QuantityMin2")));
                challenge.setQuantityMin3(Integer.valueOf(challengeNode.valueOf("@QuantityMin3")));
                challenge.setQuantityMax1(Integer.valueOf(challengeNode.valueOf("@QuantityMax1")));
                challenge.setQuantityMax2(Integer.valueOf(challengeNode.valueOf("@QuantityMax2")));
                challenge.setQuantityMax3(Integer.valueOf(challengeNode.valueOf("@QuantityMax3")));
                challenge.setRewardExp(BitKit.fromUnsignedInt(Integer.parseInt(challengeNode.valueOf("@RewardEXP"))));
                challenge.setRewardGold(Integer.valueOf(challengeNode.valueOf("@RewardGOLD")));
                challenge.setRewardItem1(Integer.valueOf(challengeNode.valueOf("@RewardItem1")));
                challenge.setRewardItem2(Integer.valueOf(challengeNode.valueOf("@RewardItem2")));
                challenge.setRewardItem3(Integer.valueOf(challengeNode.valueOf("@RewardItem3")));

                challenge.setHp(Integer.valueOf(challengeNode.valueOf("@HP")));
                challenge.setStr(BitKit.fromUnsignedInt(Integer.parseInt(challengeNode.valueOf("@STR"))));
                challenge.setSta(BitKit.fromUnsignedInt(Integer.parseInt(challengeNode.valueOf("@STA"))));
                challenge.setDex(BitKit.fromUnsignedInt(Integer.parseInt(challengeNode.valueOf("@DEX"))));
                challenge.setWil(BitKit.fromUnsignedInt(Integer.parseInt(challengeNode.valueOf("@WIL"))));

                challengeRepository.save(challenge);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();
            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemPart() {

        try {

            InputStream itemPartFile = ResourceUtil.getResource("res/Item_Parts_Ini3.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemPartFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemPart itemPart = itemPartRepository.findById(i).orElse(new ItemPart());

                itemPart.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemPart.setForPlayer(itemNode.valueOf("@Char"));
                itemPart.setPart(itemNode.valueOf("@Part"));
                itemPart.setEnchantElement(!itemNode.valueOf("@EnchantElement").equals("0"));
                itemPart.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
                itemPart.setName(itemNode.valueOf("@Name_N"));

                itemPart.setLevel(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@Level"))));
                itemPart.setStrength(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@STR"))));
                itemPart.setStamina(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@STA"))));
                itemPart.setDexterity(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@DEX"))));
                itemPart.setWillpower(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@WIL"))));
                itemPart.setAddHp(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddHP"))));
                itemPart.setAddQuick(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddQuick"))));
                itemPart.setAddBuff(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddBuff"))));
                itemPart.setSmashSpeed(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@SmashSpeed"))));
                itemPart.setMoveSpeed(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@MoveSpeed"))));
                itemPart.setChargeshotSpeed(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@ChargeshotSpeed"))));
                itemPart.setLobSpeed(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@LobSpeed"))));
                itemPart.setServeSpeed(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@ServeSpeed"))));
                itemPart.setMaxStrength(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@MAX_STR"))));
                itemPart.setMaxStamina(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@MAX_STA"))));
                itemPart.setMaxDexterity(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@MAX_DEX"))));
                itemPart.setMaxWillpower(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@MAX_WIL"))));
                itemPart.setBallSpin(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@BallSpin"))));
                itemPart.setAtss(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@ATSS"))));
                itemPart.setDfss(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@DFSS"))));
                itemPart.setSocket(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@Socket"))));
                itemPart.setGauge(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@Gauge"))));
                itemPart.setGaugeBattle(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@GaugeBattle"))));

                itemPartRepository.save(itemPart);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemSpecial() {

        try {

            InputStream itemSpecialFile = ResourceUtil.getResource("res/Item_Special.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemSpecialFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemSpecial itemSpecial = itemSpecialRepository.findById(i).orElse(new ItemSpecial());

                itemSpecial.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemSpecial.setUseType(itemNode.valueOf("@UseType"));
                itemSpecial.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
                itemSpecial.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
                itemSpecial.setName(itemNode.valueOf("@Name_en"));

                itemSpecialRepository.save(itemSpecial);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemTool() {

        try {

            InputStream itemToolFile = ResourceUtil.getResource("res/Item_Tools.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemToolFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemTool itemTool = itemToolRepository.findById(i).orElse(new ItemTool());

                itemTool.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemTool.setUseType(itemNode.valueOf("@UseType"));
                itemTool.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
                itemTool.setKind(itemNode.valueOf("@Kind"));
                itemTool.setToolGrade(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@ToolGrade"))));
                itemTool.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
                itemTool.setName(itemNode.valueOf("@Name_en"));

                itemToolRepository.save(itemTool);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemHouseDeco() {

        try {

            InputStream itemHouseDecoFile = ResourceUtil.getResource("res/Item_HouseDeco.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemHouseDecoFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemHouseDeco itemHouseDeco = itemHouseDecoRepository.findById(i).orElse(new ItemHouseDeco());

                itemHouseDeco.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemHouseDeco.setUseType(itemNode.valueOf("@UseType"));
                itemHouseDeco.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
                itemHouseDeco.setKind(itemNode.valueOf("@Kind"));
                itemHouseDeco.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
                itemHouseDeco.setName(itemNode.valueOf("@Name_en"));
                itemHouseDeco.setHousingPoint(Integer.valueOf(itemNode.valueOf("@Housing_point")));
                itemHouseDeco.setAddGold(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddGold"))));
                itemHouseDeco.setAddExp(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddExp"))));
                itemHouseDeco.setAddBattleGold(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddBattleGold"))));
                itemHouseDeco.setAddBattleExp(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@AddBattleExp"))));

                itemHouseDecoRepository.save(itemHouseDeco);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemHouse() {

        try {

            InputStream itemHouseFile = ResourceUtil.getResource("res/Item_House.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemHouseFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemHouse itemHouse = itemHouseRepository.findById(i).orElse(new ItemHouse());

                itemHouse.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemHouse.setName(itemNode.valueOf("@Name_en"));
                itemHouse.setUseType(itemNode.valueOf("@UseType"));
                itemHouse.setLevel(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@HouseLevel"))));
                itemHouse.setHousingPoint(Integer.valueOf(itemNode.valueOf("@Housing_point")));
                itemHouse.setMaxAddPercent(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@MaxAddPersent"))));

                itemHouseRepository.save(itemHouse);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemEnchant() {

        try {

            InputStream itemEnchantFile = ResourceUtil.getResource("res/Item_Enchant.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemEnchantFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemEnchant itemEnchant = itemEnchantRepository.findById(i).orElse(new ItemEnchant());

                itemEnchant.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemEnchant.setUseType(itemNode.valueOf("@UseType"));
                itemEnchant.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
                itemEnchant.setKind(itemNode.valueOf("@Kind"));
                itemEnchant.setElementalKind(itemNode.valueOf("@ElementalKind"));
                itemEnchant.setSellPrice(Integer.valueOf(itemNode.valueOf("@SellPrice")));
                itemEnchant.setName(itemNode.valueOf("@Name_en"));

                itemEnchantRepository.save(itemEnchant);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemRecipe() {

        try {

            InputStream itemRecipeFile = ResourceUtil.getResource("res/Item_Recipe_Ini3.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemRecipeFile);

            List<Node> itemList = document.selectNodes("/RecipeList/Recipe");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemRecipe itemRecipe = itemRecipeRepository.findById(i).orElse(new ItemRecipe());

                itemRecipe.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemRecipe.setUseType(itemNode.valueOf("@UseType"));
                itemRecipe.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
                itemRecipe.setUseCount(Integer.valueOf(itemNode.valueOf("@UseCount")));
                itemRecipe.setKind(itemNode.valueOf("@Kind"));
                itemRecipe.setForPlayer(itemNode.valueOf("@Character"));
                itemRecipe.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
                itemRecipe.setName(itemNode.valueOf("@Name_en"));
                itemRecipe.setRequireGold(Integer.valueOf(itemNode.valueOf("@RequireGold")));

                StringBuilder sb = new StringBuilder();
                for (int j = 0; j <= 5; j++) {
                    sb
                            .append(itemNode.valueOf("@Material" + j))
                            .append("=")
                            .append(itemNode.valueOf("@Count" + j))
                            .append(j != 5 ? ";" : "");
                }
                itemRecipe.setMaterials(sb.toString());

                sb = new StringBuilder();
                for (int j = 0; j <= 4; j++) {
                    sb
                            .append(itemNode.valueOf("@Mutation" + j))
                            .append("=")
                            .append(itemNode.valueOf("@Mutation_Pro" + j))
                            .append(",")
                            .append(itemNode.valueOf("@Mutation_MIN" + j))
                            .append(",")
                            .append(itemNode.valueOf("@Mutation_MAX" + j))
                            .append(j != 4 ? ";" : "");
                }
                itemRecipe.setMutations(sb.toString());

                itemRecipeRepository.save(itemRecipe);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemMaterial() {

        try {

            InputStream itemMaterialFile = ResourceUtil.getResource("res/Item_Material.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemMaterialFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemMaterial itemMaterial = itemMaterialRepository.findById(i).orElse(new ItemMaterial());

                itemMaterial.setItemIndex(Integer.valueOf(itemNode.valueOf("@Index")));
                itemMaterial.setUseType(itemNode.valueOf("@UseType"));
                itemMaterial.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
                itemMaterial.setKind(itemNode.valueOf("@Kind"));
                itemMaterial.setSellPrice(Integer.valueOf(itemNode.valueOf("@SellPrice")));
                itemMaterial.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
                itemMaterial.setName(itemNode.valueOf("@Name_en"));

                itemMaterialRepository.save(itemMaterial);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadItemChar() {

        try {

            InputStream itemCharFile = ResourceUtil.getResource("res/Item_Char.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(itemCharFile);

            List<Node> itemList = document.selectNodes("/ItemList/Item");

            long i = 0;
            for (Node itemNode : itemList) {
                i++;
                ItemChar itemChar = itemCharRepository.findById(i).orElse(new ItemChar());

                itemChar.setPlayerType(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@Char"))));
                itemChar.setStrength(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@STR"))));
                itemChar.setStamina(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@STA"))));
                itemChar.setDexterity(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@DEX"))));
                itemChar.setWillpower(BitKit.fromUnsignedInt(Integer.parseInt(itemNode.valueOf("@WIL"))));

                itemCharRepository.save(itemChar);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean loadProduct() {

        try {

            InputStream shopFile = ResourceUtil.getResource("res/Shop_Ini3.xml");
            SAXReader reader = new SAXReader();
            reader.setEncoding("UTF-8");
            Document document = reader.read(shopFile);

            List<Node> productList = document.selectNodes("/ProductList/Product");

            long i = 0;
            for (Node productNode : productList) {
                i++;
                Product product = productRepository.findById(i).orElse(new Product());

                product.setProductIndex(Integer.valueOf(productNode.valueOf("@Index")));
                product.setDisplay(Integer.valueOf(productNode.valueOf("@DISPLAY")));
                product.setHitDisplay(!productNode.valueOf("@HIT_DISPLAY").equals("0"));
                product.setEnabled(!productNode.valueOf("@Enable").equals("0"));
                product.setUseType(productNode.valueOf("@UseType"));
                product.setUse0(Integer.parseInt(productNode.valueOf("@Use0")));
                product.setUse1(Integer.parseInt(productNode.valueOf("@Use1")));
                product.setUse2(Integer.parseInt(productNode.valueOf("@Use2")));
                product.setPriceType(productNode.valueOf("@PriceType"));
                product.setOldPrice0(Integer.valueOf(productNode.valueOf("@OldPrice0")));
                product.setOldPrice1(Integer.valueOf(productNode.valueOf("@OldPrice1")));
                product.setOldPrice2(Integer.valueOf(productNode.valueOf("@OldPrice2")));
                product.setPrice0(Integer.valueOf(productNode.valueOf("@Price0")));
                product.setPrice1(Integer.valueOf(productNode.valueOf("@Price1")));
                product.setPrice2(Integer.valueOf(productNode.valueOf("@Price2")));
                product.setCouplePrice(Integer.valueOf(productNode.valueOf("@CouplePrice")));
                product.setCategory(productNode.valueOf("@Category"));
                product.setName(productNode.valueOf("@Name"));
                product.setGoldBack(Integer.valueOf(productNode.valueOf("@GoldBack")));
                product.setEnableParcel(!productNode.valueOf("@EnableParcel").equals("0"));
                product.setForPlayer(BitKit.fromUnsignedInt(Integer.parseInt(productNode.valueOf("@Char"))));
                product.setItem0(Integer.valueOf(productNode.valueOf("@Item0")));
                product.setItem1(Integer.valueOf(productNode.valueOf("@Item1")));
                product.setItem2(Integer.valueOf(productNode.valueOf("@Item2")));
                product.setItem3(Integer.valueOf(productNode.valueOf("@Item3")));
                product.setItem4(Integer.valueOf(productNode.valueOf("@Item4")));
                product.setItem5(Integer.valueOf(productNode.valueOf("@Item5")));
                product.setItem6(Integer.valueOf(productNode.valueOf("@Item6")));
                product.setItem7(Integer.valueOf(productNode.valueOf("@Item7")));
                product.setItem8(Integer.valueOf(productNode.valueOf("@Item8")));
                product.setItem9(Integer.valueOf(productNode.valueOf("@Item9")));

                productRepository.save(product);
            }
        }
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
        }

        return true;
    }

    private String valueOf(Node node, String propName, String defaultValue) {
        String value = node.valueOf(propName);
        if (value.isEmpty()) {
            return defaultValue;
        }

        return value;
    }
}
