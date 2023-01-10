void Main()
{
	var battleItems = new List<GuardianBtItemList>();
	var files = Directory.GetFiles(@"C:\Users\vince\Desktop\Guardian\GdBtItem");
	foreach (var file in files)
	{
		using (var stream = new StreamReader(file))
		{
			var prefix = "Ini3_GdBtItem_";
			var fileName = Path.GetFileName(file);
			var gdBtItemIndex = int.Parse(fileName.Substring(prefix.Length, 2));
			var xmlSerializer = new XmlSerializer(typeof(GuardianBtItemList));
			var guardianBattleItems = (GuardianBtItemList) xmlSerializer.Deserialize(stream);
			guardianBattleItems.btItemId = gdBtItemIndex;
			battleItems.Add(guardianBattleItems);
		}
	}

	var btItemsJson = JsonConvert.SerializeObject(battleItems, Newtonsoft.Json.Formatting.Indented);
	File.WriteAllText(@"C:\Users\vince\Desktop\Guardian\GuardianSkills.json", btItemsJson);
}

[XmlRoot(ElementName = "GuardianBtItem")]
public class GuardianBtItem
{
	[XmlAttribute(AttributeName = "BtIndex")]
	public int skillIndex { get; set; }
	[XmlAttribute(AttributeName = "ChansPer")]
	public int chance { get; set; }
}

[XmlRoot(ElementName = "GuardianBtItemList")]
public class GuardianBtItemList
{
	[XmlIgnore]
	public int btItemId { get; set; }

	[XmlElement(ElementName = "GuardianBtItem")]
	public List<GuardianBtItem> guardianBtItems { get; set; }
}
// You can define other methods, fields, classes and namespaces here
