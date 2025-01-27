package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.server.core.service.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Getter
@Log4j2
public class ServiceManager {
    private static ServiceManager instance;

    @Autowired
    private AuthTokenService authTokenService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerPocketService playerPocketService;
    @Autowired
    private ClientWhitelistService clientWhitelistService;
    @Autowired
    private ClothEquipmentService clothEquipmentService;
    @Autowired
    private QuickSlotEquipmentService quickSlotEquipmentService;
    @Autowired
    private SpecialSlotEquipmentService specialSlotEquipmentService;
    @Autowired
    private ToolSlotEquipmentService toolSlotEquipmentService;
    @Autowired
    private CardSlotEquipmentService cardSlotEquipmentService;
    @Autowired
    private BattlemonSlotEquipmentService battlemonSlotEquipmentService;
    @Autowired
    private PocketService pocketService;
    @Autowired
    private PlayerStatisticService playerStatisticService;
    @Autowired
    private HomeService homeService;
    @Autowired
    private FriendService friendService;
    @Autowired
    private GiftService giftService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ParcelService parcelService;
    @Autowired
    private ProposalService proposalService;
    @Autowired
    private GuildMemberService guildMemberService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private ItemCharService itemCharService;
    @Autowired
    private ProfaneWordsService profaneWordsService;

    @Autowired
    private BlockedIPService blockedIPService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private CommandLogService commandLogService;

    @PostConstruct
    public void init() {
        instance = this;

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static ServiceManager getInstance() {
        return instance;
    }
}
