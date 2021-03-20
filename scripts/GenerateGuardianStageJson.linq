void Main()
{
	var rubyCrab = new GuardianStage
	{
		Name = "RubyCrab",
		MapId = 0,
		IsBossStage = false,
		GuardiansLeft = GetRange(1, 3),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 1
	};

	var emeraldBeach = new GuardianStage
	{
		Name = "EmeraldBeach",
		MapId = 1,
		IsBossStage = false,
		GuardiansLeft = GetRange(1, 6),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 1
	};

	var twinkleTown = new GuardianStage
	{
		Name = "TwinkleTown",
		MapId = 2,
		IsBossStage = false,
		GuardiansLeft = GetRange(7, 12),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 1
	};

	var aeolos = new GuardianStage
	{
		Name = "Aeolos",
		MapId = 3,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 1
	};

	var lifeWood = new GuardianStage
	{
		Name = "LifeWood",
		MapId = 5,
		IsBossStage = false,
		GuardiansLeft = GetRange(1, 6),
		GuardiansRight = GetRange(19, 24),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 1
	};

	var arena = new GuardianStage
	{
		Name = "Arena",
		MapId = 6,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(25, 30),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = 180,
		ExpMultiplier = 3
	};

	var arenaBoss = new GuardianStage
	{
		Name = "ArenaBoss",
		MapId = 6,
		IsBossStage = true,
		GuardiansLeft = GetRange(25, 30),
		GuardiansRight = GetRange(19, 24),
		BossGuardian = 1,
		DefeatTimerInSeconds = 300,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 3
	};

	var monsLava = new GuardianStage
	{
		Name = "MonsLava",
		MapId = 7,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(25, 30),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 3
	};

	var monsLavaB = new GuardianStage
	{
		Name = "MonsLavaB",
		MapId = 8,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(37, 42),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 3
	};

	var devaBergLeft = GetRange(13, 18);
	devaBergLeft.AddRange(GetRange(25, 30));
	var devaBerg = new GuardianStage
	{
		Name = "DevaBerg",
		MapId = 9,
		IsBossStage = false,
		GuardiansLeft = devaBergLeft,
		GuardiansRight = GetRange(31, 36),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = 180,
		ExpMultiplier = 3
	};

	var devaBergBoss = new GuardianStage
	{
		Name = "DevaBergBoss",
		MapId = 9,
		IsBossStage = true,
		GuardiansLeft = GetRange(31, 36),
		GuardiansRight = GetRange(25, 30),
		BossGuardian = 3,
		DefeatTimerInSeconds = 300,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 3
	};

	var atlantis = new GuardianStage
	{
		Name = "Atlantis",
		MapId = 10,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(19, 24),
		GuardiansMiddle = GetRange(43, 48),
		DefeatTimerInSeconds = 480,
		BossTriggerTimerInSeconds = 240,
		ExpMultiplier = 4
	};

	var atlantisBoss = new GuardianStage
	{
		Name = "AtlantisBoss",
		MapId = 10,
		IsBossStage = true,
		GuardiansLeft = GetRange(43, 48),
		GuardiansRight = GetRange(31, 36),
		BossGuardian = 4,
		DefeatTimerInSeconds = 300,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 4
	};

	var temple = new GuardianStage
	{
		Name = "Temple",
		MapId = 11,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 42),
		GuardiansRight = GetRange(49, 54),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = 180,
		ExpMultiplier = 5
	};

	var templeBoss = new GuardianStage
	{
		Name = "TempleBoss",
		MapId = 11,
		IsBossStage = true,
		GuardiansLeft = GetRange(25, 30),
		GuardiansRight = GetRange(49, 54),
		BossGuardian = 5,
		DefeatTimerInSeconds = 300,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 5
	};

	var roomOfShadow = new GuardianStage
	{
		Name = "RoomOfShadow",
		MapId = 12,
		IsBossStage = false,
		GuardiansLeft = GetRange(55, 60),
		GuardiansRight = GetRange(55, 60),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 1
	};

	var machineCity = new GuardianStage
	{
		Name = "MachineCity",
		MapId = 13,
		IsBossStage = false,
		GuardiansLeft = GetRange(61, 66),
		GuardiansRight = GetRange(61, 66),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = 180,
		ExpMultiplier = 6
	};

	var machineCityBoss = new GuardianStage
	{
		Name = "MachineCityBoss",
		MapId = 13,
		IsBossStage = true,
		GuardiansLeft = GetRange(61, 66),
		GuardiansRight = GetRange(61, 66),
		BossGuardian = 6,
		DefeatTimerInSeconds = 300,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 6
	};

	var danceTime = new GuardianStage
	{
		Name = "DanceTime",
		MapId = 14,
		IsBossStage = false,
		GuardiansLeft = GetRange(67, 72),
		GuardiansRight = GetRange(67, 72),
		DefeatTimerInSeconds = -1,
		BossTriggerTimerInSeconds = 180,
		ExpMultiplier = 7
	};

	var danceTimeBoss = new GuardianStage
	{
		Name = "DanceTimeBoss",
		MapId = 14,
		IsBossStage = true,
		GuardiansLeft = GetRange(67, 72),
		GuardiansRight = GetRange(67, 72),
		BossGuardian = 7,
		DefeatTimerInSeconds = 300,
		BossTriggerTimerInSeconds = -1,
		ExpMultiplier = 7
	};

	var guardianStages = new List<GuardianStage>
	{
		rubyCrab,
		emeraldBeach,
		twinkleTown,
		aeolos,
		lifeWood,
		arena,
		arenaBoss,
		monsLava,
		monsLavaB,
		devaBerg,
		devaBergBoss,
		atlantis,
		atlantisBoss,
		temple,
		templeBoss,
		roomOfShadow,
		machineCity,
		machineCityBoss,
		danceTime,
		danceTimeBoss
	};

	var guardianStagesJson = JsonConvert.SerializeObject(guardianStages, Newtonsoft.Json.Formatting.Indented);
	File.WriteAllText(@"C:\Users\vince\Desktop\Guardian\GuardianStages.json", guardianStagesJson);
}

public class GuardianStage
{
	public string Name { get; set; }

	public int MapId { get; set; }

	public List<int> GuardiansLeft { get; set; }

	public List<int> GuardiansRight { get; set; }

	public List<int> GuardiansMiddle { get; set; }

	public bool IsBossStage { get; set; }

	public int BossGuardian { get; set; }

	public int DefeatTimerInSeconds { get; set; }

	public int BossTriggerTimerInSeconds { get; set; }

	public int ExpMultiplier { get; set; }
}

private List<int> GetRange(int from, int to)
{
	var count = to - from + 1;
	return Enumerable.Range(from, count).ToList();
}

// You can define other methods, fields, classes and namespaces here
