package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;

public class ASN1Util
{
	public static final int IT_UNKNOWN = 0;
	public static final int IT_BOOLEAN = 0x01;
	public static final int IT_INTEGER = 0x02;
	public static final int IT_BIT_STRING = 0x03;
	public static final int IT_OCTET_STRING = 0x04;
	public static final int IT_NULL = 0x05;
	public static final int IT_OID = 0x06;
	public static final int IT_ENUMERATED = 0x0a;
	public static final int IT_UTF8STRING = 0x0c;
	public static final int IT_NUMERICSTRING = 0x12;
	public static final int IT_PRINTABLESTRING = 0x13;
	public static final int IT_T61STRING = 0x14;
	public static final int IT_VIDEOTEXSTRING = 0x15;
	public static final int IT_IA5STRING = 0x16;
	public static final int IT_UTCTIME = 0x17;
	public static final int IT_GENERALIZEDTIME = 0x18;
	public static final int IT_UNIVERSALSTRING = 0x1c;
	public static final int IT_BMPSTRING = 0x1e;
	public static final int IT_SEQUENCE = 0x30;
	public static final int IT_SET = 0x31;
	public static final int IT_CHOICE_0 = 0x80;
	public static final int IT_CHOICE_1 = 0x81;
	public static final int IT_CHOICE_2 = 0x82;
	public static final int IT_CHOICE_3 = 0x83;
	public static final int IT_CHOICE_4 = 0x84;
	public static final int IT_CHOICE_5 = 0x85;
	public static final int IT_CHOICE_6 = 0x86;
	public static final int IT_CONTEXT_SPECIFIC_0 = 0xa0;
	public static final int IT_CONTEXT_SPECIFIC_1 = 0xa1;
	public static final int IT_CONTEXT_SPECIFIC_2 = 0xa2;
	public static final int IT_CONTEXT_SPECIFIC_3 = 0xa3;

	public static int pduParseLen(byte[] pdu, int ofst, int endOfst, SharedInt len)
	{
		if (ofst >= endOfst)
		{
			len.value = 0;
			return endOfst + 1;
		}
		if ((pdu[ofst] & 0x80) != 0)
		{
			if ((pdu[ofst] & 255) == 0x81)
			{
				if (ofst + 2 > endOfst)
				{
					len.value = 0;
					return endOfst + 1;
				}
				len.value = pdu[ofst + 1] & 255;
				return ofst + 2;
			}
			else if ((pdu[ofst] & 255) == 0x82)
			{
				if (ofst + 3 > endOfst)
				{
					len.value = 0;
					return endOfst + 1;
				}
				len.value = ByteTool.readMUInt16(pdu, ofst + 1);
				return ofst + 3;
			}
			else if ((pdu[ofst] & 255) == 0x83)
			{
				if (ofst + 4 > endOfst)
				{
					len.value = 0;
					return endOfst + 1;
				}
				len.value = ByteTool.readMUInt24(pdu, ofst + 1);
				return ofst + 4;
			}
			else if (pdu[ofst] == 0x84)
			{
				if (ofst + 5 > endOfst)
				{
					len.value = 0;
					return endOfst + 1;
				}
				len.value = ByteTool.readMInt32(pdu, ofst + 1);
				return ofst + 5;
			}
			else if (pdu[ofst] == 0x80)
			{
				len.value = 0;
				return ofst + 1;
			}
			len.value = 0;
			return endOfst + 1;
		}
		else
		{
			len.value = pdu[ofst] & 255;
			return ofst + 1;
		}
	}

	public static int pduParseUInt32(byte[] pdu, int beginOfst, int endOfst, SharedInt val)
	{
		if (endOfst - beginOfst < 3)
			return 0;
		if (pdu[beginOfst] != 2)
			return 0;
		if (pdu[beginOfst + 1] == 1)
		{
			val.value = pdu[beginOfst + 2];
			return beginOfst + 3;
		}
		else if (pdu[beginOfst + 1] == 2 && endOfst - beginOfst >= 4)
		{
			val.value = ByteTool.readMUInt16(pdu, beginOfst + 2);
			return beginOfst + 4;
		}
		else if (pdu[beginOfst + 1] == 3 && endOfst - beginOfst >= 5)
		{
			val.value = ByteTool.readMUInt24(pdu, beginOfst + 2);
			return beginOfst + 5;
		}
		else if (pdu[beginOfst + 1] == 4 && endOfst - beginOfst >= 6)
		{
			val.value = ByteTool.readMInt32(pdu, beginOfst + 2);
			return beginOfst + 6;
		}
		else
		{
			return -1;
		}
	}
	
	public static ZonedDateTime pduParseUTCTimeCont(byte[] pdu, int ofst, int len)
	{
		if (len == 13 && pdu[ofst + 12] == 'Z')
		{
			ZonedDateTime dt = ZonedDateTime.now();
			return ZonedDateTime.of((dt.getYear() / 100) * 100 + str2Digit(pdu, ofst), str2Digit(pdu, ofst + 2), str2Digit(pdu, ofst + 4), str2Digit(pdu, ofst + 6), str2Digit(pdu, ofst + 8), str2Digit(pdu, ofst + 10), 0, ZoneOffset.UTC);
		}
		else if (len == 15 && pdu[ofst + 14] == 'Z')
		{
			return ZonedDateTime.of((str2Digit(pdu, ofst + 0) * 100) + str2Digit(pdu, ofst + 2), str2Digit(pdu, ofst + 4), str2Digit(pdu, ofst + 6), str2Digit(pdu, ofst + 8), str2Digit(pdu, ofst + 10), str2Digit(pdu, ofst + 12), 0, ZoneOffset.UTC);
		}
		return null;
	}

	public static boolean pduToString(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, int level)
	{
		return pduToString(pdu, beginOfst, endOfst, sb, level, null);
	}

	public static boolean pduToString(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, int level, SharedInt nextOfst)
	{
		while (beginOfst < endOfst)
		{
			byte type = pdu[beginOfst];
			SharedInt len = new SharedInt();
			SharedInt iVal = new SharedInt();
			int ofst;
	
			ofst = pduParseLen(pdu, beginOfst + 1, endOfst, len);
			if (ofst > endOfst)
			{
				return false;
			}
			else if (ofst + len.value > endOfst)
			{
				return false;
			}
	
			switch (type & 255)
			{
			case 0x1:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("BOOLEAN ");
				sb.append('(');
				booleanToString(pdu, ofst, len.value, sb);
				sb.append(')');
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x2:
				if (len.value <= 4)
				{
					beginOfst = pduParseUInt32(pdu, beginOfst, endOfst, iVal);
					if (beginOfst == -1)
					{
						return false;
					}
					StringUtil.appendChar(sb, '\t', level);
					sb.append("INTEGER ");
					sb.append(iVal.value);
					sb.append("\r\n");
				}
				else if (len.value <= 32)
				{
					StringUtil.appendChar(sb, '\t', level);
					sb.append("INTEGER ");
					StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
					sb.append("\r\n");
					beginOfst = ofst + len.value;
				}
				else
				{
					StringUtil.appendChar(sb, '\t', level);
					sb.append("INTEGER\r\n");
					StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
					sb.append("\r\n");
					beginOfst = ofst + len.value;
				}
				break;
			case 0x3:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("BIT STRING ");
				sb.append(StringUtil.toHex(pdu[ofst]));
				{
					StringBuilder innerSb = new StringBuilder();
					if (pduToString(pdu, ofst + 1, ofst + len.value, innerSb, level + 1))
					{
						sb.append(" {\r\n");
						sb.append(innerSb.toString());
						StringUtil.appendChar(sb, '\t', level);
						sb.append("}\r\n");
					}
					else
					{
						sb.append(" (");
						StringUtil.appendHex(sb, pdu, ofst + 1, len.value - 1, ' ', LineBreakType.NONE);
						sb.append(")\r\n");
					}
				}
				beginOfst = ofst + len.value;
				break;
			case 0x4:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("OCTET STRING ");
				{
					StringBuilder innerSb = new StringBuilder();
					if (pduToString(pdu, ofst, ofst + len.value, innerSb, level + 1))
					{
						sb.append("{\r\n");
						sb.append(innerSb.toString());
						StringUtil.appendChar(sb, '\t', level);
						sb.append("}\r\n");
					}
					else
					{
						sb.append("(");
						StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
						sb.append(")\r\n");
					}
				}
				beginOfst = ofst + len.value;
				break;
			case 0x5:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("NULL\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x6:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("OID ");
				oidToString(pdu, ofst, len.value, sb);
				sb.append(" (");
				//Net::ASN1OIDDB::OIDToNameString(&pdu[ofst], len, sb);
				sb.append(")\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x0a:
				if (len.value == 1)
				{
					StringUtil.appendChar(sb, '\t', level);
					sb.append("ENUMERATED ");
					sb.append(pdu[ofst] & 255);
					sb.append("\r\n");
					beginOfst = ofst + len.value;
				}
				else
				{
					return false;
				}
				break;
			case 0x0C:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("UTF8String ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x12:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("NumericString ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x13:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("PrintableString ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x14:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("T61String ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x15:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("VideotexString ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x16:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("IA5String ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x17:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("UTCTIME ");
				if (len.value == 13 && pdu[ofst + 12] == 'Z')
				{
					ZonedDateTime dt = ZonedDateTime.now();
					dt = ZonedDateTime.of((dt.getYear() / 100) * 100 + str2Digit(pdu, ofst), str2Digit(pdu, ofst + 2), str2Digit(pdu, ofst + 4), str2Digit(pdu, ofst + 6), str2Digit(pdu, ofst + 8), str2Digit(pdu, ofst + 10), 0, ZoneOffset.UTC);
					sb.append(dt.toString());
				}
				else
				{
					sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				}
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x18:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("GeneralizedTime ");
				if (len.value == 15 && pdu[ofst + 14] == 'Z')
				{
					ZonedDateTime dt = ZonedDateTime.now();
					dt = ZonedDateTime.of(str2Digit(pdu, ofst) * 100 + str2Digit(pdu, ofst + 2), str2Digit(pdu, ofst + 4), str2Digit(pdu, ofst + 6), str2Digit(pdu, ofst + 8), str2Digit(pdu, ofst + 10), str2Digit(pdu, ofst + 12), 0, ZoneOffset.UTC);
					sb.append(dt.toString());
				}
				else
				{
					sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				}
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x1C:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("UniversalString ");
				sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
				sb.append("\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x1E:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("BMPString (");
				StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
				sb.append(")\r\n");
				beginOfst = ofst + len.value;
				break;
			case 0x0:
				if (len.value == 0)
				{
					if (nextOfst != null)
					{
						nextOfst.value = beginOfst + 2;
					}
					return true;
				}
			default:
				if ((type & 255) < 0x30)
				{
					StringUtil.appendChar(sb, '\t', level);
					sb.append("UNKNOWN 0x");
					sb.append(StringUtil.toHex(type));
					sb.append(" (");
					StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
					sb.append(")\r\n");
					beginOfst = ofst + len.value;
					break;
				}
				else
				{
					StringUtil.appendChar(sb, '\t', level);
					sb.append("UNKNOWN 0x");
					sb.append(StringUtil.toHex(type));
					StringBuilder innerSb = new StringBuilder();
					if (pduToString(pdu, ofst, ofst + len.value, innerSb, level + 1))
					{
						sb.append(" {\r\n");
						sb.append(innerSb.toString());
						StringUtil.appendChar(sb, '\t', level);
						sb.append("}\r\n");
					}
					else
					{
						sb.append(" (");
						if (StringUtil.isASCIIText(pdu, ofst, len.value))
						{
							sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
						}
						else
						{
							StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
						}
						sb.append(")\r\n");
					}
					beginOfst = ofst + len.value;
					break;
				}
			case 0x30:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("SEQUENCE {\r\n");
				if (pdu[1] == 0x80)
				{
					beginOfst = ofst;
					SharedInt tmpOfst = new SharedInt(beginOfst);
					if (!pduToString(pdu, beginOfst, endOfst, sb, level + 1, tmpOfst))
					{
						return false;
					}
					beginOfst = tmpOfst.value;
				}
				else
				{
					if (!pduToString(pdu, ofst, ofst + len.value, sb, level + 1, null))
					{
						return false;
					}
					beginOfst = ofst + len.value;
				}
				StringUtil.appendChar(sb, '\t', level);
				sb.append("}\r\n");
				break;
			case 0x31:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("SET {\r\n");
				if (pdu[1] == 0x80)
				{
					beginOfst = ofst;
					SharedInt tmpOfst = new SharedInt(beginOfst);
					if (!pduToString(pdu, beginOfst, endOfst, sb, level + 1, tmpOfst))
					{
						return false;
					}
					beginOfst = tmpOfst.value;
				}
				else
				{
					beginOfst = ofst;
					if (!pduToString(pdu, beginOfst, ofst + len.value, sb, level + 1))
					{
						return false;
					}
					beginOfst += len.value;
				}
				StringUtil.appendChar(sb, '\t', level);
				sb.append("}\r\n");
				break;
			case 0x80:
			case 0x81:
			case 0x82:
			case 0x83:
			case 0x84:
			case 0x85:
			case 0x86:
			case 0x87:
			case 0x88:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("CHOICE[");
				sb.append(((type & 255) - 0x80));
				sb.append("] ");
				if (pdu[1] == 0x80)
				{
					sb.append("{\r\n");
					SharedInt tmpOfst = new SharedInt(ofst);
					if (!pduToString(pdu, ofst, endOfst, sb, level + 1, tmpOfst))
					{
						return false;
					}
					beginOfst = tmpOfst.value;
					StringUtil.appendChar(sb, '\t', level);
					sb.append("}\r\n");
				}
				else
				{
					StringBuilder innerSb = new StringBuilder();
					if (pduToString(pdu, ofst, ofst + len.value, innerSb, level + 1))
					{
						sb.append("{\r\n");
						sb.append(innerSb.toString());
						StringUtil.appendChar(sb, '\t', level);
						sb.append("}\r\n");
					}
					else
					{
						sb.append(" (");
						if (StringUtil.isASCIIText(pdu, ofst, len.value))
						{
							sb.append(new String(pdu, ofst, len.value, StandardCharsets.UTF_8));
						}
						else
						{
							StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
						}
						sb.append(")\r\n");
					}
					beginOfst = ofst + len.value;
				}
				break;
			case 0xA0:
			case 0xA1:
			case 0xA2:
			case 0xA3:
				StringUtil.appendChar(sb, '\t', level);
				sb.append("CONTEXT SPECIFIC[");
				sb.append(((type & 255) - 0xA0));
				sb.append("] ");
				if ((pdu[1] & 255) == 0x80)
				{
					sb.append("{\r\n");
					SharedInt tmpOfst = new SharedInt(ofst);
					if (!pduToString(pdu, ofst, endOfst, sb, level + 1, tmpOfst))
					{
						return false;
					}
					beginOfst = tmpOfst.value;
					StringUtil.appendChar(sb, '\t', level);
					sb.append("}\r\n");
				}
				else
				{
					StringBuilder innerSb = new StringBuilder();
					if (pduToString(pdu, ofst, ofst + len.value, innerSb, level + 1))
					{
						sb.append("{\r\n");
						sb.append(innerSb.toString());
						StringUtil.appendChar(sb, '\t', level);
						sb.append("}\r\n");
					}
					else
					{
						sb.append("(");
						StringUtil.appendHex(sb, pdu, ofst, len.value, ' ', LineBreakType.NONE);
						sb.append(")\r\n");
					}
					beginOfst = ofst + len.value;
				}
				break;
			}
		}
		return true;		
	}

	public static boolean pduDSizeEnd(byte[] pdu, int beginOfst, int endOfst, SharedInt nextOfst)
	{
		int ofst;
		SharedInt itemLen = new SharedInt();
		while (beginOfst < endOfst)
		{
			ofst = pduParseLen(pdu, beginOfst + 1, endOfst, itemLen);
			if (ofst > endOfst)
			{
				return false;
			}
	
			if (pdu[beginOfst + 0] == 0 && pdu[beginOfst + 1] == 0)
			{
				nextOfst.value = beginOfst + 2;
				return true;
			}
			else if (pdu[beginOfst + 1] == 0x80)
			{
				if (!pduDSizeEnd(pdu, ofst, endOfst, itemLen))
				{
					return false;
				}
				beginOfst = itemLen.value;
			}
			else
			{
				beginOfst = ofst + itemLen.value;
			}
		}
		nextOfst.value = beginOfst;
		return true;
	}

	public static ASN1Item pduGetItem(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		ASN1Item item;
		SharedInt itemLen = new SharedInt();
		int cnt;
		int ofst;
		Integer iCnt;
		if (path == null || path.length() == 0)
		{
			return null;
		}
		ofst = path.indexOf('.');
		if (ofst == -1)
		{
			iCnt = StringUtil.toInteger(path);
			path = null;
		}
		else if (ofst > 0 && ofst < 11)
		{
			iCnt = StringUtil.toInteger(path.substring(0, ofst));
			path = path.substring(ofst + 1);
		}
		else
		{
			return null;
		}
	
		if (iCnt == null || iCnt.intValue() == 0)
		{
			return null;
		}
		cnt = iCnt.intValue();
		while (beginOfst < endOfst)
		{
			ofst = pduParseLen(pdu, beginOfst + 1, endOfst, itemLen);
			if (ofst > endOfst)
			{
				return null;
			}
			else if (pdu[beginOfst] == 0 && pdu[beginOfst + 1] == 0)
			{
				return null;
			}
	
			cnt--;
			if (cnt == 0)
			{
				if (path == null)
				{
					item = new ASN1Item();
					if (pdu[beginOfst + 1] == 0x80)
					{
						item.pduBegin = beginOfst;
						item.ofst = ofst;
						item.itemType = pdu[beginOfst] & 255;
						if (!pduDSizeEnd(pdu, ofst, endOfst, itemLen))
						{
							return null;
						}
						item.len = itemLen.value - ofst;
						return item;
	
					}
					else
					{
						item.pduBegin = beginOfst;
						item.ofst = ofst;
						item.itemType = pdu[beginOfst] & 255;
						item.len = itemLen.value;
						return item;
					}
				}
				if (pdu[beginOfst + 1] == 0x80)
				{
					return pduGetItem(pdu, ofst, endOfst, path);
				}
				else
				{
					return pduGetItem(pdu, ofst, ofst + itemLen.value, path);
				}
			}
			else if (pdu[beginOfst + 1] == 0x80)
			{
				if (!pduDSizeEnd(pdu, ofst, endOfst, itemLen))
				{
					return null;
				}
				beginOfst = itemLen.value;
			}
			else
			{
				beginOfst = ofst + itemLen.value;
			}
		}
		return null;
	}

	public static int pduGetItemType(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		ASN1Item item = pduGetItem(pdu, beginOfst, endOfst, path);
		if (item == null)
		{
			return 0;
		}
		return item.itemType;
	}

	public static int pduCountItem(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		SharedInt len = new SharedInt();
		int size;
		int cnt;
		int ofst;
		Integer iCnt;
		if (path == null || path.length() == 0)
		{
			cnt = 0;
			while (beginOfst < endOfst)
			{
				size = (endOfst - beginOfst);
				ofst = pduParseLen(pdu, beginOfst + 1, endOfst, len);
				if (ofst > endOfst)
				{
					return 0;
				}
				else if (pdu[beginOfst] == 0 && pdu[beginOfst + 1] == 0)
				{
					return cnt;
				}
				cnt++;
				if (pdu[beginOfst + 1] == 0x80)
				{
					if (!pduDSizeEnd(pdu, ofst, endOfst, len))
					{
						return cnt;
					}
					beginOfst = len.value;
				}
				else
				{
					beginOfst = ofst + len.value;
				}
			}
			return cnt;
		}
		size = path.indexOf(".");
		if (size == -1)
		{
			iCnt = StringUtil.toInteger(path);
			path = null;
		}
		else if (size > 0 && size < 11)
		{
			iCnt = StringUtil.toInteger(path.substring(0, size));
			path = path.substring(size + 1);
		}
		else
		{
			return 0;
		}
	
		if (iCnt == null || iCnt.intValue() <= 0)
		{
			return 0;
		}
		cnt = iCnt.intValue();
	
		while (beginOfst < endOfst)
		{
			ofst = pduParseLen(pdu, beginOfst + 1, endOfst, len);
			if (ofst > endOfst)
			{
				return 0;
			}
			else if (pdu[beginOfst] == 0 && pdu[beginOfst + 1] == 0)
			{
				return 0;
			}
	
			cnt--;
			if (cnt == 0)
			{
				if (pdu[beginOfst + 1] == 0x80)
				{
					return pduCountItem(pdu, ofst, endOfst, path);
				}
				else
				{
					return pduCountItem(pdu, ofst, ofst + len.value, path);
				}
			}
			else if (pdu[beginOfst + 1] == 0x80)
			{
				if (!pduDSizeEnd(pdu, ofst, endOfst, len))
				{
					return 0;
				}
				beginOfst = len.value;
			}
			else
			{
				beginOfst = ofst + len.value;
			}
		}
		return 0;
	}

	public static boolean pduIsValid(byte[] pdu, int beginOfst, int endOfst)
	{
		SharedInt len = new SharedInt();
		int ofst;
		while (beginOfst < endOfst)
		{
			ofst = pduParseLen(pdu, beginOfst + 1, endOfst, len);
			if (ofst > endOfst)
			{
				return false;
			}
			if (pdu[beginOfst] >= 0x30 && pdu[beginOfst] < 0x40)
			{
				if (!pduIsValid(pdu, ofst, endOfst))
				{
					return false;
				}
			}
			beginOfst = ofst + len.value;
		}
		return true;
	}

	public static int oidCompare(byte[] oid1, int oid1Ofst, int oid1Len, byte[] oid2, int oid2Ofst, int oid2Len)
	{
		int i = 0;
		while (true)
		{
			if (i == oid1Len && i == oid2Len)
			{
				return 0;
			}
			else if (i >= oid1Len)
			{
				return -1;
			}
			else if (i >= oid2Len)
			{
				return 1;
			}
			else if (oid1[oid1Ofst + i] > oid2[oid2Ofst + i])
			{
				return 1;
			}
			else if (oid1[oid1Ofst + i] < oid2[oid2Ofst + i])
			{
				return -1;
			}
			i++;
		}
	}
	
	public static boolean oidStartsWith(byte[] oid1, int oid1Ofst, int oid1Len, byte[] oid2, int oid2Ofst, int oid2Len)
	{
		if (oid1Len < oid2Len)
			return false;
		int i = 0;
		while (i < oid2Len)
		{
			if (oid1[oid1Ofst + i] != oid2[oid2Ofst + i])
				return false;
			i++;
		}
		return true;
	}
	
	public static boolean oidEqualsText(byte[] oidPDU, int oidPDUOfst, int oidPDULen, String oidText)
	{
		byte oid2[] = oidText2PDU(oidText);
		return oidCompare(oidPDU, oidPDUOfst, oidPDULen, oid2, 0, oid2.length) == 0;
	}
	
	public static void oidToString(byte[] pdu, int pduOfst, int pduSize, StringBuilder sb)
	{
		int v = 0;
		int i = 1;
		sb.append((pdu[pduOfst] & 255) / 40);
		sb.append('.');
		sb.append((pdu[pduOfst] & 255) % 40);
		while (i < pduSize)
		{
			v = (v << 7) | (pdu[pduOfst + i] & 0x7f);
			if ((pdu[pduOfst + i] & 0x80) == 0)
			{
				sb.append('.');
				sb.append(v);
				v = 0;
			}
			i++;
		}
	}

	public static int oidCalcPDUSize(String oidText)
	{
		int v;
		int retSize = 1;
		String[] sarr = StringUtil.split(oidText, ".");
		int i = sarr.length;
		if (i == 1 || i == 2)
		{
			return 1;
		}
		Integer iVal;
		while (i-- > 2)
		{
			iVal = StringUtil.toInteger(sarr[i]);
			if (iVal == null)
			{
				return retSize;
			}
			v = iVal;
			while (v >= 128)
			{
				retSize++;
				v = v >> 7;
			}
			retSize++;
		}
		return retSize;
	}
	
	public static byte[] oidText2PDU(String oidText)
	{
		byte[] pduBuff = new byte[32];
		int v;
		int retSize = 1;
		String[] sarr = StringUtil.split(oidText, ".");
		int i;
		int j;
		Integer iVal;
		j = sarr.length;
		if (j == 1)
		{
			iVal = StringUtil.toInteger(sarr[0]);
			if (iVal == null)
			{
				return new byte[0];
			}
			pduBuff = new byte[1];
			pduBuff[0] = (byte)(iVal.intValue() * 40);
			return pduBuff;
		}
		if ((iVal = StringUtil.toInteger(sarr[0])) == null)
		{
			return new byte[0];
		}
		pduBuff[0] = (byte)(iVal.intValue() * 40);
		if ((iVal = StringUtil.toInteger(sarr[1])) == null)
		{
			return new byte[0];
		}
		pduBuff[0] = (byte)(pduBuff[0] + iVal.intValue());
		if (j == 2)
		{
			return Arrays.copyOf(pduBuff, 1);
		}
		i = 2;
		while (i < j)
		{
			if ((iVal = StringUtil.toInteger(sarr[i])) == null)
			{
				return new byte[0];
			}
			v = iVal.intValue();
			if (v < 128)
			{
				pduBuff[retSize] = (byte)v;
				retSize++;
			}
			else if (v < 0x4000)
			{
				pduBuff[retSize] = (byte)(0x80 | (v >> 7));
				pduBuff[retSize + 1] = (byte)(v & 0x7f);
				retSize += 2;
			}
			else if (v < 0x200000)
			{
				pduBuff[retSize] = (byte)(0x80 | (v >> 14));
				pduBuff[retSize + 1] = (byte)(0x80 | ((v >> 7) & 0x7f));
				pduBuff[retSize + 2] = (byte)(v & 0x7f);
				retSize += 3;
			}
			else if (v < 0x10000000)
			{
				pduBuff[retSize] = (byte)(0x80 | (v >> 21));
				pduBuff[retSize + 1] = (byte)(0x80 | ((v >> 14) & 0x7f));
				pduBuff[retSize + 2] = (byte)(0x80 | ((v >> 7) & 0x7f));
				pduBuff[retSize + 3] = (byte)(v & 0x7f);
				retSize += 4;
			}
			else
			{
				pduBuff[retSize] = (byte)(0x80 | (v >> 28));
				pduBuff[retSize + 1] = (byte)(0x80 | ((v >> 21) & 0x7f));
				pduBuff[retSize + 2] = (byte)(0x80 | ((v >> 14) & 0x7f));
				pduBuff[retSize + 3] = (byte)(0x80 | ((v >> 7) & 0x7f));
				pduBuff[retSize + 4] = (byte)(v & 0x7f);
				retSize += 5;
			}
			
			i++;
		}
		return Arrays.copyOf(pduBuff, retSize);
	}
	
	public static void booleanToString(byte[] data, int ofst, int dataLen, StringBuilder sb)
	{
		if (dataLen == 1)
		{
			if (data[ofst] == 0xFF)
			{
				sb.append("0xFF TRUE");
			}
			else if (data[ofst] == 0)
			{
				sb.append("0x00 FALSE");
			}
			else
			{
				sb.append("0x");
				sb.append(StringUtil.toHex(data[ofst]));
			}
		}
		else
		{
			StringUtil.appendHex(sb, data, ofst, dataLen, ' ', LineBreakType.NONE);
		}
	}
	
	public static void integerToString(byte[] data, int ofst, int dataLen, StringBuilder sb)
	{
		switch (dataLen)
		{
		case 1:
			sb.append(data[ofst] & 255);
			return;
		case 2:
			sb.append(ByteTool.readMUInt16(data, ofst));
			return;
		case 3:
			sb.append(ByteTool.readMUInt24(data, ofst));
			return;
		case 4:
			sb.append(ByteTool.readMInt32(data, ofst));
			return;
		default:
			StringUtil.appendHex(sb, data, ofst, dataLen, ' ', LineBreakType.NONE);
			return;
		}
	}
	
	public static void utcTimeToString(byte[] data, int ofst, int dataLen, StringBuilder sb)
	{
		ZonedDateTime dt = pduParseUTCTimeCont(data, ofst, dataLen);
		if (dt != null)
		{
			sb.append(DateTimeUtil.toString(dt, "yyyy-MM-dd HH:mm:ss"));
		}
	}
	
	public static int str2Digit(byte[] pdu, int ofst)
	{
		return (pdu[ofst] - 0x30) * 10 + (pdu[ofst + 1] - 0x30);
	}
}
