package com.ft.emulator.server.game.core.packet;

public final class PacketID {
    public final static char S2CLoginWelcomePacket = 0xFF9A;
    public final static char C2SLoginRequest = 0x0FA1;
    public final static char S2CLoginAnswerPacket = 0x0FA2;
    public final static char C2SHeartbeat = 0x0FA3;
    public final static char S2CServerNotice = 0x0FA6;
    public final static char C2SDisconnectRequest = 0x0FA7;
    public final static char S2CDisconnectAnswer = 0xFA8;
    public final static char C2SAuthLoginData = 0xFA9;
    public final static char S2CAuthLoginData = 0xFAA;
    public final static char C2SPlayerNameCheck = 0x1019;
    public final static char S2CPlayerNameCheckAnswer = 0x101A;
    public final static char C2SPlayerCreate = 0x101B;
    public final static char S2CPlayerCreateAnswer = 0x101C;
    public final static char C2SLoginFirstPlayerRequest = 0x101E;
    public final static char S2CLoginFirstPlayerAnswer = 0x101F;
    public final static char C2SLoginAliveClient = 0x100F;
    public final static char S2CPlayerList = 0x1005;
    public final static char S2CGameServerList = 0x1010;

    public final static char C2SGameReceiveData = 0x105E;
    public final static char S2CGameAnswerData = 0x105F;
    public final static char C2SGameLoginData = 0x1069;
    public final static char S2CGameLoginData = 0x106A;

    public final static char C2SRoomCreate = 0x1389;
    public final static char C2SRoomCreateQuick = 0x138f;
    public final static char S2CRoomCreateAnswer = 0x138A;
    public final static char C2SRoomNameChange = 0x1791;
    public final static char C2SRoomGameModeChange = 0x18B2;
    public final static char C2SRoomIsPrivateChange = 0x178E;
    public final static char C2SRoomLevelRangeChange = 0x178F;
    public final static char C2SRoomSkillFreeChange = 0x1795;
    public final static char C2SRoomAllowBattlemonChange = 0x1793;
    public final static char C2SRoomQuickSlotChange = 0x17A2;
    public final static char C2SRoomJoin = 0x138B;
    public final static char S2CRoomJoinAnswer = 0x138C;
    public final static char C2SRoomLeave = 0x1771;
    public final static char S2CRoomLeaveAnswer = 0x1772;

    public final static char S2CRoomListAnswer = 0x138E;
    public final static char S2CRoomPlayerInformation = 0x1394;
    public final static char C2SRoomListReq = 0x13EC;

    public final static char C2SLobbyUserListRequest = 0x1707;
    public final static char S2CLobbyUserListAnswer = 0x1708;

    public final static char C2SLobbyUserInfoRequest = 0x139C;
    public final static char S2CLobbyUserInfoAnswer = 0x139D;
    public final static char C2SLobbyUserInfoClothRequest = 0x1BDF;
    public final static char S2CLobbyUserInfoClothAnswer = 0x1BE0;

    public final static char S2CRoomInformation = 0x177A;

    public final static char C2SRoomReadyChange = 0x1775;
    public final static char C2SRoomTriggerStartGame = 0x177B;

    // Not really sure what this does but it let the annoying "Starting game..." window disappear for room master
    public final static char S2CRoomStartGameAck = 0x17E6;

    public final static char S2CRoomStartGameCancelled = 0x17F3;
    public final static char S2CRoomStartGame = 0x17DE;
    public final static char C2SGameAnimationSkipReady = 0x17DD;
    public final static char S2CGameAnimationAllowSkip = 0x17E0;
    public final static char C2SGameAnimationSkipTriggered = 0x17E1;
    public final static char S2CGameAnimationSkip = 0x17E2;
    public final static char S2CGameEndLevelUpPlayerStats = 0x17E3;
    public final static char S2CGameDisplayPlayerStats = 0x17E4;
    public final static char S2CGameRemoveBlackBars = 0x183C;
    public final static char S2CGameNetworkSettings = 0x3EA;
    public final static char S2CGameSetNameColors = 0x183A;
    public final static char C2SRelayPacketToAllClients = 0x414;
    public final static char C2SMatchplayRegisterPlayerForGameSession = 0x3ED;
    public final static char S2CMatchplayAckPlayerInformation = 0x3EF;
    public final static char S2CMatchplayStartServe = 0x183E;
    public final static char S2CMatchplayStartGuardianServe = 0x184C;
    public final static char C2SMatchplayPoint = 0x183F;
    public final static char S2CMatchplayTeamWinsPoint = 0x1840;
    public final static char S2CMatchplayTeamWinsSet = 0x1842 ;
    public final static char S2CMatchplayEndBasicGame = 0x26FC; // Not really sure if name really corresponds to packet.
    public final static char S2CMatchPlaySetExperienceGainInfoData = 0x1846;
    public final static char S2CMatchplaySetGameResultData = 0x1848;
    public final static char S2CMatchplayBackToRoom = 0x1780;
    public final static char C2SMatchplayClientBackInRoom = 0x1773;
    public final static char S2CMatchplayDamageToPlayer = 0x184E;
    public final static char S2CMatchplaySpawnBossBattle = 0x1D55;
    public final static char S2CMatchplayGivePlayerSkills = 0xC98;
    public final static char S2CMatchplayPlaceSkillCrystal = 0x332C;
    public final static char C2SGameServerConnectionProblem = 0x3F1;
    public final static char C2CBallAnimationPacket = 0x10E3;
    public final static char C2CPlayerAnimationPacket = 0x32C9;

    public final static char S2CSetHost = 0x177E;
    public final static char S2CSetHostUnknown = 0x17DA;
    public final static char S2CUnsetHost = 0x17D6;

    public final static char C2SRoomPositionChange = 0x1785;
    public final static char C2SRoomKickPlayer = 0x178B;
    public final static char S2CRoomPositionChangeAnswer = 0x1786;
    public final static char C2SRoomMapChange = 0x1788;
    public final static char S2CRoomMapChangeAnswer = 0x1789;
    public final static char C2SRoomSlotCloseReq = 0x1D4C;
    public final static char S2CRoomSlotCloseAnswer = 0x1D4E;
    public final static char C2SRoomFittingReq = 0x1D60;

    public final static char S2CUnknownRoomJoin = 0x189D;

    public final static char C2SInventorySellReq = 0x1D06;
    public final static char S2CInventorySellAnswer = 0x1D07;
    public final static char C2SInventorySellItemCheckReq = 0x1D08;
    public final static char S2CInventorySellItemCheckAnswer = 0x1D09;
    public final static char S2CInventorySellItemAnswer = 0x1D0A;

    public final static char S2CPlayerLevelExpData = 0x22B8;
    public final static char S2CPlayerInfoPlayStatsData = 0x1B6F;

    public final static char C2SInventoryWearClothRequest = 0x1B63;
    public final static char S2CInventoryWearClothAnswer = 0x1B64;
    public final static char C2SInventoryWearQuickRequest = 0x1BD8;
    public final static char S2CInventoryWearQuickAnswer = 0x1BD9;
    public final static char S2CInventoryData = 0x1B69;
    public final static char C2SInventoryItemTimeExpiredRequest = 0x1BBC;
    public final static char S2CInventoryItemRemoveAnswer = 0x1B74;
    public final static char C2SQuickSlotUseRequest = 0x1BDA;

    public final static char C2SHomeItemsClearReq = 0x2552;
    public final static char C2SHomeItemsPlaceReq = 0x2550;
    public final static char C2SHomeItemsLoadReq = 0x254E;
    public final static char S2CHomeItemsLoadAnswer = 0x254F;
    public final static char S2CHomeData = 0x1519;

    public final static char C2SShopMoneyReq = 0x1B60;
    public final static char S2CShopMoneyAnswer = 0x1B61;
    public final static char C2SShopBuyReq = 0x1B67;
    public final static char S2CShopBuyAnswer = 0x1B68;
    public final static char C2SShopRequestDataPrepare = 0x2389;
    public final static char S2CShopAnswerDataPrepare = 0x238A;
    public final static char C2SShopRequestData = 0x2387;
    public final static char S2CShopAnswerData = 0x2388;

    public final static char C2SOpenGachaReq = 0x1F86;
    public final static char S2COpenGachaAnswer = 0x1F87;
    public final static char S2COpenGachaUnk = 0x1F88;

    public final static char C2SChatLobbyReq = 0x1705;
    public final static char S2CChatLobbyAnswer = 0x1706;
    public final static char C2SChatRoomReq = 0x1777;
    public final static char S2CChatRoomAnswer = 0x1778;
    public final static char C2SWhisperReq = 0x1702;
    public final static char S2CWhisperAnswer = 0x1703;

    public final static char C2SPlayerDelete = 0x1B6B;
    public final static char S2CPlayerDelete = 0x1B6C;

    public final static char C2SPlayerStatusPointChange = 0x1B6D;
    public final static char S2CPlayerStatusPointChange = 0x1B6E;

    public final static char C2SChallengeProgressReq = 0x2206;
    public final static char S2CChallengeProgressAck = 0x2207;
    public final static char C2SChallengeBeginReq = 0x2208;
    public final static char C2SChallengeHp = 0x2209;
    public final static char C2SChallengePoint = 0x220A;
    public final static char C2SChallengeSet = 0x220B;
    public final static char S2CChallengeEnd = 0x220C;
    public final static char C2STutorialBegin = 0x220D;
    public final static char C2STutorialEnd = 0x220E;
    public final static char C2STutorialProgressReq = 0x220F;
    public final static char S2CTutorialProgressAck = 0x2210;
    public final static char C2SChallengeDamage = 0x2211;
    public final static char S2CTutorialEnd = 0x2212;

    public final static char C2SEmblemListRequest = 0x226A;
    public final static char S2CEmblemListAnswer = 0x226B;

    public final static char C2SLobbyJoin = 0x237A;
    public final static char C2SLobbyLeave = 0x2379;

    public final static char D2SDevPacket = 0x555;
}
