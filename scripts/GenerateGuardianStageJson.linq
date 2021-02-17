<Query Kind="Program">
  <NuGetReference>Newtonsoft.Json</NuGetReference>
  <Namespace>Newtonsoft.Json</Namespace>
</Query>

void Main()
{
	var rubyCrab = new GuardianStage 
	{ 
		MapId = 0,
		IsBossStage = false,
		GuardiansLeft = GetRange(1, 3)
	};

	var emeraldBeach = new GuardianStage
	{
		MapId = 1,
		IsBossStage = false,
		GuardiansLeft = GetRange(1, 6)
	};

	var twinkleTown = new GuardianStage
	{
		MapId = 2,
		IsBossStage = false,
		GuardiansLeft = GetRange(7, 12)
	};

	var aeolos = new GuardianStage
	{
		MapId = 3,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18)
	};

	var lifeWood = new GuardianStage
	{
		MapId = 5,
		IsBossStage = false,
		GuardiansLeft = GetRange(1, 6),
		GuardiansRight = GetRange(19, 24)
	};

	var arena = new GuardianStage
	{
		MapId = 6,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(25, 30)
	};

	var arenaBoss = new GuardianStage
	{
		MapId = 6,
		IsBossStage = true,
		GuardiansLeft = GetRange(25, 30),
		GuardiansRight = GetRange(19, 24),
		BossGuardian = 1
	};

	var monsLava = new GuardianStage
	{
		MapId = 7,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(25, 30)
	};

	var monsLavaB = new GuardianStage
	{
		MapId = 8,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(37, 42)
	};

	var devaBergLeft = GetRange(13, 18);
	devaBergLeft.AddRange(GetRange(25, 30));
	var devaBerg = new GuardianStage
	{
		MapId = 9,
		IsBossStage = false,
		GuardiansLeft = devaBergLeft,
		GuardiansRight = GetRange(31, 36)
	};

	var devaBergBoss = new GuardianStage
	{
		MapId = 9,
		IsBossStage = true,
		GuardiansLeft = GetRange(31, 36),
		GuardiansRight = GetRange(25, 30),
		BossGuardian = 3
	};

	var atlantis = new GuardianStage
	{
		MapId = 10,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 18),
		GuardiansRight = GetRange(19, 24),
		GuardiansMiddle =  GetRange(43, 48)
	};

	var atlantisBoss = new GuardianStage
	{
		MapId = 10,
		IsBossStage = true,
		GuardiansLeft = GetRange(43, 48),
		GuardiansRight = GetRange(31, 36),
		BossGuardian = 4
	};

	var temple = new GuardianStage
	{
		MapId = 11,
		IsBossStage = false,
		GuardiansLeft = GetRange(13, 42),
		GuardiansRight = GetRange(49, 54)
	};

	var templeBoss = new GuardianStage
	{
		MapId = 11,
		IsBossStage = true,
		GuardiansLeft = GetRange(25, 30),
		GuardiansRight = GetRange(49, 54),
		BossGuardian = 5
	};

	var roomOfShadow = new GuardianStage
	{
		MapId = 12,
		IsBossStage = false,
		GuardiansLeft = GetRange(55, 60),
		GuardiansRight = GetRange(55, 60)
	};

	var machineCity = new GuardianStage
	{
		MapId = 13,
		IsBossStage = false,
		GuardiansLeft = GetRange(61, 66),
		GuardiansRight = GetRange(61, 66)
	};

	var machineCityBoss = new GuardianStage
	{
		MapId = 13,
		IsBossStage = true,
		GuardiansLeft = GetRange(61, 66),
		GuardiansRight = GetRange(61, 66),
		BossGuardian = 6
	};

	var danceTime = new GuardianStage
	{
		MapId = 14,
		IsBossStage = false,
		GuardiansLeft = GetRange(67, 72),
		GuardiansRight = GetRange(67, 72)
	};

	var danceTimeBoss = new GuardianStage
	{
		MapId = 14,
		IsBossStage = true,
		GuardiansLeft = GetRange(67, 72),
		GuardiansRight = GetRange(67, 72),
		BossGuardian = 7
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
	public int MapId { get; set; }
	
	public List<int> GuardiansLeft { get; set; }
	
	public List<int> GuardiansRight { get; set; }
	
	public List<int> GuardiansMiddle { get; set; }
	
	public bool IsBossStage { get; set; }
	
	public int BossGuardian { get; set; }
}

private List<int> GetRange(int from, int to)
{
	var count = to - from + 1;
	return Enumerable.Range(from, count).ToList();
}

// You can define other methods, fields, classes and namespaces here
