package org.sswr.util.net;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;

public class SNMPInfo
{
	public SNMPInfo()
	{
	}

	public void pduSeqGetDetail(byte[] pdu, int pduOfst, int pduSize, int level, StringBuilder sb)
	{
		if (level > 0)
		{
			StringUtil.appendChar(sb, '\t', level);
		}
		sb.append("{\r\n");
		int i = 0;
		while (i < pduSize)
		{
			i += this.pduGetDetail(null, pdu, pduOfst + i, pduSize - i, level + 1, sb);
			if (i < pduSize)
			{
				sb.append(",\r\n");
			}
		}
		sb.append("\r\n");
		if (level > 0)
		{
			StringUtil.appendChar(sb, '\t', level);
		}
		sb.append("}");
	}
	
	public int pduGetDetail(String name, byte[] pdu, int pduOfst, int pduSize, int level, StringBuilder sb)
	{
		if (pduSize < 2)
		{
			return pduSize;
		}
		if (level > 0)
		{
			StringUtil.appendChar(sb, '\t', level);
		}
		if (name != null)
		{
			sb.append(name);
			sb.append(' ');
		}
		byte t = pdu[pduOfst + 0];
		int len = pdu[pduOfst + 1] & 0xff;
		int hdrSize = 2;
		if ((len & 0x80) != 0)
		{
			if (len == 0x81)
			{
				len = pdu[pduOfst + 2] & 0xff;
				hdrSize = 3;
			}
			else if (len == 0x82)
			{
				len = ByteTool.readMUInt16(pdu, pduOfst + 2);
				hdrSize = 4;
			}
			else if (len == 0x83)
			{
				len = ByteTool.readMUInt24(pdu, pduOfst + 2);
				hdrSize = 5;
			}
			else if (len == 0x84)
			{
				len = ByteTool.readMInt32(pdu, pduOfst + 2);
				hdrSize = 6;
			}
		}
		if (hdrSize + len > pduSize)
		{
			StringUtil.appendHex(sb, pdu, pduOfst, pduSize, ' ', LineBreakType.NONE);
			return pduSize;
		}
		switch (t & 0xff)
		{
		case 2:
			sb.append("INTEGER ");
			if (len == 1)
			{
				sb.append(pdu[pduOfst + hdrSize]);
			}
			else if (len == 2)
			{
				sb.append(ByteTool.readMInt16(pdu, pduOfst + hdrSize));
			}
			else if (len == 3)
			{
				sb.append(ByteTool.readMInt24(pdu, pduOfst + hdrSize));
			}
			else if (len == 4)
			{
				sb.append(ByteTool.readMInt32(pdu, pduOfst + hdrSize));
			}
			else
			{
				StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			}
			return len + hdrSize;
		case 4:
			sb.append("OCTET STRING ");
			{
				boolean isBin = false;
				int i = 0;
				while (i < len)
				{
					if (pdu[pduOfst + i + hdrSize] < 0x20 || pdu[pduOfst + i + hdrSize] >= 0x7f)
					{
						isBin = true;
						break;
					}
					i++;
				}
				if (isBin)
				{
					StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
				}
				else
				{
					sb.append("\"");
					sb.append(new String(pdu, pduOfst + hdrSize, len, StandardCharsets.UTF_8));
					sb.append("\"");
				}
			}
			return len + hdrSize;
		case 5:
			sb.append("NULL");
			if (len > 0)
			{
				sb.append(' ');
				StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			}
			return len + hdrSize;
		case 6:
			sb.append("OBJECT IDENTIFIER ");
			SNMPUtil.oidToString(pdu, pduOfst + hdrSize, len, sb);
			return len + hdrSize;
		case 0x30:
			sb.append("SEQUENCE\r\n");
			this.pduSeqGetDetail(pdu, pduOfst + hdrSize, len, level, sb);
			return len + hdrSize;
		case 0x40:
			sb.append("IpAddress ");
			if (len == 4)
			{
				sb.append(SocketUtil.getIPv4Name(pdu, pduOfst + hdrSize));
			}
			else
			{
				StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			}
			return len + hdrSize;
		case 0x41:
			sb.append("Counter32 ");
			if (len == 1)
			{
				sb.append(pdu[pduOfst + hdrSize] & 0xff);
			}
			else if (len == 2)
			{
				sb.append(ByteTool.readMUInt16(pdu, pduOfst + hdrSize));
			}
			else if (len == 3)
			{
				sb.append(ByteTool.readMUInt24(pdu, pduOfst + hdrSize));
			}
			else if (len == 4)
			{
				sb.append(ByteTool.readMInt32(pdu, pduOfst + hdrSize));
			}
			else
			{
				StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			}
			return len + hdrSize;
		case 0x42:
			sb.append("Gauge32 ");
			if (len == 1)
			{
				sb.append(pdu[pduOfst + hdrSize] & 0xff);
			}
			else if (len == 2)
			{
				sb.append(ByteTool.readMUInt16(pdu, pduOfst + hdrSize));
			}
			else if (len == 3)
			{
				sb.append(ByteTool.readMUInt24(pdu, pduOfst + hdrSize));
			}
			else if (len == 4)
			{
				sb.append(ByteTool.readMInt32(pdu, pduOfst + hdrSize));
			}
			else
			{
				StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			}
			return len + hdrSize;
		case 0x43:
			sb.append("Timeticks ");
			if (len == 1)
			{
				sb.append(pdu[pduOfst + hdrSize] & 0xff);
			}
			else if (len == 2)
			{
				sb.append(ByteTool.readMUInt16(pdu, pduOfst + hdrSize));
			}
			else if (len == 3)
			{
				sb.append(ByteTool.readMUInt24(pdu, pduOfst + hdrSize));
			}
			else if (len == 4)
			{
				sb.append(ByteTool.readMInt32(pdu, pduOfst + hdrSize));
			}
			else
			{
				StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			}
			return len + hdrSize;
		case 0xA0:
			sb.append("GetRequest-PDU\r\n");
			this.pduSeqGetDetail(pdu, pduOfst + hdrSize, len, level, sb);
			return len + hdrSize;
		case 0xA1:
			sb.append("GetNextRequest-PDU\r\n");
			this.pduSeqGetDetail(pdu, pduOfst + hdrSize, len, level, sb);
			return len + hdrSize;
		case 0xA2:
			sb.append("GetResponse-PDU\r\n");
			this.pduSeqGetDetail(pdu, pduOfst + hdrSize, len, level, sb);
			return len + hdrSize;
		case 0xA3:
			sb.append("SetRequest-PDU\r\n");
			this.pduSeqGetDetail(pdu, pduOfst + hdrSize, len, level, sb);
			return len + hdrSize;
		case 0xA4:
			sb.append("Trap-PDU\r\n");
			this.pduSeqGetDetail(pdu, pduOfst + hdrSize, len, level, sb);
			return len + hdrSize;
		default:
			sb.append("UNKNOWN(");
			sb.append(t & 0xff);
			sb.append(") ");
			StringUtil.appendHex(sb, pdu, pduOfst + hdrSize, len, ' ', LineBreakType.NONE);
			return len + hdrSize;
		}
	}
	
	public static void valueToString(byte type, byte[] pduBuff, int pduOfst, int valLen, StringBuilder sb)
	{
		switch (type & 0xff)
		{
		case 2:
			if (valLen == 1)
			{
				sb.append(pduBuff[pduOfst + 0]);
			}
			else if (valLen == 2)
			{
				sb.append(ByteTool.readMInt16(pduBuff, pduOfst));
			}
			else if (valLen == 3)
			{
				sb.append(ByteTool.readMInt24(pduBuff, pduOfst));
			}
			else if (valLen == 4)
			{
				sb.append(ByteTool.readMInt32(pduBuff, pduOfst));
			}
			else
			{
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 4:
			{
				boolean isBin = false;
				int i = 0;
				while (i < valLen)
				{
					if (pduBuff[pduOfst + i] < 0x20 || pduBuff[pduOfst + i] >= 0x7f)
					{
						isBin = true;
						break;
					}
					i++;
				}
				if (isBin)
				{
					StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
				}
				else
				{
					sb.append("\"");
					if (valLen > 0)
					{
						sb.append(new String(pduBuff, pduOfst, valLen, StandardCharsets.UTF_8));
					}
					sb.append("\"");
				}
			}
			break;
		case 5:
			if (valLen > 0)
			{
				sb.append(' ');
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 6:
			SNMPUtil.oidToString(pduBuff, pduOfst, valLen, sb);
			break;
		case 0x30:
			{
				SNMPInfo snmp = new SNMPInfo();
				snmp.pduSeqGetDetail(pduBuff, pduOfst, valLen, 0, sb);
			}
			break;
		case 0x40:
			if (valLen == 4)
			{
				sb.append(SocketUtil.getIPv4Name(pduBuff, pduOfst));
			}
			else
			{
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 0x41:
			if (valLen == 1)
			{
				sb.append(pduBuff[pduOfst + 0] & 0xff);
			}
			else if (valLen == 2)
			{
				sb.append(ByteTool.readMUInt16(pduBuff, pduOfst));
			}
			else if (valLen == 3)
			{
				sb.append(ByteTool.readMUInt24(pduBuff, pduOfst));
			}
			else if (valLen == 4)
			{
				sb.append(ByteTool.readMInt32(pduBuff, pduOfst));
			}
			else
			{
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 0x42:
			if (valLen == 1)
			{
				sb.append(pduBuff[pduOfst + 0] & 0xff);
			}
			else if (valLen == 2)
			{
				sb.append(ByteTool.readMUInt16(pduBuff, pduOfst));
			}
			else if (valLen == 3)
			{
				sb.append(ByteTool.readMUInt24(pduBuff, pduOfst));
			}
			else if (valLen == 4)
			{
				sb.append(ByteTool.readMInt32(pduBuff, pduOfst));
			}
			else
			{
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 0x43:
			if (valLen == 1)
			{
				sb.append(pduBuff[pduOfst + 0] & 0xff);
			}
			else if (valLen == 2)
			{
				sb.append(ByteTool.readMUInt16(pduBuff, pduOfst));
			}
			else if (valLen == 3)
			{
				sb.append(ByteTool.readMUInt24(pduBuff, pduOfst));
			}
			else if (valLen == 4)
			{
				sb.append(ByteTool.readMInt32(pduBuff, pduOfst));
			}
			else
			{
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 0x46:
			if (valLen == 1)
			{
				sb.append(pduBuff[pduOfst + 0] & 0xff);
			}
			else if (valLen == 2)
			{
				sb.append(ByteTool.readMUInt16(pduBuff, pduOfst));
			}
			else if (valLen == 3)
			{
				sb.append(ByteTool.readMUInt24(pduBuff, pduOfst));
			}
			else if (valLen == 4)
			{
				sb.append(ByteTool.readMInt32(pduBuff, pduOfst));
			}
			else if (valLen == 8)
			{
				sb.append(ByteTool.readMInt64(pduBuff, pduOfst));
			}
			else
			{
				StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			}
			break;
		case 0xA0:
			{
				SNMPInfo snmp = new SNMPInfo();
				snmp.pduSeqGetDetail(pduBuff, pduOfst, valLen, 0, sb);
			}
			break;
		case 0xA1:
			{
				SNMPInfo snmp = new SNMPInfo();
				snmp.pduSeqGetDetail(pduBuff, pduOfst, valLen, 0, sb);
			}
			break;
		case 0xA2:
			{
				SNMPInfo snmp = new SNMPInfo();
				snmp.pduSeqGetDetail(pduBuff, pduOfst, valLen, 0, sb);
			}
			break;
		case 0xA3:
			{
				SNMPInfo snmp = new SNMPInfo();
				snmp.pduSeqGetDetail(pduBuff, pduOfst, valLen, 0, sb);
			}
			break;
		case 0xA4:
			{
				SNMPInfo snmp = new SNMPInfo();
				snmp.pduSeqGetDetail(pduBuff, pduOfst, valLen, 0, sb);
			}
			break;
		default:
			StringUtil.appendHex(sb, pduBuff, pduOfst, valLen, ' ', LineBreakType.NONE);
			break;
		}
	}
}
