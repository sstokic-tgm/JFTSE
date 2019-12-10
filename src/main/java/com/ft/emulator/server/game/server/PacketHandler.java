package com.ft.emulator.server.game.server;

import com.ft.emulator.common.dao.GenericModelDao;
import com.ft.emulator.common.service.EntityManagerFactoryUtil;
import com.ft.emulator.common.utilities.BitKit;
import com.ft.emulator.common.utilities.StreamUtils;
import com.ft.emulator.common.utilities.StringUtils;
import com.ft.emulator.common.validation.ValidationException;
import com.ft.emulator.server.authserver.AuthenticationImpl;
import com.ft.emulator.server.database.model.account.Account;
import com.ft.emulator.server.database.model.challenge.Challenge;
import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.database.model.character.CharacterPlayer;
import com.ft.emulator.server.database.model.character.StatusPointsAddedDto;
import com.ft.emulator.server.database.model.home.AccountHome;
import com.ft.emulator.server.database.model.home.HomeInventory;
import com.ft.emulator.server.database.model.item.Product;
import com.ft.emulator.server.database.model.pocket.CharacterPlayerPocket;
import com.ft.emulator.server.database.model.pocket.Pocket;
import com.ft.emulator.server.database.model.tutorial.Tutorial;
import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import com.ft.emulator.server.game.characterplayer.StatusPointImpl;
import com.ft.emulator.server.game.home.HomeImpl;
import com.ft.emulator.server.game.inventory.InventoryImpl;
import com.ft.emulator.server.game.item.EItemCategory;
import com.ft.emulator.server.game.item.EItemUseType;
import com.ft.emulator.server.game.matchplay.room.Room;
import com.ft.emulator.server.game.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.money.MoneyImpl;
import com.ft.emulator.server.game.server.packets.*;
import com.ft.emulator.server.game.server.packets.authserver.C2SLoginPacket;
import com.ft.emulator.server.game.server.packets.authserver.S2CLoginAnswerPacket;
import com.ft.emulator.server.game.server.packets.challenge.*;
import com.ft.emulator.server.game.server.packets.character.*;
import com.ft.emulator.server.game.server.packets.chat.*;
import com.ft.emulator.server.game.server.packets.gameserver.C2SGameServerLoginPacket;
import com.ft.emulator.server.game.server.packets.gameserver.C2SGameServerRequestPacket;
import com.ft.emulator.server.game.server.packets.gameserver.S2CGameServerAnswerPacket;
import com.ft.emulator.server.game.server.packets.authserver.S2CGameServerListPacket;
import com.ft.emulator.server.game.server.packets.home.C2SHomeItemsPlaceReqPacket;
import com.ft.emulator.server.game.server.packets.home.S2CHomeDataPacket;
import com.ft.emulator.server.game.server.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.ft.emulator.server.game.server.packets.inventory.*;
import com.ft.emulator.server.game.server.packets.lobby.C2SLobbyUserListRequestPacket;
import com.ft.emulator.server.game.server.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.ft.emulator.server.game.server.packets.room.*;
import com.ft.emulator.server.game.server.packets.shop.*;
import com.ft.emulator.server.game.server.packets.tutorial.C2STutorialBeginRequestPacket;
import com.ft.emulator.server.game.server.packets.tutorial.C2STutorialEndPacket;
import com.ft.emulator.server.game.server.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.ft.emulator.server.game.shop.ShopImpl;
import com.ft.emulator.server.game.singleplay.challenge.ChallengeBasicGame;
import com.ft.emulator.server.game.singleplay.challenge.ChallengeBattleGame;
import com.ft.emulator.server.game.singleplay.challenge.ChallengeManagerImpl;
import com.ft.emulator.server.game.singleplay.challenge.GameMode;
import com.ft.emulator.server.game.singleplay.tutorial.TutorialGame;
import com.ft.emulator.server.game.singleplay.tutorial.TutorialManagerImpl;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class PacketHandler {

    private final static Logger logger = LoggerFactory.getLogger("packethandler");

    private GameHandler gameHandler;

    // DAOs
    private GenericModelDao<Account> accountDao;
    private GenericModelDao<CharacterPlayer> characterPlayerDao;
    private GenericModelDao<Pocket> pocketDao;
    private GenericModelDao<CharacterPlayerPocket> characterPlayerPocketDao;
    private GenericModelDao<AccountHome> accountHomeDao;
    private GenericModelDao<HomeInventory> homeInventoryDao;
    private GenericModelDao<Challenge> challengeDao;
    private GenericModelDao<ChallengeProgress> challengeProgressDao;
    private GenericModelDao<Tutorial> tutorialDao;
    private GenericModelDao<TutorialProgress> tutorialProgressDao;
    private GenericModelDao<Product> productDao;

    public PacketHandler(GameHandler gameHandler) {

        if(gameHandler != null)
            this.gameHandler = gameHandler;

	accountDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Account.class);
	characterPlayerDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), CharacterPlayer.class);
	pocketDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Pocket.class);
	characterPlayerPocketDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), CharacterPlayerPocket.class);
	accountHomeDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), AccountHome.class);
	homeInventoryDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), HomeInventory.class);
	challengeDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Challenge.class);
	challengeProgressDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), ChallengeProgress.class);
	tutorialDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Tutorial.class);
	tutorialProgressDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), TutorialProgress.class);
	productDao = new GenericModelDao<>(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory(), Product.class);
    }

    public void handlePacket(Client client, Packet packet) {

	switch (packet.getPacketId()) {

	case PacketID.C2SLoginRequest:

	    this.handleLoginPacket(client, packet);
	    break;

	case PacketID.C2SDisconnectRequest:

	    this.handleDisconnectPacket(client, packet);
	    break;

	case PacketID.C2SLoginFirstCharacterRequest:

	    this.handleFirstCharacterPacket(client, packet);
	    break;

	case PacketID.C2SCharacterNameCheck:

	    this.handleCharacterNameCheckPacket(client, packet);
	    break;

	case PacketID.C2SCharacterCreate:

	    this.handleCharacterCreatePacket(client, packet);
	    break;

	case PacketID.C2SCharacterDelete:

	    this.handleCharacterDeletePacket(client, packet);
	    break;

	case PacketID.C2SCharacterStatusPointChange:

	    this.handleCharacterStatusPointChangePacket(client, packet);
	    break;

	case PacketID.C2SGameLoginData:

	    this.handleGameServerLoginPacket(client, packet);
	    break;

	case PacketID.C2SGameReceiveData:

	    this.handleGameServerDataRequestPacket(client, packet);
	    break;

	case PacketID.C2SHomeItemsLoadReq:

	    this.handleHomeItemsLoadRequestPacket(client, packet);
	    break;

	case PacketID.C2SHomeItemsPlaceReq:

	    this.handleHomeItemsPlaceRequestPacket(client, packet);
	    break;

	case PacketID.C2SHomeItemsClearReq:

	    this.handleHomeItemClearRequestPacket(client, packet);
	    break;

	case PacketID.C2SInventorySellReq:
	case PacketID.C2SInventorySellItemCheckReq:

	    this.handleInventoryItemSellPackets(client, packet);
	    break;

	case PacketID.C2SInventoryWearClothRequest:

	    this.handleInventoryWearClothPacket(client, packet);
	    break;

	case PacketID.C2SShopMoneyReq:

	    this.handleShopMoneyRequestPacket(client, packet);
	    break;

	case PacketID.C2SShopBuyReq:

	    this.handleShopBuyRequestPacket(client, packet);
	    break;

	case PacketID.C2SShopRequestDataPrepare:
	case PacketID.C2SShopRequestData:

	    this.handleShopRequestDataPackets(client, packet);
	    break;

	case PacketID.C2SChallengeProgressReq:

	    this.handleChallengeProgressRequestPacket(client, packet);
	    break;

	case PacketID.C2STutorialProgressReq:

	    this.handleTutorialProgressRequestPacket(client, packet);
	    break;

	case PacketID.C2SChallengeBeginReq:

	    this.handleChallengeBeginRequestPacket(client, packet);
	    break;

	case PacketID.C2SChallengeHp:

	    this.handleChallengeHpPacket(client, packet);
	    break;

	case PacketID.C2SChallengePoint:

	    this.handleChallengePointPacket(client, packet);
	    break;

	case PacketID.C2SChallengeDamage:

	    this.handleChallengeDamagePacket(client, packet);
	    break;

	case PacketID.C2SChallengeSet:

	    this.handleChallengeSetPacket(client, packet);
	    break;

	case PacketID.C2STutorialBegin:

	    this.handleTutorialBeginPacket(client, packet);
	    break;

	case PacketID.C2STutorialEnd:

	    this.handleTutorialEndPacket(client, packet);
	    break;

	case PacketID.C2SChatLobbyReq:
	case PacketID.C2SChatRoomReq:
	case PacketID.C2SWhisperReq:

	    this.handleChatMessagePackets(client, packet);
	    break;

	case PacketID.C2SRoomCreate:

	    this.handleRoomCreatePacket(client, packet);
	    break;

	case PacketID.C2SRoomPositionChange:

	    this.handleRoomPositionChange(client, packet);
	    break;

	case PacketID.C2SRoomReadyChange:

	    this.handleRoomReadyChange(client, packet);
	    break;

	case PacketID.C2SRoomMapChange:

	    this.handleRoomMapChange(client, packet);
	    break;

	case PacketID.C2SRoomListReq:

	    this.handleRoomListReqPacket(client, packet);
	    break;

	case PacketID.C2SLobbyUserListRequest:

	    this.handleLobbyUserListReqPacket(client, packet);
	    break;

	case PacketID.C2SLobbyJoin:

	    this.handleLobbyJoinLeave(client, true);
	    break;

	case PacketID.C2SLobbyLeave:

	    this.handleLobbyJoinLeave(client, false);
	    break;

	case PacketID.C2SRoomJoin:

	    this.handleRoomJoinPacket(client, packet);
	    break;

	case PacketID.C2SRoomStartGame:

	    this.handleRoomStartGame(client, packet);
	    break;

	case 0x17DD:

	    this.handle17DDPacket(client, packet);
	    break;

	case PacketID.C2SEmblemListRequest:

	    this.handleEmblemListRequestPacket(client, packet);
	    break;

	case PacketID.C2SHeartbeat:
	case PacketID.C2SLoginAliveClient:
	    break;

	default:

	    this.handleUnknown(client, packet);
	    break;
	}
    }

    public void sendWelcomePacket(Client client) {

	S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0,0,0,0);
	client.getPacketStream().write(welcomePacket);
    }

    private void handleLoginPacket(Client client, Packet packet) {

	C2SLoginPacket loginPacket = new C2SLoginPacket(packet);

	AuthenticationImpl authenticationImpl = new AuthenticationImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	Account account = authenticationImpl.login(loginPacket.getUsername(), loginPacket.getPassword());

	if(account == null) {

	    S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.ACCOUNT_INVALID_USER_ID);
	    client.getPacketStream().write(loginAnswerPacket);
	}
	else {

	    Integer accountStatus = account.getStatus();
	    if(!account.getStatus().equals((int)S2CLoginAnswerPacket.SUCCESS)) {

		S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(accountStatus.shortValue());
		client.getPacketStream().write(loginAnswerPacket);
		return;
	    }

	    // set last login date
	    account.setLastLogin(new Date());
	    // mark as logged in
	    account.setStatus((int)S2CLoginAnswerPacket.ACCOUNT_ALREADY_LOGGED_IN);

	    try {
		account = accountDao.save(account);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
		return;
	    }

	    client.setAccount(account);

	    S2CLoginAnswerPacket loginAnswerPacket = new S2CLoginAnswerPacket(S2CLoginAnswerPacket.SUCCESS);
	    client.getPacketStream().write(loginAnswerPacket);

	    S2CCharacterListPacket characterListPacket = new S2CCharacterListPacket(account, account.getCharacterPlayerList());
	    client.getPacketStream().write(characterListPacket);

	    S2CGameServerListPacket gameServerListPacket = new S2CGameServerListPacket(authenticationImpl.getGameServerList());
	    client.getPacketStream().write(gameServerListPacket);
	}
    }

    private void handleDisconnectPacket(Client client, Packet packet) {

        if(this.gameHandler != null && !this.gameHandler.getClients().isEmpty()) {
	    this.gameHandler.removeClient(client);

	    Account account = client.getAccount();

	    // reset status
	    account.setStatus((int) S2CLoginAnswerPacket.SUCCESS);
	    try {

		accountDao.save(account);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
		return;
	    }
	}

	S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
	client.getPacketStream().write(disconnectAnswerPacket);
    }

    private void handleFirstCharacterPacket(Client client, Packet packet) {

        C2SFirstCharacterPacket firstCharacterPacket = new C2SFirstCharacterPacket(packet);

        if(client.getAccount().getCharacterPlayerList().isEmpty()) {

	    CharacterPlayer characterPlayer = new CharacterPlayer();
	    characterPlayer.setAccount(client.getAccount());
	    characterPlayer.setCType(firstCharacterPacket.getCharacterType());
	    characterPlayer.setFirstCharacter(true);

	    try {
		characterPlayer = characterPlayerDao.save(characterPlayer);
	    }
	    catch (ValidationException e) {

	        logger.error(e.getMessage());
	        e.printStackTrace();
	        return;
	    }

	    S2CFirstCharacterAnswerPacket firstCharacterAnswerPacket = new S2CFirstCharacterAnswerPacket((char)0, characterPlayer.getId(), characterPlayer.getCType());
	    client.getPacketStream().write(firstCharacterAnswerPacket);
        }
        else {

	    S2CFirstCharacterAnswerPacket firstCharacterAnswerPacket = new S2CFirstCharacterAnswerPacket((char)-1, (long)0, (byte)0);
	    client.getPacketStream().write(firstCharacterAnswerPacket);
	}
    }

    private void handleCharacterNameCheckPacket(Client client, Packet packet) {

	C2SCharacterNameCheckPacket characterNameCheckPacket = new C2SCharacterNameCheckPacket(packet);

	Map<String, Object> filters = new HashMap<>();
	filters.put("name", characterNameCheckPacket.getNickname());

	CharacterPlayer characterPlayer = characterPlayerDao.find(filters);
	if(characterPlayer == null) {

	    S2CCharacterNameCheckAnswerPacket characterNameCheckAnswerPacket = new S2CCharacterNameCheckAnswerPacket((char)0);
	    client.getPacketStream().write(characterNameCheckAnswerPacket);
	}
	else {

	    S2CCharacterNameCheckAnswerPacket characterNameCheckAnswerPacket = new S2CCharacterNameCheckAnswerPacket((char)-1);
	    client.getPacketStream().write(characterNameCheckAnswerPacket);
	}
    }

    private void handleCharacterCreatePacket(Client client, Packet packet) {

	C2SCharacterCreatePacket characterCreatePacket = new C2SCharacterCreatePacket(packet);

	CharacterPlayer characterPlayer = characterPlayerDao.find(Long.valueOf(characterCreatePacket.getCharacterId()));
	if(characterPlayer == null) {

	    S2CCharacterCreateAnswerPacket characterCreateAnswerPacket = new S2CCharacterCreateAnswerPacket((char)-1);
	    client.getPacketStream().write(characterCreateAnswerPacket);
	    return;
	}

	characterPlayer.setName(characterCreatePacket.getNickname());
	characterPlayer.setAlreadyCreated(true);
	characterPlayer.setStrength(characterCreatePacket.getStrength());

	characterPlayer.setStamina(characterCreatePacket.getStamina());
	characterPlayer.setDexterity(characterCreatePacket.getDexterity());
	characterPlayer.setWillpower(characterCreatePacket.getWillpower());
	characterPlayer.setStatusPoints(characterCreatePacket.getStatusPoints());
	characterPlayer.setLevel(characterCreatePacket.getLevel());

	// create pocket
	Pocket pocket = new Pocket();
	pocket.setBelongings(0);
	try {
	    pocket = pocketDao.save(pocket);
	}
	catch (ValidationException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
	}

	characterPlayer.setPocket(pocket);

	try {
	    characterPlayerDao.save(characterPlayer);
	}
	catch (ValidationException e) {

	    logger.error(e.getMessage());
	    e.printStackTrace();
	}

	// create home
	AccountHome accountHome = new AccountHome();
	accountHome.setAccount(client.getAccount());
	try {
	    accountHomeDao.save(accountHome);
	}
	catch (ValidationException e) {

	    logger.error(e.getMessage());
	    e.printStackTrace();
	}

	S2CCharacterCreateAnswerPacket characterCreateAnswerPacket = new S2CCharacterCreateAnswerPacket((char)0);
	client.getPacketStream().write(characterCreateAnswerPacket);
    }

    private void handleCharacterDeletePacket(Client client, Packet packet) {

	C2SCharacterDeletePacket characterDeletePacket = new C2SCharacterDeletePacket(packet);

	CharacterPlayer characterPlayer = characterPlayerDao.find(Long.valueOf(characterDeletePacket.getCharacterId()));
	if(characterPlayer != null) {

	    characterPlayerDao.remove(characterPlayer.getId());

	    S2CCharacterDeleteAnswerPacket characterDeleteAnswerPacket = new S2CCharacterDeleteAnswerPacket((char)0);
	    client.getPacketStream().write(characterDeleteAnswerPacket);

	    S2CCharacterListPacket characterListPacket = new S2CCharacterListPacket(client.getAccount(), client.getAccount().getCharacterPlayerList());
	    client.getPacketStream().write(characterListPacket);
	}
	else {

	    S2CCharacterDeleteAnswerPacket characterDeleteAnswerPacket = new S2CCharacterDeleteAnswerPacket((char)-1);
	    client.getPacketStream().write(characterDeleteAnswerPacket);
	}
    }

    private void handleCharacterStatusPointChangePacket(Client client, Packet packet) {

	C2SCharacterStatusPointChangePacket characterStatusPointChangeRequestPacket = new C2SCharacterStatusPointChangePacket(packet);

	CharacterPlayer activeCharacterPlayer = client.getActiveCharacterPlayer();

	// we can't change; attributes should be server sided
	if(activeCharacterPlayer.getStatusPoints() == 0) {

	    S2CCharacterStatusPointChangePacket characterStatusPointChangeAnswerPacket = new S2CCharacterStatusPointChangePacket(activeCharacterPlayer, new StatusPointsAddedDto());
	    client.getPacketStream().write(characterStatusPointChangeAnswerPacket);
	}
	else if(activeCharacterPlayer.getStatusPoints() > 0 && characterStatusPointChangeRequestPacket.getStatusPoints() >= 0) {

	    StatusPointImpl statusPointImpl = new StatusPointImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	    if(statusPointImpl.isStatusPointHack(characterStatusPointChangeRequestPacket, activeCharacterPlayer)) {

		S2CCharacterStatusPointChangePacket characterStatusPointChangeAnswerPacket = new S2CCharacterStatusPointChangePacket(activeCharacterPlayer, new StatusPointsAddedDto());
		client.getPacketStream().write(characterStatusPointChangeAnswerPacket);
		return;
	    }

	    // update db
	    activeCharacterPlayer.setStrength(characterStatusPointChangeRequestPacket.getStrength());
	    activeCharacterPlayer.setStamina(characterStatusPointChangeRequestPacket.getStamina());
	    activeCharacterPlayer.setDexterity(characterStatusPointChangeRequestPacket.getDexterity());
	    activeCharacterPlayer.setWillpower(characterStatusPointChangeRequestPacket.getWillpower());
	    activeCharacterPlayer.setStatusPoints(characterStatusPointChangeRequestPacket.getStatusPoints());

	    try {
		activeCharacterPlayer = characterPlayerDao.save(activeCharacterPlayer);
	    }
	    catch (ValidationException e) {

		logger.error(e.getMessage());
		e.printStackTrace();
	    }

	    client.setActiveCharacterPlayer(activeCharacterPlayer);

	    InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	    StatusPointsAddedDto statusPointsAddedDto = inventoryImpl.getStatusPointsFromCloths(activeCharacterPlayer);

	    S2CCharacterStatusPointChangePacket characterStatusPointChangeAnswerPacket = new S2CCharacterStatusPointChangePacket(activeCharacterPlayer, statusPointsAddedDto);
	    client.getPacketStream().write(characterStatusPointChangeAnswerPacket);
	}
    }

    private void handleGameServerDataRequestPacket(Client client, Packet packet) {

	InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	HomeImpl homeImpl = new HomeImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	C2SGameServerRequestPacket gameServerRequestPacket = new C2SGameServerRequestPacket(packet);
	byte requestType = gameServerRequestPacket.getRequestType();

	// pass level & exp
	if(requestType == (byte)0) {

	    S2CCharacterLevelExpPacket characterLevelExpPacket = new S2CCharacterLevelExpPacket(client.getActiveCharacterPlayer().getLevel(), client.getActiveCharacterPlayer().getExpPoints());
	    client.getPacketStream().write(characterLevelExpPacket);
	}
	// pass home/house data
	else if(requestType == (byte)1) {

	    AccountHome accountHome = homeImpl.getAccountHome(client.getAccount());

	    S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
	    client.getPacketStream().write(homeDataPacket);
	}
	// pass inventory & equipped items
	else if(requestType == (byte)2) {

	    List<CharacterPlayerPocket> items = inventoryImpl.getInventoryItems(client.getActiveCharacterPlayer().getPocket());
	    StreamUtils.batches(items, 10)
		    .forEach(itemList -> {
			S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(itemList);
			client.getPacketStream().write(inventoryDataPacket);
		    });

	    StatusPointsAddedDto statusPointsAddedDto = inventoryImpl.getStatusPointsFromCloths(client.getActiveCharacterPlayer());
	    Map<String, Integer> equippedCloths = inventoryImpl.getEquippedCloths(client.getActiveCharacterPlayer());

	    S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char)0, equippedCloths, client.getActiveCharacterPlayer(), statusPointsAddedDto);
	    client.getPacketStream().write(inventoryWearClothAnswerPacket);
	}

	S2CGameServerAnswerPacket gameServerAnswerPacket = new S2CGameServerAnswerPacket(gameServerRequestPacket.getRequestType());
	client.getPacketStream().write(gameServerAnswerPacket);
    }

    private void handleHomeItemsLoadRequestPacket(Client client, Packet packet) {

	HomeImpl homeImpl = new HomeImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

        AccountHome accountHome = homeImpl.getAccountHome(client.getAccount());
        List<HomeInventory> homeInventoryList = homeImpl.getHomeInventoryList(accountHome);

	S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(homeInventoryList);
	client.getPacketStream().write(homeItemsLoadAnswerPacket);
    }

    private void handleHomeItemsPlaceRequestPacket(Client client, Packet packet) {

	C2SHomeItemsPlaceReqPacket homeItemsPlaceReqPacket = new C2SHomeItemsPlaceReqPacket(packet);
	List<Map<String, Object>> homeItemsDataList = homeItemsPlaceReqPacket.getHomeItemsDataList();

	InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	HomeImpl homeImpl = new HomeImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	AccountHome accountHome = homeImpl.getAccountHome(client.getAccount());

	homeItemsDataList
		.forEach(hidl -> {

		    int inventoryItemId = (int)hidl.get("inventoryItemId");
		    CharacterPlayerPocket characterPlayerPocket = inventoryImpl.getItemAsPocket((long)inventoryItemId, client.getActiveCharacterPlayer().getPocket());
		    if(characterPlayerPocket != null) {

		        int itemCount = characterPlayerPocket.getItemCount();

		        // those items are deco items -> its placed on the wall
		        if(itemCount % 3 != 0) {
			    itemCount--;
			}
		        else {
		            itemCount = 0;
			}

		        if(itemCount == 0) {
		            inventoryImpl.removeItemFromInventory((long)inventoryItemId);
			}
		        else {

		            characterPlayerPocket.setItemCount(itemCount);
		            try {
		                characterPlayerPocket = characterPlayerPocketDao.save(characterPlayerPocket);
			    }
		            catch (ValidationException e) {
		                logger.error(e.getMessage());
		                e.printStackTrace();
			    }
			}

			int itemIndex = (int)hidl.get("itemIndex");
			byte unk0 = (byte)hidl.get("unk4");
			byte unk1 = (byte)hidl.get("unk5");
			byte xPos = (byte)hidl.get("xPos");
			byte yPos = (byte)hidl.get("yPos");

			HomeInventory homeInventory = new HomeInventory();
			homeInventory.setId((long)inventoryItemId);
			homeInventory.setAccountHome(accountHome);
			homeInventory.setItemIndex((long)itemIndex);
			homeInventory.setUnk0(unk0);
			homeInventory.setUnk1(unk1);
			homeInventory.setXPos(xPos);
			homeInventory.setYPos(yPos);

			try {
			    homeInventory = homeInventoryDao.save(homeInventory);
			}
			catch (ValidationException e) {
			    logger.error(e.getMessage());
			    e.printStackTrace();
			}

			// TODO update AccountHome stats
		    }
		});
    }

    private void handleHomeItemClearRequestPacket(Client client, Packet packet) {

	HomeImpl homeImpl = new HomeImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	AccountHome accountHome = homeImpl.getAccountHome(client.getAccount());
	List<HomeInventory> homeInventoryList = homeImpl.getHomeInventoryList(accountHome);

	homeInventoryList.forEach(hil -> {

	    CharacterPlayerPocket characterPlayerPocket = inventoryImpl.getItemAsPocketByItemIndex(hil.getItemIndex(), client.getActiveCharacterPlayer().getPocket());
	    // create a new one if null, null indicates that all items are placed
	    if(characterPlayerPocket == null) {

	        characterPlayerPocket = new CharacterPlayerPocket();
	        characterPlayerPocket.setItemIndex(hil.getItemIndex());
	        characterPlayerPocket.setPocket(client.getActiveCharacterPlayer().getPocket());
	        characterPlayerPocket.setItemCount(1);
	        characterPlayerPocket.setCategory(EItemCategory.HOUSE_DECO.getName());
	        characterPlayerPocket.setUseType(StringUtils.firstCharToUpperCase(EItemUseType.COUNT.getName().toLowerCase()));

	        try {
		    inventoryImpl.incrementPocketBelongings(client.getActiveCharacterPlayer().getPocket());
		}
	        catch (ValidationException e) {
	            logger.error(e.getMessage());
	            e.printStackTrace();
		}
	    }
	    // exists, so we increment
	    else {

	        characterPlayerPocket.setItemCount(characterPlayerPocket.getItemCount() + 1);
	    }

	    try {
	        characterPlayerPocketDao.save(characterPlayerPocket);
	    }
	    catch (ValidationException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }

	    homeImpl.removeItemFromHomeInventory(hil.getId());
	});

	S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(new ArrayList<>());
	client.getPacketStream().write(homeItemsLoadAnswerPacket);

	List<CharacterPlayerPocket> items = inventoryImpl.getInventoryItems(client.getActiveCharacterPlayer().getPocket());
	StreamUtils.batches(items, 10)
		.forEach(itemList -> {
		    S2CInventoryDataPacket inventoryDataPacket = new S2CInventoryDataPacket(itemList);
		    client.getPacketStream().write(inventoryDataPacket);
		});
    }

    private void handleGameServerLoginPacket(Client client, Packet packet) {

        C2SGameServerLoginPacket gameServerLoginPacket = new C2SGameServerLoginPacket(packet);

	CharacterPlayer characterPlayer = characterPlayerDao.find(Long.valueOf(gameServerLoginPacket.getCharacterId()), "account", "pocket");
	if(characterPlayer != null && characterPlayer.getAccount() != null) {

	    Packet gameServerLoginAnswerPacket = new Packet(PacketID.S2CGameLoginData);
	    gameServerLoginAnswerPacket.write((char)0, 1);
	    client.getPacketStream().write(gameServerLoginAnswerPacket);

	    client.setAccount(characterPlayer.getAccount());
	    client.setActiveCharacterPlayer(characterPlayer);
	}
	else {

	    Packet gameServerLoginAnswerPacket = new Packet(PacketID.S2CGameLoginData);
	    gameServerLoginAnswerPacket.write((char)-1, 0);
	    client.getPacketStream().write(gameServerLoginAnswerPacket);
	}
    }

    private void handleInventoryItemSellPackets(Client client, Packet packet) {

	InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

        switch (packet.getPacketId()) {

	case PacketID.C2SInventorySellReq:
	{
	    byte status = S2CInventorySellAnswerPacket.SUCCESS;

	    C2SInventorySellReqPacket inventorySellReqPacket = new C2SInventorySellReqPacket(packet);

	    int itemPocketId = inventorySellReqPacket.getItemPocketId();
	    CharacterPlayerPocket characterPlayerPocket = inventoryImpl.getItemAsPocket((long)itemPocketId, client.getActiveCharacterPlayer().getPocket());

	    if(characterPlayerPocket == null) {
	        status = S2CInventorySellAnswerPacket.NO_ITEM;

		S2CInventorySellAnswerPacket inventorySellAnswerPacket = new S2CInventorySellAnswerPacket(status, 0, 0);
		client.getPacketStream().write(inventorySellAnswerPacket);

		break;
	    }

	    Integer sellPrice = inventoryImpl.getItemSellPrice(characterPlayerPocket);

	    S2CInventorySellAnswerPacket inventorySellAnswerPacket = new S2CInventorySellAnswerPacket(status, itemPocketId, sellPrice);
	    client.getPacketStream().write(inventorySellAnswerPacket);

	    break;
	}

	case PacketID.C2SInventorySellItemCheckReq:
	{
	    byte status = S2CInventorySellItemCheckAnswerPacket.SUCCESS;

	    C2SInventorySellItemCheckReqPacket inventorySellItemCheckReqPacket = new C2SInventorySellItemCheckReqPacket(packet);

	    int itemPocketId = inventorySellItemCheckReqPacket.getItemPocketId();
	    Pocket pocket = client.getActiveCharacterPlayer().getPocket();
	    CharacterPlayerPocket characterPlayerPocket = inventoryImpl.getItemAsPocket((long)itemPocketId, pocket);

	    if(characterPlayerPocket == null) {
		status = S2CInventorySellItemCheckAnswerPacket.NO_ITEM;

		S2CInventorySellItemCheckAnswerPacket inventorySellItemCheckAnswerPacket = new S2CInventorySellItemCheckAnswerPacket(status);
		client.getPacketStream().write(inventorySellItemCheckAnswerPacket);

		break;
	    }

	    Integer sellPrice = inventoryImpl.getItemSellPrice(characterPlayerPocket);

	    S2CInventorySellItemCheckAnswerPacket inventorySellItemCheckAnswerPacket = new S2CInventorySellItemCheckAnswerPacket(status);
	    client.getPacketStream().write(inventorySellItemCheckAnswerPacket);

	    S2CInventorySellItemAnserPacket inventorySellAnswerPacket = new S2CInventorySellItemAnserPacket((char)characterPlayerPocket.getItemCount().intValue(), inventorySellItemCheckReqPacket.getItemPocketId());
	    client.getPacketStream().write(inventorySellAnswerPacket);

	    inventoryImpl.removeItemFromInventory(characterPlayerPocket.getId());
	    try {
		pocket = inventoryImpl.decrementPocketBelongings(client.getActiveCharacterPlayer().getPocket());
	    }
	    catch (ValidationException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }
	    client.getActiveCharacterPlayer().setPocket(pocket);

	    MoneyImpl moneyImpl = new MoneyImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	    CharacterPlayer characterPlayer = client.getActiveCharacterPlayer();
	    try {
	        characterPlayer = moneyImpl.updateMoney(characterPlayer, sellPrice);
	    }
	    catch (ValidationException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }

	    // send new money status to client
	    Packet moneyAnswerPacket = new Packet(PacketID.S2CShopMoneyAnswer);

	    characterPlayer = moneyImpl.getCurrentMoneyForPlayer(characterPlayer);

	    moneyAnswerPacket.write(characterPlayer.getAccount().getAp());
	    moneyAnswerPacket.write(characterPlayer.getGold());
	    client.getPacketStream().write(moneyAnswerPacket);

	    client.setActiveCharacterPlayer(characterPlayer);

	    break;
	}
	}
    }

    private void handleInventoryWearClothPacket(Client client, Packet packet) {

        C2SInventoryWearClothReqPacket inventoryWearClothReqPacket = new C2SInventoryWearClothReqPacket(packet);

	CharacterPlayer characterPlayer = client.getActiveCharacterPlayer();

	InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
        inventoryImpl.updateCloths(characterPlayer, inventoryWearClothReqPacket);

	try {
	    characterPlayer = characterPlayerDao.save(characterPlayer);
	}
	catch (ValidationException e) {

	    S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char) 1, new HashMap<>(), characterPlayer, new StatusPointsAddedDto());
	    client.getPacketStream().write(inventoryWearClothAnswerPacket);

	    logger.error(e.getMessage());
	    e.printStackTrace();
	}

	client.setActiveCharacterPlayer(characterPlayer);

	StatusPointsAddedDto statusPointsAddedDto = inventoryImpl.getStatusPointsFromCloths(characterPlayer);

	S2CInventoryWearClothAnswerPacket inventoryWearClothAnswerPacket = new S2CInventoryWearClothAnswerPacket((char)0, inventoryWearClothReqPacket, characterPlayer, statusPointsAddedDto);
	client.getPacketStream().write(inventoryWearClothAnswerPacket);
    }

    private void handleShopMoneyRequestPacket(Client client, Packet packet) {

	Packet unknownAnswer = new Packet(PacketID.S2CShopMoneyAnswer);

	MoneyImpl moneyImpl = new MoneyImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	CharacterPlayer characterPlayer = moneyImpl.getCurrentMoneyForPlayer(client.getActiveCharacterPlayer());

	unknownAnswer.write(characterPlayer.getAccount().getAp());
	unknownAnswer.write(characterPlayer.getGold());
	client.getPacketStream().write(unknownAnswer);

	client.setActiveCharacterPlayer(characterPlayer);
    }

    private void handleShopBuyRequestPacket(Client client, Packet packet) {

        C2SShopBuyPacket shopBuyPacket = new C2SShopBuyPacket(packet);

	byte unk0 = shopBuyPacket.getUnk0();
	int itemId = shopBuyPacket.getItemId();
	byte option = shopBuyPacket.getOption();

	ShopImpl shopImpl = new ShopImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	MoneyImpl moneyImpl = new MoneyImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	InventoryImpl inventoryImpl = new InventoryImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	Product product = shopImpl.getProduct((long)itemId);

	CharacterPlayer characterPlayer = client.getActiveCharacterPlayer();
	int gold = characterPlayer.getGold();
	int result = gold - product.getPrice0();

	if(result >= 0) {

	    CharacterPlayerPocket characterPlayerPocket = new CharacterPlayerPocket();
	    characterPlayerPocket.setCategory(product.getCategory());
	    characterPlayerPocket.setItemIndex(product.getItem0());
	    characterPlayerPocket.setUseType(product.getUseType());

	    if (option == 0) {
		characterPlayerPocket.setItemCount(product.getUse0() == 0 ? 1 : product.getUse0());
	    }
	    else if (option == 1) {
		characterPlayerPocket.setItemCount(product.getUse1());
	    }
	    else if (option == 2) {
		characterPlayerPocket.setItemCount(product.getUse2());
	    }

	    Pocket pocket = client.getActiveCharacterPlayer().getPocket();
	    logger.info(pocket.getId() + "");
	    characterPlayerPocket.setPocket(pocket);

	    try {

		characterPlayerPocket = characterPlayerPocketDao.save(characterPlayerPocket);
		pocket = inventoryImpl.incrementPocketBelongings(pocket);
	    }
	    catch (ValidationException e) {
		logger.error(e.getMessage());
		e.printStackTrace();
	    }
	    client.getActiveCharacterPlayer().setPocket(pocket);

	    S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.SUCCESS, characterPlayerPocket);
	    client.getPacketStream().write(shopBuyPacketAnswer);

	    try {
		characterPlayer = moneyImpl.setMoney(characterPlayer, result);
	    }
	    catch (ValidationException e) {
		logger.error(e.getMessage());
		e.printStackTrace();
	    }

	    // send new money status to client
	    Packet moneyAnswerPacket = new Packet(PacketID.S2CShopMoneyAnswer);

	    characterPlayer = moneyImpl.getCurrentMoneyForPlayer(characterPlayer);

	    moneyAnswerPacket.write(characterPlayer.getAccount().getAp());
	    moneyAnswerPacket.write(characterPlayer.getGold());
	    client.getPacketStream().write(moneyAnswerPacket);

	    client.setActiveCharacterPlayer(characterPlayer);
	}
	else {

	    S2CShopBuyPacket shopBuyPacketAnswer = new S2CShopBuyPacket(S2CShopBuyPacket.NEED_MORE_GOLD, null);
	    client.getPacketStream().write(shopBuyPacketAnswer);
	}
    }

    private void handleShopRequestDataPackets(Client client, Packet packet) {

        ShopImpl shopImpl = new ShopImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());

	switch(packet.getPacketId()) {

	case PacketID.C2SShopRequestDataPrepare: {

	    C2SShopRequestDataPreparePacket shopRequestDataPreparePacket = new C2SShopRequestDataPreparePacket(packet);

	    byte category = shopRequestDataPreparePacket.getCategory();
	    byte part = shopRequestDataPreparePacket.getPart();
	    byte character = shopRequestDataPreparePacket.getCharacter();

	    List<Product> productList = shopImpl.getProductList(category, part, character);

	    S2CShopAnswerDataPreparePacket shopAnswerDataPreparePacket = new S2CShopAnswerDataPreparePacket(shopRequestDataPreparePacket.getCategory(), shopRequestDataPreparePacket.getPart(), shopRequestDataPreparePacket.getCharacter(), productList.size());
	    client.getPacketStream().write(shopAnswerDataPreparePacket);

	    break;
	}

	case PacketID.C2SShopRequestData: {

	    C2SShopRequestDataPacket shopRequestDataPacket = new C2SShopRequestDataPacket(packet);

	    byte category = shopRequestDataPacket.getCategory();
	    byte part = shopRequestDataPacket.getPart();
	    byte character = shopRequestDataPacket.getCharacter();
	    int page = BitKit.toUnsignedInt(shopRequestDataPacket.getPage());

	    List<Product> productList = shopImpl.getProductList(category, part, character);

	    List<Product> productPaginatedList = productList.stream()
		    .skip(page == 1 ? 0 : (page * 6) - 6)
		    .limit(6)
		    .collect(Collectors.toList());

	    S2CShopAnswerDataPacket shopAnswerDataPacket = new S2CShopAnswerDataPacket(productPaginatedList.size(), productPaginatedList);
	    client.getPacketStream().write(shopAnswerDataPacket);

	    break;
	}
	}
    }

    private void handleChallengeProgressRequestPacket(Client client, Packet packet) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("characterPlayer", client.getActiveCharacterPlayer());

        List<ChallengeProgress> challengeProgressList = challengeProgressDao.getList(filters, "characterPlayer", "challenge");

        S2CChallengeProgressAnswerPacket challengeProgressAnswerPacket = new S2CChallengeProgressAnswerPacket(challengeProgressList);
        client.getPacketStream().write(challengeProgressAnswerPacket);
    }

    private void handleTutorialProgressRequestPacket(Client client, Packet packet) {

        Map<String, Object> filters = new HashMap<>();
        filters.put("characterPlayer", client.getActiveCharacterPlayer());

        List<TutorialProgress> tutorialProgressList = tutorialProgressDao.getList(filters, "characterPlayer", "tutorial");

        S2CTutorialProgressAnswerPacket tutorialProgressAnswerPacket = new S2CTutorialProgressAnswerPacket(tutorialProgressList);
        client.getPacketStream().write(tutorialProgressAnswerPacket);
    }

    private void handleChallengeBeginRequestPacket(Client client, Packet packet) {

        C2SChallengeBeginRequestPacket challengeBeginRequestPacket = new C2SChallengeBeginRequestPacket(packet);
	Long challengeId = (long)challengeBeginRequestPacket.getChallengeId();

	Challenge currentChallenge = this.gameHandler.getChallenge(challengeId);

	if(currentChallenge.getGameMode() == GameMode.BASIC) {
	    client.setActiveChallengeGame(new ChallengeBasicGame(challengeId));
	}
	else if(currentChallenge.getGameMode() == GameMode.BATTLE) {
	    client.setActiveChallengeGame(new ChallengeBattleGame(challengeId));
	}

	Packet answer = new Packet(PacketID.C2STutorialBegin);
	answer.write((char)1);
	client.getPacketStream().write(answer);
    }

    private void handleChallengeHpPacket(Client client, Packet packet) {

        C2SChallengeHpPacket challengeHpPacket = new C2SChallengeHpPacket(packet);

        if(client.getActiveChallengeGame() instanceof ChallengeBattleGame) {
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setNpcHp((int)challengeHpPacket.getNpcHp());
            ((ChallengeBattleGame) client.getActiveChallengeGame()).setPlayerHp((int)challengeHpPacket.getPlayerHp());
	}
    }

    private void handleChallengePointPacket(Client client, Packet packet) {

        C2SChallengePointPacket challengePointPacket = new C2SChallengePointPacket(packet);

	((ChallengeBasicGame) client.getActiveChallengeGame()).setPoints(challengePointPacket.getPointsPlayer(), challengePointPacket.getPointsNpc());

	if(client.getActiveChallengeGame().getFinished()) {

	    boolean win = ((ChallengeBasicGame) client.getActiveChallengeGame()).getSetsPlayer() == 2;

	    ChallengeManagerImpl challengeManagerImpl = new ChallengeManagerImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	    challengeManagerImpl.finishChallengeGame(client, win);
	}
    }

    private void handleChallengeDamagePacket(Client client, Packet packet) {

        C2SChallengeDamagePacket challengeDamagePacket = new C2SChallengeDamagePacket(packet);

	((ChallengeBattleGame) client.getActiveChallengeGame()).setHp(challengeDamagePacket.getPlayer(), challengeDamagePacket.getDmg());
	if(client.getActiveChallengeGame().getFinished()) {

	    boolean win = ((ChallengeBattleGame) client.getActiveChallengeGame()).getPlayerHp() > 0;

	    ChallengeManagerImpl challengeManagerImpl = new ChallengeManagerImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	    challengeManagerImpl.finishChallengeGame(client, win);
	}
    }

    private void handleChallengeSetPacket(Client client, Packet packet) {
    }

    private void handleTutorialBeginPacket(Client client, Packet packet) {

        C2STutorialBeginRequestPacket tutorialBeginRequestPacket = new C2STutorialBeginRequestPacket(packet);
	Long tutorialId = (long)tutorialBeginRequestPacket.getTutorialId();

	client.setActiveTutorialGame(new TutorialGame(tutorialId));

	Packet answer = new Packet(PacketID.C2STutorialBegin);
	answer.write((char)1);
	client.getPacketStream().write(answer);
    }

    private void handleTutorialEndPacket(Client client, Packet packet) {

        C2STutorialEndPacket tutorialEndPacket = new C2STutorialEndPacket(packet);
        client.getActiveTutorialGame().finishTutorial();

	TutorialManagerImpl tutorialManagerImpl = new TutorialManagerImpl(EntityManagerFactoryUtil.INSTANCE.getEntityManagerFactory());
	tutorialManagerImpl.finishTutorialGame(client);
    }

    private void handleChatMessagePackets(Client client, Packet packet) {

        switch (packet.getPacketId()) {

	case PacketID.C2SChatLobbyReq:
	{
	    C2SChatLobbyReqPacket chatLobbyReqPacket = new C2SChatLobbyReqPacket(packet);
	    S2CChatLobbyAnswerPacket chatLobbyAnswerPacket = new S2CChatLobbyAnswerPacket(chatLobbyReqPacket.getUnk(), client.getActiveCharacterPlayer().getName(), chatLobbyReqPacket.getMessage());

	    this.gameHandler.getClients().forEach(cl -> cl.getPacketStream().write(chatLobbyAnswerPacket));

	    break;
	}
	case PacketID.C2SChatRoomReq:
	{
	    C2SChatRoomReqPacket chatRoomReqPacket = new C2SChatRoomReqPacket(packet);
	    S2CChatRoomAnswerPacket chatRoomAnswerPacket = new S2CChatRoomAnswerPacket(chatRoomReqPacket.getType(), client.getActiveCharacterPlayer().getName(), chatRoomReqPacket.getMessage());

	    List<Client> clientList = this.gameHandler.getClientsInRoom(client.getActiveRoom().getId());
	    clientList.forEach(cl -> cl.getPacketStream().write(chatRoomAnswerPacket));

	    break;
	}
	case PacketID.C2SWhisperReq:
	{
	    C2SWhisperReqPacket whisperReqPacket = new C2SWhisperReqPacket(packet);
	    S2CWhisperAnswerPacket whisperAnswerPacket = new S2CWhisperAnswerPacket(client.getActiveCharacterPlayer().getName(), whisperReqPacket.getReceiverName(), whisperReqPacket.getMessage());

	    this.gameHandler.getClients().stream()
		    .filter(cl -> cl.getActiveCharacterPlayer().getName().equals(whisperReqPacket.getReceiverName()))
		    .findAny()
		    .ifPresent(cl -> cl.getPacketStream().write(whisperAnswerPacket));

	    client.getPacketStream().write(whisperAnswerPacket);

	    break;
	}
	}
    }

    private void handleRoomCreatePacket(Client client, Packet packet) {

        C2SRoomCreatePacket roomCreatePacket = new C2SRoomCreatePacket(packet);

	Room room = new Room();
	room.setId((char)this.gameHandler.getRooms().size());
	room.setName(roomCreatePacket.getName());
	room.setGameMode((byte)0);
	room.setBattleMode(roomCreatePacket.getGameMode());
	room.setLevel(client.getActiveCharacterPlayer().getLevel());
	room.setLevelRange(roomCreatePacket.getLevelRange());
	room.setMap((byte)1);
	room.setBetting(false);
	room.setBettingCoins((byte)0);
	room.setBettingGold(0);
	room.setMaxPlayers(roomCreatePacket.getPlayers());
	room.setIsPrivate(roomCreatePacket.isPrivate());
	room.setBettingMode((byte)0);
	room.setBall(roomCreatePacket.getBall());

	RoomPlayer roomPlayer = new RoomPlayer();
	roomPlayer.setPlayer(client.getActiveCharacterPlayer());
	roomPlayer.setPosition((char)0);
	roomPlayer.setMaster(true);
	roomPlayer.setReady(false);
	room.getPlayerList().add(roomPlayer);

	client.setActiveRoom(room);
	this.gameHandler.getRooms().add(room);

	Packet roomCreateAnswerPacket = new Packet(PacketID.S2CRoomCreateAnswer);
	roomCreateAnswerPacket.write(0);
	client.getPacketStream().write(roomCreateAnswerPacket);

	S2CRoomInformation roomInformation = new S2CRoomInformation(room);
	client.getPacketStream().write(roomInformation);

	S2CRoomPlayerInformation roomPlayerInformation = new S2CRoomPlayerInformation(room.getPlayerList());
	client.getPacketStream().write(roomPlayerInformation);

	S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(this.gameHandler.getRooms());
	this.gameHandler.getClientsInLobby().forEach(cil -> cil.getPacketStream().write(roomListAnswerPacket));
    }

    private void handleRoomJoinPacket(Client client, Packet packet) {

        C2SRoomJoinPacket roomJoinPacket = new C2SRoomJoinPacket(packet);

        Room room = this.gameHandler.getRooms().stream()
		.filter(r -> r.getId() == roomJoinPacket.getRoomId())
		.findAny()
		.orElse(null);

        S2CRoomJoinAnswer roomJoinAnswer = new S2CRoomJoinAnswer((short)0, (byte)0, (byte)0, (byte)0);
        client.getPacketStream().write(roomJoinAnswer);

        client.setActiveRoom(room);

        S2CRoomInformation roomInformation = new S2CRoomInformation(room);
        client.getPacketStream().write(roomInformation);

        RoomPlayer roomPlayer = new RoomPlayer();
        roomPlayer.setPlayer(client.getActiveCharacterPlayer());
        roomPlayer.setPosition((char)1);
        roomPlayer.setMaster(false);
        roomPlayer.setReady(false);
        room.getPlayerList().add(roomPlayer);

        List<Client> clientList = this.gameHandler.getClientsInRoom(room.getId());
        clientList.forEach(c -> {
            S2CRoomPlayerInformation roomPlayerInformation = new S2CRoomPlayerInformation(room.getPlayerList());
            c.getPacketStream().write(roomPlayerInformation);
	});
    }

    private void handleRoomReadyChange(Client client, Packet packet) {

        C2SRoomReadyChangePacket roomReadyChangePacket = new C2SRoomReadyChangePacket(packet);

        client.getActiveRoom().getPlayerList().stream()
		.filter(pl -> pl.getPlayer().getId().equals(client.getActiveCharacterPlayer().getId()))
		.findAny()
		.orElse(null)
		.setReady(roomReadyChangePacket.getReady() == 1);

        List<Client> clientList = this.gameHandler.getClientsInRoom(client.getActiveRoom().getId());
	clientList.forEach(c -> {
	    S2CRoomPlayerInformation roomPlayerInformation = new S2CRoomPlayerInformation(client.getActiveRoom().getPlayerList());
	    c.getPacketStream().write(roomPlayerInformation);
	});
    }

    private void handleRoomPositionChange(Client client, Packet packet) {

        C2SRoomPositionChange roomPositionChangePacket = new C2SRoomPositionChange(packet);

        char currentPosition = client.getActiveRoom().getPlayerList().stream()
		.filter(pl -> pl.getPlayer().getId().equals(client.getActiveCharacterPlayer().getId()))
		.findAny()
		.get()
		.getPosition();

        S2CRoomPositionChangeAnswer positionChangeAnswer = new S2CRoomPositionChangeAnswer((char)0, currentPosition, roomPositionChangePacket.getPosition());
        client.getActiveRoom().getPlayerList().stream()
		.filter(pl -> pl.getPlayer().getId().equals(client.getActiveCharacterPlayer().getId()))
		.findAny()
		.get()
		.setPosition(roomPositionChangePacket.getPosition());
        client.getPacketStream().write(positionChangeAnswer);

	List<Client> clientList = this.gameHandler.getClientsInRoom(client.getActiveRoom().getId());
	clientList.forEach(c -> {
	    S2CRoomPlayerInformation roomPlayerInformation = new S2CRoomPlayerInformation(client.getActiveRoom().getPlayerList());
	    c.getPacketStream().write(roomPlayerInformation);
	});
    }

    private void handleRoomMapChange(Client client, Packet packet) {

        C2SRoomMapChangePacket roomMapChangePacket = new C2SRoomMapChangePacket(packet);
        S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(roomMapChangePacket.getMap());

        client.getPacketStream().write(roomMapChangeAnswerPacket);
    }

    private void handleRoomStartGame(Client client, Packet packet) {

    }

    private void handle17DDPacket(Client client, Packet packet) {

    }

    private void handleRoomListReqPacket(Client client, Packet packet) {

	S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(this.gameHandler.getRooms());
	client.getPacketStream().write(roomListAnswerPacket);
    }

    private void handleLobbyUserListReqPacket(Client client, Packet packet) {

        C2SLobbyUserListRequestPacket lobbyUserListRequestPacket = new C2SLobbyUserListRequestPacket(packet);

        List<CharacterPlayer> lobbyCharacterList = this.gameHandler.getCharacterPlayersInLobby();

        S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(lobbyCharacterList);
        client.getPacketStream().write(lobbyUserListAnswerPacket);
    }

    private void handleLobbyJoinLeave(Client client, boolean joined) {

	client.setInLobby(joined);
    }

    private void handleEmblemListRequestPacket(Client client, Packet packet) {

    }

    private void handleUnknown(Client client, Packet packet) {

	Packet unknownAnswer = new Packet((char)(packet.getPacketId() + 1));
	if(unknownAnswer.getPacketId() == (char)0x200E) {
	    unknownAnswer.write((char)1); // guild not active
	    client.getPacketStream().write(unknownAnswer);
	}
	else {
	    unknownAnswer.write((short) 0);
	    client.getPacketStream().write(unknownAnswer);
	}

    }
}