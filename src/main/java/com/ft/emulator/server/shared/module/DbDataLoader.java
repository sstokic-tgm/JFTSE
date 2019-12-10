package com.ft.emulator.server.shared.module;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.model.AbstractBaseModel;
import com.ft.emulator.common.resource.ResourceUtil;
import com.ft.emulator.common.service.EntityManagerFactoryUtil;
import com.ft.emulator.common.utilities.BitKit;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.database.model.challenge.Challenge;
import com.ft.emulator.server.database.model.item.*;
import com.ft.emulator.server.database.model.level.LevelExp;
import com.ft.emulator.server.database.model.tutorial.Tutorial;
import com.ft.emulator.server.game.singleplay.challenge.GameMode;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

public class DbDataLoader {

    private final static Logger logger = LoggerFactory.getLogger("main");

    public DbDataLoader() {

        boolean dataLoaded = true;

	boolean levelExpInitialized = getTableCount(LevelExp.class);
	boolean mapQuestInitialized = getTableCount(Challenge.class) && getTableCount(Tutorial.class);
	boolean itemPartsInitialized = getTableCount(ItemPart.class);
	boolean itemSpecialInitialized = getTableCount(ItemSpecial.class);
	boolean itemToolsInitialized = getTableCount(ItemTool.class);
	boolean itemHouseDecoInitialized = getTableCount(ItemHouseDeco.class);
	boolean itemEnchantInitialized = getTableCount(ItemEnchant.class);
	boolean itemRecipeInitialized = getTableCount(ItemRecipe.class);
	boolean itemMaterialInitialized = getTableCount(ItemMaterial.class);
	boolean productInitialized = getTableCount(Product.class);

	if (!levelExpInitialized) {
	    logger.info("Initializing LevelExp...");
	    if (loadLevelExp()) {
		logger.info("LevelExp successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!mapQuestInitialized) {
	    logger.info("Initializing MapQuest...");
	    if (loadMapQuest()) {
		logger.info("MapQuest successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemPartsInitialized) {
	    logger.info("Initializing ItemParts...");
	    if (loadItemParts()) {
		logger.info("ItemPart successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemSpecialInitialized) {
	    logger.info("Initializing ItemSpecial...");
	    if (loadItemSpecial()) {
	        logger.info("ItemSpecial successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemToolsInitialized) {
	    logger.info("Initializing ItemTools...");
	    if (loadItemTools()) {
	        logger.info("ItemTools successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemHouseDecoInitialized) {
	    logger.info("Initializing ItemHouseDeco...");
	    if (loadItemHouseDeco()) {
	        logger.info("ItemHouseDeco successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemEnchantInitialized) {
	    logger.info("Initializing ItemEnchant...");
	    if (loadItemEnchant()) {
	        logger.info("ItemEnchant successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemRecipeInitialized) {
	    logger.info("Initializing ItemRecipe...");
	    if (loadItemRecipe()) {
	        logger.info("ItemRecipe successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!itemMaterialInitialized) {
	    logger.info("Initializing ItemMaterial...");
	    if (loadItemMaterial()) {
	        logger.info("ItemMaterial successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!productInitialized) {
	    logger.info("Initializing Product...");
	    if (loadProduct()) {
	        logger.info("Product successfully initialized!");
	    }
	}
	else {
	    dataLoaded = false;
	}

	if (!dataLoaded) {
	    logger.info("Data is up to date!");
	}
    }

    private boolean getTableCount(Class entity) {

	EntityManager em = EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory().createEntityManager();

	boolean result = em.createQuery("SELECT COUNT(0) FROM " + entity.getSimpleName(), Long.class).getResultList().get(0) != 0;

	em.close();

	return result;
    }

    private boolean loadLevelExp() {

        boolean result = true;

	GenericModelDao<LevelExp> levelExpDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), LevelExp.class);

        try {

	    InputStream levelExpFile = ResourceUtil.getResource("res/LevelExp.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(levelExpFile);

	    List<Node> exps = document.selectNodes("/ExpList/Exp");

	    for(int i = 0; i < exps.size(); i++) {

	        // we wanna use 60 levels as of now
	        if(i == 60) {
	            break;
		}

	        Node exp = exps.get(i);

	        LevelExp levelExp = new LevelExp();
	        levelExp.setLevel((byte)i);
	        levelExp.setExpValue(Integer.valueOf(exp.valueOf("@Value").trim()));

	        try {

	            levelExp = levelExpDao.save(levelExp);
		}
	        catch (ValidationException e) {

	            logger.error(e.getMessage());
	            e.printStackTrace();

	            result = false;
	            break;
		}
	    }
	}
        catch (DocumentException de) {

            de.printStackTrace();

            return false;
	}

        return result;
    }

    private boolean loadMapQuest() {

	boolean result = true;

	GenericModelDao<Tutorial> tutorialDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Tutorial.class);
	GenericModelDao<Challenge> challengeDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Challenge.class);

	try {

	    InputStream mapQuestFile = ResourceUtil.getResource("res/MapQuest.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(mapQuestFile);

	    List<Node> tutorials = document.selectNodes("/Tables/Tutorial");
	    List<Node> challenges = document.selectNodes("/Tables/Challenge");

	    for(Node tutorialNode : tutorials) {

	        Tutorial tutorial = new Tutorial();

		tutorial.setTutorialIndex(Long.valueOf(tutorialNode.valueOf("@Index")));
		tutorial.setItemRewardRepeat(!tutorialNode.valueOf("@ItemRewardRepeat").toLowerCase().equals("no"));
		tutorial.setQuantityMin1(Integer.valueOf(tutorialNode.valueOf("@QuantityMin1")));
		tutorial.setQuantityMin2(Integer.valueOf(tutorialNode.valueOf("@QuantityMin2")));
		tutorial.setQuantityMin3(Integer.valueOf(tutorialNode.valueOf("@QuantityMin3")));
		tutorial.setQuantityMax1(Integer.valueOf(tutorialNode.valueOf("@QuantityMax1")));
		tutorial.setQuantityMax2(Integer.valueOf(tutorialNode.valueOf("@QuantityMax2")));
		tutorial.setQuantityMax3(Integer.valueOf(tutorialNode.valueOf("@QuantityMax3")));
		tutorial.setRewardExp(BitKit.fromUnsignedInt(Integer.valueOf(tutorialNode.valueOf("@RewardEXP"))));
		tutorial.setRewardGold(Integer.valueOf(tutorialNode.valueOf("@RewardGOLD")));
		tutorial.setRewardItem1(Integer.valueOf(tutorialNode.valueOf("@RewardItem1")));
		tutorial.setRewardItem2(Integer.valueOf(tutorialNode.valueOf("@RewardItem2")));
		tutorial.setRewardItem3(Integer.valueOf(tutorialNode.valueOf("@RewardItem3")));

		try {

		    tutorial = tutorialDao.save(tutorial);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }

	    // continue to load data into the db if the import above didn't fail
	    if(result) {

	        for(Node challengeNode : challenges) {

	            Challenge challenge = new Challenge();

		    challenge.setChallengeIndex(Long.valueOf(challengeNode.valueOf("@Index")));
		    challenge.setLevel(Byte.valueOf(challengeNode.valueOf("@Level")));
		    challenge.setLevelRestriction(Byte.valueOf(challengeNode.valueOf("@LevelRestriction")));

		    String gameModeStr = challengeNode.valueOf("@GameMode");
		    Short gameMode = null;
		    if(gameModeStr.equals("BASIC")) {
		        gameMode = GameMode.BASIC;
		    }
		    else if(gameModeStr.equals("BATTLE")) {
		        gameMode = GameMode.BATTLE;
		    }
		    else if(gameModeStr.equals("GUARDIAN")) {
		        gameMode = GameMode.GUARDIAN;
		    }
		    challenge.setGameMode(gameMode);

		    challenge.setItemRewardRepeat(!challengeNode.valueOf("@ItemRewardRepeat").toLowerCase().equals("no"));
		    challenge.setQuantityMin1(Integer.valueOf(challengeNode.valueOf("@QuantityMin1")));
		    challenge.setQuantityMin2(Integer.valueOf(challengeNode.valueOf("@QuantityMin2")));
		    challenge.setQuantityMin3(Integer.valueOf(challengeNode.valueOf("@QuantityMin3")));
		    challenge.setQuantityMax1(Integer.valueOf(challengeNode.valueOf("@QuantityMax1")));
		    challenge.setQuantityMax2(Integer.valueOf(challengeNode.valueOf("@QuantityMax2")));
		    challenge.setQuantityMax3(Integer.valueOf(challengeNode.valueOf("@QuantityMax3")));
		    challenge.setRewardExp(BitKit.fromUnsignedInt(Integer.valueOf(challengeNode.valueOf("@RewardEXP"))));
		    challenge.setRewardGold(Integer.valueOf(challengeNode.valueOf("@RewardGOLD")));
		    challenge.setRewardItem1(Integer.valueOf(challengeNode.valueOf("@RewardItem1")));
		    challenge.setRewardItem2(Integer.valueOf(challengeNode.valueOf("@RewardItem2")));
		    challenge.setRewardItem3(Integer.valueOf(challengeNode.valueOf("@RewardItem3")));

		    challenge.setHp(Integer.valueOf(challengeNode.valueOf("@HP")));
		    challenge.setStr(BitKit.fromUnsignedInt(Integer.valueOf(challengeNode.valueOf("@STR"))));
		    challenge.setSta(BitKit.fromUnsignedInt(Integer.valueOf(challengeNode.valueOf("@STA"))));
		    challenge.setDex(BitKit.fromUnsignedInt(Integer.valueOf(challengeNode.valueOf("@DEX"))));
		    challenge.setWil(BitKit.fromUnsignedInt(Integer.valueOf(challengeNode.valueOf("@WIL"))));

		    try {

			challenge = challengeDao.save(challenge);
		    }
		    catch (ValidationException e) {

			logger.error(e.getMessage());
			e.printStackTrace();

			result = false;
			break;
		    }
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemParts() {

	boolean result = true;

	GenericModelDao<ItemPart> itemPartDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemPart.class);

	try {

	    InputStream itemPartsFile = ResourceUtil.getResource("res/Item_Parts_Ini3.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemPartsFile);

	    List<Node> items = document.selectNodes("/ItemList/Item");

	    for(Node itemNode : items) {

		ItemPart itemPart = new ItemPart();

		itemPart.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemPart.setForCharacter(itemNode.valueOf("@Char"));
		itemPart.setPart(itemNode.valueOf("@Part"));
		itemPart.setEnchantElement(!itemNode.valueOf("@EnchantElement").equals("0"));
		itemPart.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
		itemPart.setName(itemNode.valueOf("@Name_N"));

		itemPart.setLevel(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@Level"))));
		itemPart.setStrength(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@STR"))));
		itemPart.setStamina(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@STA"))));
		itemPart.setDexterity(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@DEX"))));
		itemPart.setWillpower(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@WIL"))));
		itemPart.setAddHp(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@AddHP"))));
		itemPart.setAddQuick(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@AddQuick"))));
		itemPart.setAddBuff(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@AddBuff"))));
		itemPart.setSmashSpeed(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@SmashSpeed"))));
		itemPart.setMoveSpeed(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@MoveSpeed"))));
		itemPart.setChargeshotSpeed(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@ChargeshotSpeed"))));
		itemPart.setLobSpeed(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@LobSpeed"))));
		itemPart.setServeSpeed(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@ServeSpeed"))));
		itemPart.setMaxStrength(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@MAX_STR"))));
		itemPart.setMaxStamina(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@MAX_STA"))));
		itemPart.setMaxDexterity(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@MAX_DEX"))));
		itemPart.setMaxWillpower(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@MAX_WIL"))));
		itemPart.setBallSpin(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@BallSpin"))));
		itemPart.setAtss(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@ATSS"))));
		itemPart.setDfss(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@DFSS"))));
		itemPart.setSocket(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@Socket"))));
		itemPart.setGauge(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@Gauge"))));
		itemPart.setGaugeBattle(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@GaugeBattle"))));

		try {

		    itemPartDao.save(itemPart);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemSpecial() {

	boolean result = true;

	GenericModelDao<ItemSpecial> itemSpecialDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemSpecial.class);

	try {

	    InputStream itemSpecialFile = ResourceUtil.getResource("res/Item_Special.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemSpecialFile);

	    List<Node> items = document.selectNodes("/ItemList/Item");

	    for(Node itemNode : items) {

		ItemSpecial itemSpecial = new ItemSpecial();

		itemSpecial.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemSpecial.setUseType(itemNode.valueOf("@UseType"));
		itemSpecial.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
		itemSpecial.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
		itemSpecial.setName(itemNode.valueOf("@Name_en"));

		try {

		    itemSpecialDao.save(itemSpecial);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemTools() {

	boolean result = true;

	GenericModelDao<ItemTool> itemToolDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemTool.class);

	try {

	    InputStream itemToolsFile = ResourceUtil.getResource("res/Item_Tools.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemToolsFile);

	    List<Node> items = document.selectNodes("/ItemList/Item");

	    for(Node itemNode : items) {

		ItemTool itemTool = new ItemTool();

		itemTool.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemTool.setUseType(itemNode.valueOf("@UseType"));
		itemTool.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
		itemTool.setKind(itemNode.valueOf("@Kind"));
		itemTool.setToolGrade(BitKit.fromUnsignedInt(Integer.valueOf(itemNode.valueOf("@ToolGrade"))));
		itemTool.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
		itemTool.setName(itemNode.valueOf("@Name_en"));

		try {

		    itemToolDao.save(itemTool);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemHouseDeco() {

	boolean result = true;

	GenericModelDao<ItemHouseDeco> itemHouseDecoDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemHouseDeco.class);

	try {

	    InputStream itemHouseDecoFile = ResourceUtil.getResource("res/Item_HouseDeco.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemHouseDecoFile);

	    List<Node> items = document.selectNodes("/ItemList/Item");

	    for(Node itemNode : items) {

		ItemHouseDeco itemHouseDeco = new ItemHouseDeco();

		itemHouseDeco.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemHouseDeco.setUseType(itemNode.valueOf("@UseType"));
		itemHouseDeco.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
		itemHouseDeco.setKind(itemNode.valueOf("@Kind"));
		itemHouseDeco.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
		itemHouseDeco.setName(itemNode.valueOf("@Name_en"));

		try {

		    itemHouseDecoDao.save(itemHouseDeco);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemEnchant() {

	boolean result = true;

	GenericModelDao<ItemEnchant> itemEnchantDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemEnchant.class);

	try {

	    InputStream itemEnchantFile = ResourceUtil.getResource("res/Item_Enchant.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemEnchantFile);

	    List<Node> items = document.selectNodes("/ItemList/Item");

	    for(Node itemNode : items) {

		ItemEnchant itemEnchant = new ItemEnchant();

		itemEnchant.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemEnchant.setUseType(itemNode.valueOf("@UseType"));
		itemEnchant.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
		itemEnchant.setKind(itemNode.valueOf("@Kind"));
		itemEnchant.setElementalKind(itemNode.valueOf("@ElementalKind"));
		itemEnchant.setSellPrice(Integer.valueOf(itemNode.valueOf("@SellPrice")));
		itemEnchant.setName(itemNode.valueOf("@Name_en"));

		try {

		    itemEnchantDao.save(itemEnchant);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemRecipe() {

	boolean result = true;

	GenericModelDao<ItemRecipe> itemRecipeDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemRecipe.class);

	try {

	    InputStream itemRecipeFile = ResourceUtil.getResource("res/Item_Recipe_Ini3.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemRecipeFile);

	    List<Node> items = document.selectNodes("/RecipeList/Recipe");

	    for(Node itemNode : items) {

		ItemRecipe itemRecipe = new ItemRecipe();

		itemRecipe.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemRecipe.setUseType(itemNode.valueOf("@UseType"));
		itemRecipe.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
		itemRecipe.setUseCount(Integer.valueOf(itemNode.valueOf("@UseCount")));
		itemRecipe.setKind(itemNode.valueOf("@Kind"));
		itemRecipe.setForCharacter(itemNode.valueOf("@Character"));
		itemRecipe.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
		itemRecipe.setName(itemNode.valueOf("@Name_en"));

		try {

		    itemRecipeDao.save(itemRecipe);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadItemMaterial() {

	boolean result = true;

	GenericModelDao<ItemMaterial> itemMaterialDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ItemMaterial.class);

	try {

	    InputStream itemMaterialFile = ResourceUtil.getResource("res/Item_Material.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(itemMaterialFile);

	    List<Node> items = document.selectNodes("/ItemList/Item");

	    for(Node itemNode : items) {

		ItemMaterial itemMaterial = new ItemMaterial();

		itemMaterial.setItemIndex(Long.valueOf(itemNode.valueOf("@Index")));
		itemMaterial.setUseType(itemNode.valueOf("@UseType"));
		itemMaterial.setMaxUse(Integer.valueOf(itemNode.valueOf("@MaxUse")));
		itemMaterial.setKind(itemNode.valueOf("@Kind"));
		itemMaterial.setSellPrice(Integer.valueOf(itemNode.valueOf("@SellPrice")));
		itemMaterial.setEnableParcel(!itemNode.valueOf("@EnableParcel").equals("0"));
		itemMaterial.setName(itemNode.valueOf("@Name_en"));

		try {

		    itemMaterialDao.save(itemMaterial);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }

    private boolean loadProduct() {

	boolean result = true;

	GenericModelDao<Product> productDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Product.class);

	try {

	    InputStream shopFile = ResourceUtil.getResource("res/Shop_Ini3.xml");
	    SAXReader reader = new SAXReader();
	    reader.setEncoding("UTF-8");
	    Document document = reader.read(shopFile);

	    List<Node> products = document.selectNodes("/ProductList/Product");

	    for(Node productNode : products) {

	        Product product = new Product();

		product.setProductIndex(Long.valueOf(productNode.valueOf("@Index")));
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
		product.setForCharacter(BitKit.fromUnsignedInt(Integer.valueOf(productNode.valueOf("@Char"))));
		product.setItem0(Long.valueOf(productNode.valueOf("@Item0")));
		product.setItem1(Long.valueOf(productNode.valueOf("@Item1")));
		product.setItem2(Long.valueOf(productNode.valueOf("@Item2")));
		product.setItem3(Long.valueOf(productNode.valueOf("@Item3")));
		product.setItem4(Long.valueOf(productNode.valueOf("@Item4")));
		product.setItem5(Long.valueOf(productNode.valueOf("@Item5")));
		product.setItem6(Long.valueOf(productNode.valueOf("@Item6")));
		product.setItem7(Long.valueOf(productNode.valueOf("@Item7")));
		product.setItem8(Long.valueOf(productNode.valueOf("@Item8")));
		product.setItem9(Long.valueOf(productNode.valueOf("@Item9")));

		try {

		    productDao.save(product);
		}
		catch (ValidationException e) {

		    logger.error(e.getMessage());
		    e.printStackTrace();

		    result = false;
		    break;
		}
	    }
	}
	catch (DocumentException de) {

	    de.printStackTrace();

	    return false;
	}

	return result;
    }
}