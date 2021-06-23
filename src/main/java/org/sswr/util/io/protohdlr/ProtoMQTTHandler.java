package org.sswr.util.io.protohdlr;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.ProtocolDataListener;
import org.sswr.util.io.ProtocolHandler;

public class ProtoMQTTHandler implements ProtocolHandler
{
	class ClientData
	{
		public byte packetType;
		public int packetSize;
		public int packetDataSize;
		public byte[] packetBuff;
	}
	private ProtocolDataListener listener;

	public ProtoMQTTHandler(ProtocolDataListener listener)
	{
		this.listener = listener;
	}

	public Object createStreamData(IOStream stm)
	{
		ClientData cliData = new ClientData();
		cliData.packetBuff = null;
		cliData.packetSize = 0;
		cliData.packetDataSize = 0;
		cliData.packetType = 0;
		return cliData;
	}

	public void deleteStreamData(IOStream stm, Object stmData)
	{

	}
	
	public int parseProtocol(IOStream stm, Object stmObj, Object stmData, byte[] buff, int buffOfst, int buffSize)
	{
		ClientData cliData = (ClientData)stmData;
		if (cliData.packetBuff != null)
		{
			if (cliData.packetSize - cliData.packetDataSize <= buffSize)
			{
				ByteTool.copyArray(cliData.packetBuff, cliData.packetDataSize, buff, 0, cliData.packetSize - cliData.packetDataSize);
				this.listener.dataParsed(stm, stmObj, cliData.packetType, 0, cliData.packetBuff, 0, cliData.packetSize);
				cliData.packetBuff = null;
				if (cliData.packetSize - cliData.packetDataSize == buffSize)
				{
					return 0;
				}
				buffOfst += cliData.packetSize - cliData.packetDataSize;
				buffSize -= cliData.packetSize - cliData.packetDataSize;
			}
			else
			{
				ByteTool.copyArray(cliData.packetBuff, cliData.packetDataSize, buff, buffOfst, buffSize);
				cliData.packetDataSize += buffSize;
				return 0;
			}
		}
		int packetSize;
		int i;
		while (buffSize >= 2)
		{
			if ((buff[buffOfst + 1] & 0x80) != 0)
			{
				if (buffSize < 5)
					break;
				if ((buff[buffOfst + 2] & 0x80) != 0)
				{
					if ((buff[buffOfst + 3] & 0x80) != 0)
					{
						packetSize = ((buff[buffOfst + 1] & 0x7f) | ((buff[buffOfst + 2] & 0x7f) << 7) | ((buff[buffOfst + 3] & 0x7f) << 14) | ((buff[buffOfst + 4] & 0xff) << 21));
						i = 5;
					}
					else
					{
						packetSize = ((buff[buffOfst + 1] & 0x7f) | ((buff[buffOfst + 2] & 0x7f) << 7) | ((buff[buffOfst + 3] & 0xff) << 14));
						i = 4;
					}
				}
				else
				{
					packetSize = ((buff[buffOfst + 1] & 0x7f) | ((buff[buffOfst + 2] & 0xff) << 7));
					i = 3;
				}
			}
			else
			{
				packetSize = buff[buffOfst + 1] & 0xff;
				i = 2;
			}
			if (buffSize >= packetSize + i)
			{
				this.listener.dataParsed(stm, stmObj, buff[0], 0, buff, buffOfst + i, packetSize);
				if (buffSize == packetSize + i)
				{
					return 0;
				}
				buffOfst += packetSize + i;
				buffSize -= packetSize + i;
			}
			else
			{
				cliData.packetBuff = new byte[packetSize];
				cliData.packetSize = packetSize;
				cliData.packetType = buff[buffOfst + 0];
				ByteTool.copyArray(cliData.packetBuff, 0, buff, buffOfst + i, buffSize - i);
				cliData.packetDataSize = buffSize - i;
				return 0;
			}
		}
		return buffSize;
	}

	public int buildPacket(byte[] buff, int buffOfst, int cmdType, int seqId, byte[] cmd, int cmdOfst, int cmdSize, Object stmData)
	{
		buff[buffOfst + 0] = (byte)(cmdType & 0xff);
		if (cmdSize < 128)
		{
			buff[buffOfst + 1] = (byte)cmdSize;
			if (cmdSize > 0)
			{
				ByteTool.copyArray(buff, buffOfst + 2, cmd, cmdOfst, cmdSize);
			}
			return cmdSize + 2;
		}
		else if (cmdSize < 16384)
		{
			buff[buffOfst + 1] = (byte)((cmdSize & 0x7F) | 0x80);
			buff[buffOfst + 2] = (byte)(cmdSize >> 7);
			ByteTool.copyArray(buff, buffOfst + 3, cmd, cmdOfst, cmdSize);
			return cmdSize + 3;
		}
		else if (cmdSize < 2097152)
		{
			buff[buffOfst + 1] = (byte)((cmdSize & 0x7F) | 0x80);
			cmdSize >>= 7;
			buff[buffOfst + 2] = (byte)((cmdSize & 0x7F) | 0x80);
			buff[buffOfst + 3] = (byte)(cmdSize >> 7);
			ByteTool.copyArray(buff, buffOfst + 4, cmd, cmdOfst, cmdSize);
			return cmdSize + 4;
		}
		else if (cmdSize < 268435456)
		{
			buff[buffOfst + 1] = (byte)((cmdSize & 0x7F) | 0x80);
			cmdSize >>= 7;
			buff[buffOfst + 2] = (byte)((cmdSize & 0x7F) | 0x80);
			cmdSize >>= 7;
			buff[buffOfst + 3] = (byte)((cmdSize & 0x7F) | 0x80);
			buff[buffOfst + 4] = (byte)(cmdSize >> 7);
			ByteTool.copyArray(buff, buffOfst + 5, cmd, cmdOfst, cmdSize);
			return cmdSize + 5;
		}
		else
		{
			return 0;
		}
	}

	public boolean parseUTF8Str(byte[] buff, SharedInt index, int buffSize, StringBuilder sb)
	{
		int strSize;
		if ((buffSize - index.value) < 2)
			return false;
		strSize = ByteTool.readMUInt16(buff, index.value);
		if (buffSize - 2 - index.value < strSize)
		{
			return false;
		}
		sb.append(new String(buff, 2 + index.value, strSize));
		index.value = strSize + 2 + index.value;
		return true;
	}
}
