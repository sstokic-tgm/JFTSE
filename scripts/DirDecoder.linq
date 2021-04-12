<Query Kind="Program" />

void Main()
{
	var files = Directory.GetFiles(@"C:\Users\vince\Desktop\Guardian\FT_RES_TOOL_EXE\FT_RES_TOOL_EXE\Info");
	foreach (var file in files)
	{
		var process = Process.Start(@"C:\Users\vince\Desktop\Guardian\FT_RES_TOOL_EXE\FT_RES_TOOL_EXE\decomp.exe", file);
	}
}

// You can define other methods, fields, classes and namespaces here
