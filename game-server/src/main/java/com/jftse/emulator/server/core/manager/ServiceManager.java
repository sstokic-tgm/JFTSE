package com.jftse.emulator.server.core.manager;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.matchplay.extension.GuardianBattleStateProvider;
import com.jftse.emulator.server.core.matchplay.extension.MatchTimerExtension;
import com.jftse.emulator.server.core.matchplay.extension.MatchplayLifecycleExtension;
import com.jftse.emulator.server.core.matchplay.extension.WaveCompletionExtension;
import com.jftse.emulator.server.core.rpc.GrpcAuthService;
import com.jftse.emulator.server.core.service.LotteryServiceV2;
import com.jftse.emulator.server.core.service.impl.ClothEquipmentServiceImpl;
import com.jftse.server.core.jdbc.JdbcUtil;
import com.jftse.server.core.service.*;
import com.jftse.server.core.shared.MetricsService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

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
    private LotteryServiceV2 lotteryServiceV2;
    @Autowired
    private LevelService levelService;
    @Autowired
    private GuardianService guardianService;
    @Autowired
    private BossGuardianService bossGuardianService;
    // A plugin's own services (and their entities/repositories) live in the plugin module itself,
    // not here - game-server can't depend on a plugin module, so they aren't autowired in this
    // class. A plugin whose own non-Spring classes need static access to such services should
    // mirror this class's own singleton getInstance() pattern with its own registry bean.
    // All beans implementing MatchTimerExtension are auto-collected by Spring - a new extension
    // just needs to be a @Component implementing the interface, no registration code needed here.
    // required=false + the empty-list default matter: with zero plugins installed (the public repo
    // on its own, no extension jar present), there are zero beans of these types, and Spring's
    // default List<T> autowiring throws NoSuchBeanDefinitionException in that case rather than
    // injecting an empty list - confirmed by actually booting game-server.jar with no plugin on the
    // classpath before this fix.
    @Autowired(required = false)
    private List<MatchTimerExtension> matchTimerExtensions = Collections.emptyList();
    @Autowired(required = false)
    private List<WaveCompletionExtension> waveCompletionExtensions = Collections.emptyList();
    @Autowired(required = false)
    private List<GuardianBattleStateProvider> guardianBattleStateProviders = Collections.emptyList();
    @Autowired(required = false)
    private List<MatchplayLifecycleExtension> matchplayLifecycleExtensions = Collections.emptyList();
    @Autowired
    private GuardianSkillsService guardianSkillsService;
    @Autowired
    private SkillDropRateService skillDropRateService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ScenarioService scenarioService;
    @Autowired
    private MapService mapService;
    @Autowired
    private EnchantService enchantService;
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProfaneWordsService profaneWordsService;

    @Autowired
    private UptimeService uptimeService;
    @Autowired
    private MetricsService metricsService;

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
    private ScriptStateService scriptStateService;

    @Autowired
    private GrpcAuthService grpcAuthService;

    @PostConstruct
    public void init() {
        instance = this;

        log.info(this.getClass().getSimpleName() + " initialized");
    }

    public static ServiceManager getInstance() {
        return instance;
    }
}
