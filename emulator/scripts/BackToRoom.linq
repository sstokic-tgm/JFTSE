<Query Kind="Program">
  <Namespace>System.Net.Sockets</Namespace>
</Query>

void Main()
{
	TcpClient client = new TcpClient("127.0.0.1", 5895);
	var packetStream = new PacketStream(client.GetStream(), new byte[4], new byte[4]);
	var packet = new Packet(0x555);

	var packetToRelay = new Packet(0x1780); //0x1780 Backtoroom
	packetToRelay.Write((byte)1);

	packet.WriteRawData(packetToRelay.GetRawPacket());
	packetStream.Write(packet);
}


public class Packet
{
	private int _readPosition = 0;

	public Packet(Packet packet)
	{
		this.CheckSerial = packet.CheckSerial;
		this.CheckSum = packet.CheckSum;
		this.PacketId = packet.PacketId;
		this.DataLength = packet.DataLength;

		this.Data = new byte[this.DataLength];
		Buffer.BlockCopy(packet.Data, 0, this.Data, 0, this.DataLength);
	}

	public Packet(byte[] rawData)
	{
		this.CheckSerial = BitConverter.ToUInt16(rawData, 0);
		this.CheckSum = BitConverter.ToUInt16(rawData, 2);
		this.PacketId = BitConverter.ToUInt16(rawData, 4);
		this.DataLength = BitConverter.ToUInt16(rawData, 6);

		this.Data = new byte[this.DataLength];
		Buffer.BlockCopy(rawData, 8, this.Data, 0, this.DataLength);
	}

	public Packet(ushort packetId)
	{
		this.PacketId = packetId;
		this.DataLength = 0;
		this.Data = new byte[4096];
	}

	public ushort CheckSerial { get; set; }

	public ushort CheckSum { get; set; }

	public ushort PacketId { get; set; }

	public ushort DataLength { get; set; }

	public byte[] Data { get; set; }

	public static int IndexOf(byte[] array, byte[] pattern, int offset)
	{
		int success = 0;
		for (int i = offset; i < array.Length; i++)
		{
			if (array[i] == pattern[success])
			{
				success++;
			}
			else
			{
				success = 0;
			}

			if (pattern.Length == success)
			{
				return i - pattern.Length + 1;
			}
		}

		return -1;
	}

	public void Write(params object[] dataList)
	{
		foreach (object o in dataList)
		{
			this.Write(o);
		}
	}

	public byte[] AddByteToArray(byte[] byteArray, byte newByte)
	{
		byte[] newArray = new byte[byteArray.Length + 1];
		byteArray.CopyTo(newArray, 1);
		newArray[0] = newByte;
		return newArray;
	}

	public void WriteRawData(byte[] data)
	{
		Buffer.BlockCopy(data, 0, this.Data, this.DataLength, data.Length);
		this.DataLength += (ushort)data.Length;
	}

	public void Write(object element)
	{
		byte[] dataElement;
		switch (Type.GetTypeCode(element.GetType()))
		{
			case TypeCode.Int16:
				dataElement = BitConverter.GetBytes(Convert.ToInt16(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 2);
				this.DataLength += 2;
				break;
			case TypeCode.UInt16:
				dataElement = BitConverter.GetBytes(Convert.ToUInt16(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 2);
				this.DataLength += 2;
				break;

			case TypeCode.Int32:
				dataElement = BitConverter.GetBytes(Convert.ToInt32(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 4);
				this.DataLength += 4;
				break;
			case TypeCode.UInt32:
				dataElement = BitConverter.GetBytes(Convert.ToUInt32(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 4);
				this.DataLength += 4;
				break;

			case TypeCode.Int64:
				dataElement = BitConverter.GetBytes(Convert.ToInt64(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 8);
				this.DataLength += 8;
				break;
			case TypeCode.UInt64:
				dataElement = BitConverter.GetBytes(Convert.ToUInt64(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 8);
				this.DataLength += 8;
				break;

			case TypeCode.DateTime:
				dataElement = BitConverter.GetBytes(Convert.ToDateTime(element).ToFileTime());
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 8);
				this.DataLength += 8;
				break;

			case TypeCode.String:
				dataElement = Encoding.Unicode.GetBytes(Convert.ToString(element));
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, dataElement.Length);
				this.DataLength += Convert.ToUInt16(dataElement.Length);

				Buffer.BlockCopy(new byte[] { 0, 0 }, 0, this.Data, this.DataLength, 2);
				this.DataLength += 2;
				break;

			case TypeCode.Byte:
				dataElement = BitConverter.GetBytes((byte)element);
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 1);
				this.DataLength += 1;
				break;

			case TypeCode.Boolean:
				dataElement = BitConverter.GetBytes((bool)element);
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 1);
				this.DataLength += 1;
				break;

			case TypeCode.Single:
				dataElement = BitConverter.GetBytes((float)element);
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 4);
				this.DataLength += 4;
				break;

			case TypeCode.Char:
				dataElement = BitConverter.GetBytes((char)element);
				Buffer.BlockCopy(dataElement, 0, this.Data, this.DataLength, 2);
				this.DataLength += 2;
				break;
			default:
				break;
		}
	}

	public int ReadInteger()
	{
		int element = BitConverter.ToInt32(this.Data, this._readPosition);
		this._readPosition += 4;
		return element;
	}

	public byte ReadByte()
	{
		byte result = this.Data[this._readPosition];
		this._readPosition += 1;
		return result;
	}

	public bool ReadBoolean()
	{
		byte result = this.Data[this._readPosition];
		this._readPosition += 1;
		return Convert.ToBoolean(result);
	}

	public void ReadByte(out byte element)
	{
		element = this.Data[this._readPosition];
		this._readPosition += 1;
	}

	public short ReadShort()
	{
		short result = BitConverter.ToInt16(this.Data, this._readPosition);
		this._readPosition += 2;
		return result;
	}

	public string ReadUnicodeString()
	{
		string result = string.Empty;
		int stringLength = IndexOf(this.Data, new byte[] { 0x00, 0x00 }, this._readPosition) + 1 - this._readPosition;
		if (stringLength > 1)
		{
			result = Encoding.Unicode.GetString(this.Data, this._readPosition, stringLength);
			this._readPosition += stringLength + 2;
		}
		else
		{
			result = string.Empty;
			this._readPosition += 2;
		}

		return result;
	}

	public string ReadString()
	{
		string result = string.Empty;
		int stringLength = IndexOf(this.Data, new byte[] { 0x00 }, this._readPosition) - this._readPosition;
		if (stringLength > 0)
		{
			result = Encoding.ASCII.GetString(this.Data, this._readPosition, stringLength);
			this._readPosition += stringLength + 1;
		}

		return result;
	}

	public byte[] GetRawPacket()
	{
		byte[] p = new byte[8 + this.DataLength];

		byte[] serial = BitConverter.GetBytes(this.CheckSerial);
		byte[] check = BitConverter.GetBytes(this.CheckSum);
		byte[] id = BitConverter.GetBytes(this.PacketId);
		byte[] dataLength = BitConverter.GetBytes(this.DataLength);

		Buffer.BlockCopy(serial, 0, p, 0, 2);
		Buffer.BlockCopy(check, 0, p, 2, 2);
		Buffer.BlockCopy(id, 0, p, 4, 2);
		Buffer.BlockCopy(dataLength, 0, p, 6, 2);
		Buffer.BlockCopy(this.Data, 0, p, 8, this.DataLength);

		return p;
	}
}

public class PacketStream
{
	public static readonly byte[] SerialTable =
	{
															0xF2, 0x30, 0x75, 0x86, 0xD4, 0x7D, 0x57, 0x38, 0x6E, 0x68,
															0x4F, 0x7E, 0x30, 0x58, 0xED, 0x7D, 0x5C, 0x47, 0xC3, 0x31,
															0xCA, 0x2B, 0x5F, 0x56, 0xC8, 0x7A, 0x65, 0x34, 0xF6, 0x62,
															0x31, 0x5B, 0x00, 0x38, 0x15, 0x5B, 0xD8, 0x2F, 0xA7, 0x57,
															0xB8, 0x79, 0x3D, 0x3C, 0x40, 0x6C, 0xFB, 0x89, 0xBE, 0x63,
															0x19, 0x5F, 0x36, 0x57, 0xC1, 0x81, 0xEC, 0x52, 0x15, 0x58,
															0x2A, 0x35, 0x3B, 0x7F, 0x6A, 0x7E, 0xF9, 0x40, 0x44, 0x7E,
															0xF7, 0x3F, 0xD8, 0x6E, 0xA5, 0x57, 0xA8, 0x2D, 0x43, 0x57,
															0xC2, 0x56, 0x4D, 0x63, 0xF4, 0xCB, 0xBD, 0x81, 0x4E, 0x7E,
															0xB5, 0x5E, 0x1A, 0x5F, 0xB1, 0x5A, 0x8A, 0x37, 0xB5, 0x53,
															0x14, 0xA5, 0xEB, 0x56, 0x5B, 0x60, 0xD1, 0x63, 0x70, 0x57,
															0xF5, 0x64, 0xC6, 0xAD, 0xD7, 0x57, 0xCC, 0x5E, 0x2D, 0x31,
															0x04, 0x7E, 0xEB, 0x56, 0xE7, 0x38, 0xE5, 0x63, 0xD4, 0x57,
															0x3D, 0x59, 0x96, 0x38, 0x77, 0x67, 0xC0, 0x60, 0x2D, 0x31,
															0x1A, 0xD1, 0xD9, 0x86, 0xDE, 0x7D, 0x07, 0x4C, 0xCE, 0x58,
															0x87, 0x7D, 0x08, 0x58, 0xD9, 0x7D, 0x04, 0x2C, 0xCF, 0x2F,
															0x16, 0x7B, 0xB7, 0x58, 0xFA, 0x7A, 0x45, 0x40, 0xEA, 0x64,
															0x73, 0x82, 0x46, 0x5B, 0x79, 0x5B, 0xC0, 0x7E, 0xC5, 0x57,
															0x58, 0x89, 0x69, 0x3D, 0x86, 0x6C, 0xB5, 0x89, 0x2E, 0x62,
															0xE9, 0x66, 0x66, 0x59, 0xDF, 0x81, 0xB4, 0x53, 0xCD, 0x63,
															0xDC, 0x7D, 0x8B, 0x57, 0x84, 0x91, 0xE9, 0x5A, 0x60, 0x30,
															0xB1, 0x67, 0x0A, 0x38, 0x81, 0x62, 0x72, 0x3B, 0x55, 0x63,
															0x62, 0x34, 0x31, 0x7F, 0x38, 0x7E, 0x59, 0x31, 0xCC, 0x91,
															0xBF, 0x40, 0xE2, 0x6E, 0xD7, 0x57, 0xE0, 0x2C, 0x2B, 0x5B,
															0x04, 0x7E, 0xBD, 0x57, 0x84, 0x91, 0x79, 0x5C, 0x8C, 0x31,
															0xC5, 0x67, 0x1E, 0x38, 0xB3, 0x62, 0x02, 0x3D, 0x3D, 0x67,
															0x62, 0x34, 0x31, 0x7F, 0x38, 0x7E, 0x77, 0x31, 0xCC, 0x91,
															0x23, 0x41, 0x0E, 0x70, 0x9F, 0x58, 0xA8, 0x2D, 0x49, 0x5B,
															0x2E, 0xD1, 0x21, 0x85, 0x9E, 0x77, 0xD7, 0x3A, 0xB0, 0x58,
															0xB7, 0x75, 0x08, 0x58, 0xD9, 0x7D, 0x04, 0x2C, 0xCF, 0x2F,
															0x94, 0x7F, 0x7B, 0x58, 0xE7, 0x38, 0xDB, 0x63, 0xC0, 0x57,
															0x75, 0x58, 0x66, 0x40, 0x3F, 0x68, 0x5C, 0x60, 0xCD, 0x40,
															0x5E, 0x89, 0x6E, 0x3D, 0x72, 0x6C, 0xBF, 0x89, 0x22, 0x64,
															0xE9, 0x66, 0x06, 0x69, 0x97, 0x8D, 0x7C, 0x54, 0x31, 0x64,
															0x02, 0x2B, 0x6B, 0x68, 0xC2, 0x7B, 0xEF, 0x40, 0x36, 0x64,
															0x4B, 0x87, 0x68, 0x5C, 0x77, 0x67, 0x64, 0x8F, 0xCD, 0x4F,
															0x8E, 0x35, 0xF9, 0x7F, 0x74, 0x7E, 0x81, 0x31, 0xFE, 0x91,
															0x87, 0x41, 0x72, 0x70, 0x03, 0x59, 0xD6, 0x2C, 0x53, 0x5B,
															0xF6, 0xD1, 0xDD, 0x87, 0xD0, 0x77, 0xE1, 0x3A, 0xF6, 0x58,
															0xDF, 0x75, 0xD0, 0x58, 0x15, 0x7E, 0xD2, 0x2B, 0xF7, 0x2F,
															0x5E, 0x79, 0xFB, 0x50, 0x26, 0x7C, 0x59, 0x40, 0x56, 0x62,
															0xAF, 0x87, 0xCC, 0x5C, 0xB5, 0x5B, 0xC3, 0x7F, 0xF9, 0x50,
															0x00, 0x3C, 0xDD, 0x2D, 0xFA, 0x34, 0x91, 0x5D, 0xC4, 0x30,
															0xE1, 0x5F, 0x6E, 0x38, 0x49, 0x63, 0xBA, 0x2F, 0x85, 0xD8,
															0x1E, 0x7A, 0xED, 0x56, 0xB5, 0x38, 0xB3, 0x63, 0xCA, 0x57,
															0x49, 0x57, 0x32, 0x38, 0xE3, 0x5A, 0x90, 0x54, 0x15, 0x67,
															0x62, 0x89, 0x9B, 0x3D, 0x7C, 0x6C, 0x8D, 0x89, 0x8E, 0x52,
															0x21, 0x66, 0xD6, 0x57, 0xDF, 0x81, 0xB4, 0x53, 0x2D, 0x54,
															0x62, 0x34, 0x1B, 0x30, 0x38, 0x7E, 0x45, 0x31, 0xCC, 0x91,
															0x83, 0x40, 0x8A, 0x53, 0xAB, 0x56, 0x9A, 0x2C, 0x17, 0x5B,
															0x12, 0x52, 0xFB, 0x50, 0xCE, 0x79, 0x27, 0x40, 0x42, 0x62,
															0x1F, 0x86, 0x10, 0x5A, 0xBF, 0x5B, 0xDC, 0x7B, 0xCD, 0x4F,
															0xDC, 0x7D, 0x8B, 0x57, 0x84, 0x91, 0xB9, 0x62, 0x28, 0x31,
															0x15, 0x68, 0x39, 0x30, 0xB1, 0x5A, 0x39, 0x30, 0x75, 0x34,
															0x2E, 0xD1, 0xEF, 0x84, 0x9E, 0x77, 0xD7, 0x3A, 0x20, 0x57,
															0xB7, 0x75, 0x78, 0x56, 0xD9, 0x7D, 0x04, 0x2C, 0x6B, 0x2F,
															0xE4, 0x2F, 0x49, 0x57, 0xF7, 0x5F, 0x2D, 0x58, 0x5C, 0x6B,
															0x25, 0x5D, 0x96, 0x38, 0x67, 0x40, 0x8E, 0x60, 0x19, 0x31,
															0x1B, 0x30, 0xC9, 0x2D, 0x76, 0x45, 0x15, 0x7A, 0x2E, 0x62,
															0x61, 0x53, 0xAA, 0x56, 0x9B, 0x84, 0x3C, 0x67, 0xCD, 0x63,
															0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
														};

	private readonly NetworkStream _baseStream;

	private readonly byte[] _decryptKey;

	private readonly byte[] _encryptKey;

	private int _header1Key = 0;

	private int _indicator;

	public PacketStream(NetworkStream stream, byte[] decryptKey, byte[] encryptKey)
	{
		this._baseStream = stream;
		this._decryptKey = decryptKey;
		this._encryptKey = encryptKey;
	}

	public byte[] DecryptBytes(byte[] encryptedBuffer, int size)
	{
		byte[] decrypted = new byte[size];
		Array.Copy(encryptedBuffer, decrypted, size);

		for (int i = 0; i < size; ++i)
		{
			decrypted[i] ^= this._decryptKey[(i & 3)];
		}

		return decrypted;
	}

	public short CreateSerial(byte[] data)
	{
		int pos = (((this._header1Key << 4) - this._header1Key * 4 + this._indicator) * 2);
		short header = BitConverter.ToInt16(SerialTable, pos);

		data[0] = BitConverter.GetBytes(header)[0];
		data[1] = BitConverter.GetBytes(header)[1];

		this._indicator += 1;
		this._indicator %= 60;

		return header;
	}

	public short CreateCheckSum(byte[] data)
	{
		short v2 = Convert.ToInt16(data[0] + data[1] + data[4] + data[5] + data[6] + data[7]);
		long tempV2 = v2 & 0x80000001;
		bool v1 = tempV2 == 0;
		short result;

		if (tempV2 < 0)
		{
			v1 = ((tempV2 - 1) | 0xFFFFFFFE) == -1;
		}

		if (v1)
		{
			result = Convert.ToInt16(v2 + 1587);

			data[2] = BitConverter.GetBytes(result)[0];
			data[3] = BitConverter.GetBytes(result)[1];
		}
		else
		{
			result = Convert.ToInt16(v2 + 1568);
			data[2] = BitConverter.GetBytes(result)[0];
			data[3] = BitConverter.GetBytes(result)[1];
		}

		return result;
	}

	public byte[] EncryptBytes(byte[] decryptedBuffer, int size)
	{
		return decryptedBuffer;
	}

	public int Read(byte[] buffer, int offset, int size)
	{
		byte[] encryptedBuffer = new byte[size];
		int result = this._baseStream.Read(encryptedBuffer, 0, size);
		Buffer.BlockCopy(this.DecryptBytes(encryptedBuffer, size), 0, buffer, offset, size);
		return result;
	}

	public void Write(byte[] buffer, int offset, int size)
	{
		this.CreateSerial(buffer);
		this.CreateCheckSum(buffer);
		byte[] encrypted = this.EncryptBytes(buffer, buffer.Length);
		this._baseStream.Write(buffer, offset, size);
	}

	public void Write(Packet packet)
	{
		Console.WriteLine($"SEND [{packet.PacketId:X4}] {BitConverter.ToString(packet.GetRawPacket(), 0, packet.DataLength + 8)}");
		this.Write(packet.GetRawPacket(), 0, packet.DataLength + 8);
	}
}
// You can define other methods, fields, classes and namespaces here
