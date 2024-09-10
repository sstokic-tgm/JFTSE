package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.rpc.server.TransitionServerServiceImpl;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
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
    private ClientWhitelistService clientWhitelistService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ClothEquipmentServiceImpl clothEquipmentService;
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
    private PlayerPocketService playerPocketService;
    @Autowired
    private PlayerStatisticService playerStatisticService;
    @Autowired
    private ProductService productService;
    @Autowired
    private HomeService homeService;
    @Autowired
    private PetService petService;
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
    private ItemRecipeService itemRecipeService;
    @Autowired
    private ItemMaterialService itemMaterialService;
    @Autowired
    private ItemSpecialService itemSpecialService;
    @Autowired
    private SocialService socialService;
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private TutorialService tutorialService;
    @Autowired
    private LotteryService lotteryService;
    @Autowired
    private LevelService levelService;
    @Autowired
    private GuardianService guardianService;
    @Autowired
    private BossGuardianService bossGuardianService;
    @Autowired
    private GuardianSkillsService guardianSkillsService;
    @Autowired
    private SkillDropRateService skillDropRateService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private WillDamageService willDamageService;
    @Autowired
    private ScenarioService scenarioService;
    @Autowired
    private MapService mapService;
    @Autowired
    private EnchantService enchantService;

    @Autowired
    private ProfaneWordsService profaneWordsService;

    @Autowired
    private ModuleService moduleService;
    @Autowired
    private GameLogService gameLogService;
    @Autowired
    private CommandLogService commandLogService;

    @Autowired
    private BlockedIPService blockedIPService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private JdbcUtil jdbcUtil;

    @Autowired
    private GameEventService gameEventService;

    @Autowired
    private TransitionServerServiceImpl transitionServerService;

    @PostConstruct
    public void init() {
        instance = this;

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static ServiceManager getInstance() {
        return instance;
    }
}
