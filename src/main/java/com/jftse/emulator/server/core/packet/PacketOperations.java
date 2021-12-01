package com.jftse.emulator.server.core.packet;

import com.jftse.emulator.server.core.handler.*;
import com.jftse.emulator.server.core.handler.anticheat.*;
import com.jftse.emulator.server.core.handler.authentication.*;
import com.jftse.emulator.server.core.handler.game.*;
import com.jftse.emulator.server.core.handler.game.challenge.*;
import com.jftse.emulator.server.core.handler.game.chat.*;
import com.jftse.emulator.server.core.handler.game.emblem.*;
import com.jftse.emulator.server.core.handler.game.gacha.*;
import com.jftse.emulator.server.core.handler.game.guild.*;
import com.jftse.emulator.server.core.handler.game.home.*;
import com.jftse.emulator.server.core.handler.game.inventory.*;
import com.jftse.emulator.server.core.handler.game.lobby.*;
import com.jftse.emulator.server.core.handler.game.lobby.room.*;
import com.jftse.emulator.server.core.handler.game.matchplay.*;
import com.jftse.emulator.server.core.handler.game.messenger.*;
import com.jftse.emulator.server.core.handler.game.player.*;
import com.jftse.emulator.server.core.handler.game.ranking.*;
import com.jftse.emulator.server.core.handler.game.shop.*;
import com.jftse.emulator.server.core.handler.game.tutorial.*;
import com.jftse.emulator.server.core.handler.relay.*;

public enum PacketOperations {
    S2CLoginWelcomePacket(0xFF9A),
    C2SLoginRequest(0x0FA1),
    S2CLoginAnswerPacket(0x0FA2),
    C2SHeartbeat(0x0FA3),
    C2SServerNotice(0x0FA5),
    S2CServerNotice(0x0FA6),
    C2SDisconnectRequest(0x0FA7),
    S2CDisconnectAnswer(0xFA8),
    C2SAuthLoginData(0xFA9),
    S2CAuthLoginData(0xFAA),
    C2SServerTimeRequest(0xFAB),
    S2CServerTimeAnswer(0xFAC),
    C2SPlayerNameCheck(0x1019),
    S2CPlayerNameCheckAnswer(0x101A),
    C2SPlayerCreate(0x101B),
    S2CPlayerCreateAnswer(0x101C),
    C2SLoginFirstPlayerRequest(0x101E),
    S2CLoginFirstPlayerAnswer(0x101F),
    C2SLoginAliveClient(0x100F),
    S2CPlayerList(0x1005),
    S2CGameServerList(0x1010),

    C2SGameReceiveData(0x105E),
    S2CGameAnswerData(0x105F),
    C2SGameLoginData(0x1069),
    S2CGameLoginData(0x106A),

    C2SRoomCreate(0x1389),
    C2SRoomCreateQuick(0x138f),
    S2CRoomCreateAnswer(0x138A),
    C2SRoomNameChange(0x1791),
    C2SRoomGameModeChange(0x18B2),
    C2SRoomIsPrivateChange(0x178E),
    C2SRoomLevelRangeChange(0x178F),
    C2SRoomSkillFreeChange(0x1795),
    C2SRoomAllowBattlemonChange(0x1793),
    C2SRoomQuickSlotChange(0x17A2),
    C2SRoomJoin(0x138B),
    S2CRoomJoinAnswer(0x138C),
    C2SRoomLeave(0x1771),
    S2CRoomLeaveAnswer(0x1772),

    S2CRoomListAnswer(0x138E),
    S2CRoomPlayerInformation(0x1394),
    C2SRoomListReq(0x13EC),
    S2CRoomSetGuardians(0x1D4F),
    S2CRoomSetGuardianStats(0x1D50),
    S2CRoomSetBossGuardiansStats(0x1D58),

    C2SLobbyUserListRequest(0x1707),
    S2CLobbyUserListAnswer(0x1708),

    C2SLobbyUserInfoRequest(0x139C),
    S2CLobbyUserInfoAnswer(0x139D),
    C2SLobbyUserInfoClothRequest(0x1BDF),
    S2CLobbyUserInfoClothAnswer(0x1BE0),

    S2CRoomInformation(0x177A),

    C2SRoomReadyChange(0x1775),
    C2SRoomTriggerStartGame(0x177B),

    // Not really sure what this does but it let the annoying "Starting game..." window disappear for room master
    S2CRoomStartGameAck(0x17E6),

    S2CRoomStartGameCancelled(0x17F3),
    S2CRoomStartGame(0x17DE),
    C2SGameAnimationSkipReady(0x17DD),
    S2CGameAnimationAllowSkip(0x17E0),
    C2SGameAnimationSkipTriggered(0x17E1),
    S2CGameAnimationSkip(0x17E2),
    S2CGameEndLevelUpPlayerStats(0x17E3),
    S2CGameDisplayPlayerStats(0x17E4),
    S2CGameRemoveBlackBars(0x183C),
    S2CGameNetworkSettings(0x3EA),
    S2CGameSetNameColorAndRemoveBlackBar(0x183A),
    S2CMatchplaySetPlayerPosition(0x184A),
    C2SRelayPacketToAllClients(0x414),
    C2SMatchplayRegisterPlayerForGameSession(0x3ED),
    S2CMatchplayAckPlayerInformation(0x3EF),
    S2CMatchplayStartServe(0x183E),
    S2CMatchplayStartGuardianServe(0x184C),
    C2SMatchplayPoint(0x183F),
    S2CMatchplayTeamWinsPoint(0x1840),
    S2CMatchplayTeamWinsSet(0x1842 ),
    S2CMatchplayEndBasicGame(0x26FC), // Not really sure if name really corresponds to packet.
    S2CMatchplayDisplayItemRewards(0x1DB6),
    S2CMatchPlaySetExperienceGainInfoData(0x1846),
    S2CMatchplaySetGameResultData(0x1848),
    S2CMatchplayBackToRoom(0x1780),
    C2SMatchplayClientBackInRoom(0x1773),
    S2CMatchplayClientBackInRoomAck(0x1774),
    S2CMatchplayDamageToPlayer(0x184E),
    S2CMatchplaySpawnBossBattle(0x1D55),
    S2CMatchplayGivePlayerSkills(0xC98),
    S2CMatchplayLetCrystalDisappear(0x332D),
    C2SMatchplayPlayerPicksUpCrystal(0x18E7),
    S2CMatchplayPlaceSkillCrystal(0x332C),
    C2SMatchplaySwapQuickSlotItems(0xc97),
    C2SMatchplayClientSkillHitsTarget(0x2619),
    C2SMatchplaySkillHitsTarget(0x22F1),
    C2SMatchplayPlayerUseSkill(0x18E9),
    S2CMatchplayUseSkill(0x18EA),
    S2CMatchplayIncreaseBreathTimerBy60Seconds(0xC96),
    S2CMatchplayGiveRandomSkill(0x332E),
    S2CMatchplayGiveSpecificSkill(0x18E8),
    C2SGameServerConnectionProblem(0x3F1),
    C2CBallAnimationPacket(0x10E3),
    C2CPlayerAnimationPacket(0x32C9),

    S2CSetHost(0x177E),
    S2CSetHostUnknown(0x17DA),
    S2CUnsetHost(0x17D6),

    C2SRoomPositionChange(0x1785),
    C2SRoomKickPlayer(0x178B),
    S2CRoomPositionChangeAnswer(0x1786),
    C2SRoomMapChange(0x1788),
    S2CRoomMapChangeAnswer(0x1789),
    C2SRoomSlotCloseReq(0x1D4C),
    S2CRoomSlotCloseAnswer(0x1D4E),
    C2SRoomFittingReq(0x1D60),

    S2CUnknownRoomJoin(0x189D),

    C2SInventorySellReq(0x1D06),
    S2CInventorySellAnswer(0x1D07),
    C2SInventorySellItemCheckReq(0x1D08),
    S2CInventorySellItemCheckAnswer(0x1D09),
    S2CInventorySellItemAnswer(0x1D0A),

    S2CPlayerLevelExpData(0x22B8),
    S2CPlayerInfoPlayStatsData(0x1B6F),

    C2SUnknownInventoryOpenRequest(0x237C),
    C2SInventoryWearClothRequest(0x1B63),
    S2CInventoryWearClothAnswer(0x1B64),
    C2SInventoryWearQuickRequest(0x1BD8),
    S2CInventoryWearQuickAnswer(0x1BD9),
    C2SInventoryWearToolRequest(0x1D04),
    S2CInventoryWearToolAnswer(0x1D05),
    C2SInventoryWearSpecialRequest(0x1B70),
    S2CInventoryWearSpecialAnswer(0x1B71),
    C2SInventoryWearCardRequest(0x1C21),
    S2CInventoryWearCardAnswer(0x1C22),
    S2CInventoryData(0x1B69),
    C2SInventoryItemTimeExpiredRequest(0x1BBC),
    S2CInventoryItemRemoveAnswer(0x1B74),
    C2SQuickSlotUseRequest(0x1BDA),

    C2SHomeItemsClearReq(0x2552),
    C2SHomeItemsPlaceReq(0x2550),
    C2SHomeItemsRemoveReq(0x2551),
    S2CHomeItemsRemoveAnswer(0x2552),
    C2SHomeItemsLoadReq(0x254E),
    S2CHomeItemsLoadAnswer(0x254F),
    S2CHomeData(0x1519),

    C2SShopMoneyReq(0x1B60),
    S2CShopMoneyAnswer(0x1B61),
    C2SShopBuyReq(0x1B67),
    S2CShopBuyAnswer(0x1B68),
    C2SShopRequestDataPrepare(0x2389),
    S2CShopAnswerDataPrepare(0x238A),
    C2SShopRequestData(0x2387),
    S2CShopAnswerData(0x2388),

    C2SOpenGachaReq(0x1F86),
    S2COpenGachaAnswer(0x1F87),
    S2COpenGachaUnk(0x1F88),

    C2SChatLobbyReq(0x1705),
    S2CChatLobbyAnswer(0x1706),
    C2SChatRoomReq(0x1777),
    S2CChatRoomAnswer(0x1778),
    C2SWhisperReq(0x1702),
    S2CWhisperAnswer(0x1703),

    C2SAddFriendRequest(0x1F41),
    S2CAddFriendAnswer(0x1F42),
    S2CFriendRequestNotification(0x1F44),
    C2SAddFriendApprovalRequest(0x1F45),
    S2CFriendsListAnswer(0x1F4A),
    C2SDeleteFriendRequest(0x1F55),
    S2CDeleteFriendAnswer(0x1F57),
    C2SSendMessageRequest(0x1F5F),
    S2CReceivedMessageNotification(0x1F61),
    C2SMessageListRequest(0x1F63),
    S2CMessageListAnswer(0x1F64),
    C2SDeleteMessagesRequest(0x1F62),
    C2SMessageSeenRequest(0x1F67),
    C2SSendGiftRequest(0x1F73),
    S2CSendGiftAnswer(0x1F74),
    S2CReceivedGiftNotification(0x1F75),
    C2SClubMembersListRequest(0x1FBA),
    S2CClubMembersListAnswer(0x1FBB),
    S2CYouGotPresentMessage(0x2135),
    C2SSendParcelRequest(0x2199),
    S2CSendParcelAnswer(0x219A),
    S2CReceivedParcelNotification(0x219B),
    C2SParcelListRequest(0x219C),
    S2CParcelListAnswer(0x219D),
    C2SDenyParcelRequest(0x21A0),
    C2SAcceptParcelRequest(0x21A2),
    S2CAcceptParcelAnswer(0x21A3),
    C2SCancelParcelSendingRequest(0x21A4),
    S2CCancelParcelSendingAnswer(0x21A5),
    S2CRemoveParcelFromListAnswer(0x21A6),
    C2SSendProposalRequest(0x251D),
    S2CProposalDeliveredAnswer(0x251E),
    S2CReceivedProposalNotification(0x251F),
    C2SProposalAnswerRequest(0x2521),
    S2CRelationshipAnswer(0x2523),
    C2SProposalListRequest(0x2526),
    S2CProposalListAnswer(0x2527),
    S2CYouBrokeUpWithYourCoupleAnswer(0x252A),

    C2SPlayerDelete(0x1B6B),
    S2CPlayerDelete(0x1B6C),

    C2SPlayerStatusPointChange(0x1B6D),
    S2CPlayerStatusPointChange(0x1B6E),

    C2SGuildNoticeRequest(0x1FFE),
    S2CGuildNoticeAnswer(0x1FFF),
    C2SGuildNameCheckRequest(0x2009),
    S2CGuildNameCheckAnswer(0x200A),
    C2SGuildCreateRequest(0x200B),
    S2CGuildCreateAnswer(0x200C),
    C2SGuildDataRequest(0x200D),
    S2CGuildDataAnswer(0x200E),
    C2SGuildListRequest(0x200F),
    S2CGuildListAnswer(0x2010),
    C2SGuildJoinRequest(0x2011),
    S2CGuildJoinAnswer(0x2012),
    C2SGuildLeaveRequest(0x2014),
    S2CGuildLeaveAnswer(0x2015),
    C2SGuildChangeInformationRequest(0x2017),
    C2SGuildReserveMemberDataRequest(0x2018),
    S2CGuildReserveMemberDataAnswer(0x2019),
    C2SGuildMemberDataRequest(0x201A),
    S2CGuildMemberDataAnswer(0x201B),
    C2SGuildChangeMasterRequest(0x201F),
    S2CGuildChangeMasterAnswer(0x2020),
    C2SGuildChangeSubMasterRequest(0x2021),
    S2CGuildChangeSubMasterAnswer(0x2022),
    C2SGuildDismissMemberRequest(0x2023),
    S2CGuildDismissMemberAnswer(0x2024),
    S2CGuildDismissInfo(0x2025), // ?
    C2SGuildDeleteRequest(0x2026),
    S2CGuildDeleteAnswer(0x2027),
    C2SGuildGoldWithdrawalRequest(0x2029), // ?
    S2CGuildGoldWithdrawalAnswer(0x202A), // ?
    C2SGuildGoldDataRequest(0x202C),
    S2CGuildGoldDataAnswer(0x202D),
    C2SGuildChangeNoticeRequest(0x202E),
    S2CGuildChangeNoticeAnswer(0x202F),
    C2SGuildChatRequest(0x2030),
    S2CGuildChatAnswer(0x2031),
    C2SGuildChangeLogoRequest(0x2034),
    S2CGuildChangeLogoAnswer(0x2035),
    C2SGuildChangeLogoInfo(0x2036),
    C2SGuildSearchRequest(0x203A),
    S2CGuildSearchAnswer(0x203B),
    C2SGuildChangeReverseMemberRequest(0x203F),
    S2CGuildChangeReverseMemberAnswer(0x2040),
    C2SGuildCastleInfoRequest(0x2044),
    S2CGuildCastleInfoAnswer(0x2045),
    C2SGuildCastleChangeInfoRequest(0x2046),
    S2CGuildCastleChangeInfoAnswer(0x2047),

    C2SChallengeProgressReq(0x2206),
    S2CChallengeProgressAck(0x2207),
    C2SChallengeBeginReq(0x2208),
    C2SChallengeHp(0x2209),
    C2SChallengePoint(0x220A),
    C2SChallengeSet(0x220B),
    S2CChallengeEnd(0x220C),
    C2STutorialBegin(0x220D),
    C2STutorialEnd(0x220E),
    C2STutorialProgressReq(0x220F),
    S2CTutorialProgressAck(0x2210),
    C2SChallengeDamage(0x2211),
    S2CTutorialEnd(0x2212),

    C2SEmblemListRequest(0x226A),
    S2CEmblemListAnswer(0x226B),

    C2SLobbyJoin(0x237A),
    C2SLobbyLeave(0x2379),

    C2SRankingPersonalDataReq(0x206D),
    C2SRankingDataReq(0x206F),
    S2CRankingPersonalDataAnswer(0x206E),
    S2CRankingDataAnswer(0x2070),

    C2SUnknown0xE00E(0xE00E),
    C2SUnknown0x1071(0x1071),

    D2SDevPacket(0x555),

    C2SAntiCheatClientRegister(0x9791),
    C2SAntiCheatClientModuleReq(0x9795);

    private final int value;
    private Class<? extends AbstractHandler> handler;
    private static final PacketOperations[] VALUES = values();

    static {
        C2SLoginRequest.handler = LoginPacketHandler.class;
        C2SDisconnectRequest.handler = DisconnectPacketHandler.class;
        C2SLoginFirstPlayerRequest.handler = FirstPlayerPacketHandler.class;
        C2SPlayerNameCheck.handler = PlayerNameCheckPacketHandler.class;
        C2SPlayerCreate.handler = PlayerCreatePacketHandler.class;
        C2SPlayerDelete.handler = PlayerDeletePacketHandler.class;
        C2SAuthLoginData.handler = AuthLoginDataPacketHandler.class;
        C2SHeartbeat.handler = HeartBeatPacketHandler.class;
        C2SLoginAliveClient.handler = LoginAliveClientHandler.class;
        C2SServerNotice.handler = ServerNoticeHandler.class;
        C2SRelayPacketToAllClients.handler = RelayPacketRequestHandler.class;
        C2SMatchplayRegisterPlayerForGameSession.handler = RegisterPlayerForSessionHandler.class;
        C2SGameLoginData.handler = GameServerLoginPacketHandler.class;
        C2SServerTimeRequest.handler = ServerTimeRequestPacketHandler.class;
        C2SGameReceiveData.handler = GameServerDataRequestPacketHandler.class;
        C2SHomeItemsLoadReq.handler = HomeItemsLoadRequestPacketHandler.class;
        C2SHomeItemsPlaceReq.handler = HomeItemsPlaceRequestPacketHandler.class;
        C2SHomeItemsClearReq.handler = HomeItemClearRequestPacketHandler.class;
        C2SInventorySellReq.handler = InventorySellRequestHandler.class;
        C2SInventorySellItemCheckReq.handler = InventorySellItemCheckRequestHandler.class;
        C2SUnknownInventoryOpenRequest.handler = UnknownInventoryOpenPacketHandler.class;
        C2SInventoryWearClothRequest.handler = InventoryWearClothPacketHandler.class;
        C2SInventoryWearToolRequest.handler = InventoryWearToolPacketHandler.class;
        C2SInventoryWearQuickRequest.handler = InventoryWearQuickPacketHandler.class;
        C2SInventoryWearSpecialRequest.handler = InventoryWearSpecialPacketHandler.class;
        C2SInventoryWearCardRequest.handler = InventoryWearCardPacketHandler.class;
        C2SInventoryItemTimeExpiredRequest.handler = InventoryItemTimeExpiredPacketHandler.class;
        C2SShopMoneyReq.handler = ShopMoneyRequestPacketHandler.class;
        C2SShopBuyReq.handler = ShopBuyRequestPacketHandler.class;
        C2SShopRequestDataPrepare.handler = ShopRequestDataPreparePacketHandler.class;
        C2SShopRequestData.handler = ShopRequestDataPacketHandler.class;
        C2SPlayerStatusPointChange.handler = PlayerStatusPointChangePacketHandler.class;
        C2SChallengeProgressReq.handler = ChallengeProgressRequestPacketHandler.class;
        C2STutorialProgressReq.handler = TutorialProgressRequestPacketHandler.class;
        C2SChallengeBeginReq.handler = ChallengeBeginRequestPacketHandler.class;
        C2SChallengeHp.handler = ChallengeHpPacketHandler.class;
        C2SChallengePoint.handler = ChallengePointPacketHandler.class;
        C2SChallengeDamage.handler = ChallengeDamagePacketHandler.class;
        C2SChallengeSet.handler = ChallengeSetPacketHandler.class;
        C2STutorialBegin.handler = TutorialBeginPacketHandler.class;
        C2STutorialEnd.handler = TutorialEndPacketHandler.class;
        C2SQuickSlotUseRequest.handler = QuickSlotUseRequestHandler.class;
        C2SLobbyUserListRequest.handler = LobbyUserListReqPacketHandler.class;
        C2SLobbyUserInfoRequest.handler = LobbyUserInfoReqPacketHandler.class;
        C2SLobbyUserInfoClothRequest.handler = LobbyUserInfoClothReqPacketHandler.class;
        C2SChatLobbyReq.handler = ChatMessageLobbyPacketHandler.class;
        C2SChatRoomReq.handler = ChatMessageRoomPacketHandler.class;
        C2SWhisperReq.handler = ChatMessageWhisperPacketHandler.class;
        C2SLobbyJoin.handler = LobbyJoinPacketHandler.class;
        C2SLobbyLeave.handler = LobbyLeavePacketHandler.class;
        C2SEmblemListRequest.handler = EmblemListRequestPacketHandler.class;
        C2SOpenGachaReq.handler = OpenGachaRequestPacketHandler.class;
        C2SRoomCreate.handler = RoomCreateRequestPacketHandler.class;
        C2SRoomNameChange.handler = RoomNameChangePacketHandler.class;
        C2SRoomGameModeChange.handler = GameModeChangePacketHandler.class;
        C2SRoomIsPrivateChange.handler = RoomIsPrivateChangePacketHandler.class;
        C2SRoomLevelRangeChange.handler = RoomLevelRangeChangePacketHandler.class;
        C2SRoomSkillFreeChange.handler = RoomSkillFreeChangePacketHandler.class;
        C2SRoomAllowBattlemonChange.handler = RoomAllowBattlemonChangePacketHandler.class;
        C2SRoomQuickSlotChange.handler = RoomQuickSlotChangePacketHandler.class;
        C2SRoomJoin.handler = RoomJoinRequestPacketHandler.class;
        C2SRoomLeave.handler = RoomLeaveRequestPacketHandler.class;
        C2SRoomReadyChange.handler = RoomReadyChangeRequestPacketHandler.class;
        C2SRoomMapChange.handler = RoomMapChangeRequestPacketHandler.class;
        C2SRoomPositionChange.handler = RoomPositionChangeRequestPacketHandler.class;
        C2SRoomKickPlayer.handler = RoomKickPlayerRequestPacketHandler.class;
        C2SRoomSlotCloseReq.handler = RoomSlotCloseRequestPacketHandler.class;
        C2SRoomFittingReq.handler = RoomFittingRequestPacketHandler.class;
        C2SRoomCreateQuick.handler = RoomCreateQuickRequestPacketHandler.class;
        C2SRoomListReq.handler = RoomListRequestPacketHandler.class;
        C2SMatchplayClientBackInRoom.handler = ClientBackInRoomPacketHandler.class;
        C2SRankingPersonalDataReq.handler = RankingPersonalDataReqPacketHandler.class;
        C2SRankingDataReq.handler = RankingDataReqPacketHandler.class;
        C2SGuildNoticeRequest.handler = GuildNoticeRequestPacketHandler.class;
        C2SGuildNameCheckRequest.handler = GuildNameCheckRequestPacketHandler.class;
        C2SGuildCreateRequest.handler = GuildCreateRequestPacketHandler.class;
        C2SGuildDataRequest.handler = GuildDataRequestPacketHandler.class;
        C2SGuildListRequest.handler = GuildListRequestPacketHandler.class;
        C2SGuildJoinRequest.handler = GuildJoinRequestPacketHandler.class;
        C2SGuildLeaveRequest.handler = GuildLeaveRequestPacketHandler.class;
        C2SGuildChangeInformationRequest.handler = GuildChangeInformationRequestPacketHandler.class;
        C2SGuildReserveMemberDataRequest.handler = GuildReverseMemberDataRequestPacketHandler.class;
        C2SGuildMemberDataRequest.handler = GuildMemberDataRequestPacketHandler.class;
        C2SGuildChangeMasterRequest.handler = GuildChangeMasterRequestPacketHandler.class;
        C2SGuildChangeSubMasterRequest.handler = GuildChangeSubMasterRequestPacketHandler.class;
        C2SGuildDismissMemberRequest.handler = GuildDismissMemberRequestPacketHandler.class;
        C2SGuildDeleteRequest.handler = GuildDeleteRequestPacketHandler.class;
        C2SGuildChangeNoticeRequest.handler = GuildChangeNoticeRequestPacketHandler.class;
        C2SGuildChatRequest.handler = GuildChatRequestPacketHandler.class;
        C2SGuildSearchRequest.handler = GuildSearchRequestPacketHandler.class;
        C2SGuildChangeReverseMemberRequest.handler = GuildChangeReverseMemberRequestHandler.class;
        C2SGuildChangeLogoRequest.handler = GuildChangeLogoRequestHandler.class;
        C2SClubMembersListRequest.handler = ClubMembersListRequestHandler.class;
        C2SMessageListRequest.handler = MessageListRequestHandler.class;
        C2SParcelListRequest.handler = ParcelListRequestHandler.class;
        C2SProposalListRequest.handler = ProposalListRequestHandler.class;
        C2SAddFriendRequest.handler = AddFriendRequestPacketHandler.class;
        C2SAddFriendApprovalRequest.handler = AddFriendApprovalRequestHandler.class;
        C2SDeleteFriendRequest.handler = DeleteFriendRequestHandler.class;
        C2SSendMessageRequest.handler = SendMessageRequestHandler.class;
        C2SDeleteMessagesRequest.handler = DeleteMessageRequestHandler.class;
        C2SMessageSeenRequest.handler = MessageSeenRequestHandler.class;
        C2SSendGiftRequest.handler = SendGiftRequestHandler.class;
        C2SSendParcelRequest.handler = SendParcelRequestHandler.class;
        C2SDenyParcelRequest.handler = DenyParcelRequestHandler.class;
        C2SAcceptParcelRequest.handler = AcceptParcelRequestHandler.class;
        C2SCancelParcelSendingRequest.handler = CancelSendingParcelRequestHandler.class;
        C2SProposalAnswerRequest.handler = ProposalAnswerRequestHandler.class;
        C2SSendProposalRequest.handler = SendProposalRequestHandler.class;
        C2SRoomTriggerStartGame.handler = RoomStartGamePacketHandler.class;
        C2SGameAnimationSkipReady.handler = GameAnimationReadyToSkipPacketHandler.class;
        C2SGameAnimationSkipTriggered.handler = GameAnimationSkipTriggeredPacketHandler.class;
        C2SMatchplayPoint.handler = MatchplayPointPacketHandler.class;
        C2SMatchplayPlayerPicksUpCrystal.handler = PlayerPickingUpCrystalHandler.class;
        C2SMatchplayPlayerUseSkill.handler = PlayerUseSkillHandler.class;
        C2SMatchplaySkillHitsTarget.handler = SkillHitsTargetHandler.class;
        C2SMatchplaySwapQuickSlotItems.handler = SwapQuickSlotItemsHandler.class;

        D2SDevPacket.handler = DevPacketHandler.class;
        C2SUnknown0xE00E.handler = UnknownPacketHandler.class;
        C2SUnknown0x1071.handler = Unknown0x1071PacketHandler.class;

        C2SAntiCheatClientRegister.handler = ACClientRegisterHandler.class;
        C2SAntiCheatClientModuleReq.handler = ACClientModuleHandler.class;
    }

    PacketOperations(int value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public Character getValueAsChar() {
        return (char) value;
    }

    public String getName() {
        return toString();
    }

    public Class<? extends AbstractHandler> getHandler() {
        return handler;
    }

    public static Class<? extends AbstractHandler> handlerOf(int packetId) {
        for (PacketOperations pop : VALUES) {
            if (pop.getValue().equals(packetId)) {
                return pop.getHandler();
            }
        }
        return UnknownPacketHandler.class;
    }

    public static String getNameByValue(int value) {
        for (PacketOperations packetOperation : values()) {
            if (packetOperation.getValue().equals(value)) {
                return packetOperation.getName();
            }
        }
        return String.format("0x%x", value);
    }
}
