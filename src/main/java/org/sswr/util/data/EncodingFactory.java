package org.sswr.util.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncodingFactory
{
	public static final int MAX_SHORT_LEN = 45;

	static class EncodingInfo
	{
		public int codePage;
		public String dotNetName;
		public String desc;
		public String[] internetNames;

		EncodingInfo(int codePage, String dotNetName, String desc, String[] internetNames)
		{
			this.codePage = codePage;
			this.dotNetName = dotNetName;
			this.desc = desc;
			this.internetNames = internetNames;
		}
	}

	private Map<String, EncodingInfo> encMap;
	private static List<EncodingInfo> encInfo = List.of(
		new EncodingInfo(  037,                  "IBM037", "IBM EBCDIC US-Canada", new String[]{}),
		new EncodingInfo(  437,                  "IBM437", "OEM United States", new String[]{}),
		new EncodingInfo(  500,                  "IBM500", "IBM EBCDIC International", new String[]{}),
		new EncodingInfo(  708,                "ASMO-708", "Arabic (ASMO 708)", new String[]{}),
		new EncodingInfo(  709,                      null, "Arabic (ASMO-449+, BCON V4)", new String[]{}),
		new EncodingInfo(  710,                      null, "Arabic - Transparent Arabic", new String[]{}),
		new EncodingInfo(  720,                 "DOS-720", "Arabic (Transparent ASMO); Arabic (DOS)", new String[]{}),
		new EncodingInfo(  737,                  "ibm737", "OEM Greek (formerly 437G); Greek (DOS)", new String[]{}),
		new EncodingInfo(  775,                  "ibm775", "OEM Baltic; Baltic (DOS)", new String[]{}),
		new EncodingInfo(  850,                  "ibm850", "OEM Multilingual Latin 1; Western European (DOS)", new String[]{}),
		new EncodingInfo(  852,                  "ibm852", "OEM Latin 2; Central European (DOS)", new String[]{}),
		new EncodingInfo(  855,                  "IBM855", "OEM Cyrillic (primarily Russian)", new String[]{}),
		new EncodingInfo(  857,                  "ibm857", "OEM Turkish; Turkish (DOS)", new String[]{}),
		new EncodingInfo(  858,                "IBM00858", "OEM Multilingual Latin 1 + Euro symbol", new String[]{}),
		new EncodingInfo(  860,                  "IBM860", "OEM Portuguese; Portuguese (DOS)", new String[]{}),
		new EncodingInfo(  861,                  "ibm861", "OEM Icelandic; Icelandic (DOS)", new String[]{}),
		new EncodingInfo(  862,                 "DOS-862", "OEM Hebrew; Hebrew (DOS)", new String[]{}),
		new EncodingInfo(  863,                  "IBM863", "OEM French Canadian; French Canadian (DOS)", new String[]{}),
		new EncodingInfo(  864,                  "IBM864", "OEM Arabic; Arabic (864)", new String[]{}),
		new EncodingInfo(  865,                  "IBM865", "OEM Nordic; Nordic (DOS)", new String[]{}),
		new EncodingInfo(  866,                   "cp866", "OEM Russian; Cyrillic (DOS)", new String[]{}),
		new EncodingInfo(  869,                  "ibm869", "OEM Modern Greek; Greek, Modern (DOS)", new String[]{}),
		new EncodingInfo(  870,                  "IBM870", "IBM EBCDIC Multilingual/ROECE (Latin 2); IBM EBCDIC Multilingual Latin 2", new String[]{}),
		new EncodingInfo(  874,             "windows-874", "ANSI/OEM Thai (same as 28605, ISO 8859-15); Thai (Windows)", new String[]{}),
		new EncodingInfo(  875,                   "cp875", "IBM EBCDIC Greek Modern", new String[]{}),
		new EncodingInfo(  932,               "shift_jis", "ANSI/OEM Japanese; Japanese (Shift-JIS)",				new String[]{"Shift_JIS", "MS_Kanji", "csShiftJIS"}),
		new EncodingInfo(  936,                  "gb2312", "ANSI/OEM Simplified Chinese (PRC, Singapore); Chinese Simplified (GB2312)", new String[]{"GB2312", "csGB2312"}),
		new EncodingInfo(  949,          "ks_c_5601-1987", "ANSI/OEM Korean (Unified Hangul Code)", new String[]{}),
		new EncodingInfo(  950,                    "big5", "ANSI/OEM Traditional Chinese (Taiwan; Hong Kong SAR, PRC); Chinese Traditional (Big5)", new String[]{"Big5", "csBig5", "Big5-HKSCS"}),
		new EncodingInfo( 1026,                 "IBM1026", "IBM EBCDIC Turkish (Latin 5)", new String[]{}),
		new EncodingInfo( 1047,                "IBM01047", "IBM EBCDIC Latin 1/Open System", new String[]{}),
		new EncodingInfo( 1140,                "IBM01140", "IBM EBCDIC US-Canada (037 + Euro symbol); IBM EBCDIC (US-Canada-Euro)", new String[]{}),
		new EncodingInfo( 1141,                "IBM01141", "IBM EBCDIC Germany (20273 + Euro symbol); IBM EBCDIC (Germany-Euro)", new String[]{}),
		new EncodingInfo( 1142,                "IBM01142", "IBM EBCDIC Denmark-Norway (20277 + Euro symbol); IBM EBCDIC (Denmark-Norway-Euro)", new String[]{}),
		new EncodingInfo( 1143,                "IBM01143", "IBM EBCDIC Finland-Sweden (20278 + Euro symbol); IBM EBCDIC (Finland-Sweden-Euro)", new String[]{}),
		new EncodingInfo( 1144,                "IBM01144", "IBM EBCDIC Italy (20280 + Euro symbol); IBM EBCDIC (Italy-Euro)", new String[]{}),
		new EncodingInfo( 1145,                "IBM01145", "IBM EBCDIC Latin America-Spain (20284 + Euro symbol); IBM EBCDIC (Spain-Euro)", new String[]{}),
		new EncodingInfo( 1146,                "IBM01146", "IBM EBCDIC United Kingdom (20285 + Euro symbol); IBM EBCDIC (UK-Euro)", new String[]{}),
		new EncodingInfo( 1147,                "IBM01147", "IBM EBCDIC France (20297 + Euro symbol); IBM EBCDIC (France-Euro)", new String[]{}),
		new EncodingInfo( 1148,                "IBM01148", "IBM EBCDIC International (500 + Euro symbol); IBM EBCDIC (International-Euro)", new String[]{}),
		new EncodingInfo( 1149,                "IBM01149", "IBM EBCDIC Icelandic (20871 + Euro symbol); IBM EBCDIC (Icelandic-Euro)", new String[]{}),
		new EncodingInfo( 1200,                  "utf-16", "Unicode UTF-16, little endian byte order (BMP of ISO 10646); available only to managed applications", new String[]{"UTF-16", "UTF-16LE"}),
		new EncodingInfo( 1201,             "unicodeFFFE", "Unicode UTF-16, big endian byte order; available only to managed applications", new String[]{"UTF-16BE"}),
		new EncodingInfo( 1250,            "windows-1250", "ANSI Central European; Central European (Windows)", new String[]{}),
		new EncodingInfo( 1251,            "windows-1251", "ANSI Cyrillic; Cyrillic (Windows)", new String[]{}),
		new EncodingInfo( 1252,            "windows-1252", "ANSI Latin 1; Western European (Windows)", new String[]{}),
		new EncodingInfo( 1253,            "windows-1253", "ANSI Greek; Greek (Windows)", new String[]{}),
		new EncodingInfo( 1254,            "windows-1254", "ANSI Turkish; Turkish (Windows)", new String[]{}),
		new EncodingInfo( 1255,            "windows-1255", "ANSI Hebrew; Hebrew (Windows)", new String[]{}),
		new EncodingInfo( 1256,            "windows-1256", "ANSI Arabic; Arabic (Windows)", new String[]{}),
		new EncodingInfo( 1257,            "windows-1257", "ANSI Baltic; Baltic (Windows)", new String[]{}),
		new EncodingInfo( 1258,            "windows-1258", "ANSI/OEM Vietnamese; Vietnamese (Windows)", new String[]{}),
		new EncodingInfo( 1361,                   "Johab", "Korean (Johab)", new String[]{}),
		new EncodingInfo(10000,               "macintosh", "MAC Roman; Western European (Mac)",					new String[]{"macintosh", "csMacintosh"}),
		new EncodingInfo(10001,          "x-mac-japanese", "Japanese (Mac)", new String[]{}),
		new EncodingInfo(10002,       "x-mac-chinesetrad", "MAC Traditional Chinese (Big5); Chinese Traditional (Mac)", new String[]{}),
		new EncodingInfo(10003,            "x-mac-korean", "Korean (Mac)", new String[]{}),
		new EncodingInfo(10004,            "x-mac-arabic", "Arabic (Mac)", new String[]{}),
		new EncodingInfo(10005,            "x-mac-hebrew", "Hebrew (Mac)", new String[]{}),
		new EncodingInfo(10006,             "x-mac-greek", "Greek (Mac)", new String[]{}),
		new EncodingInfo(10007,          "x-mac-cyrillic", "Cyrillic (Mac)", new String[]{}),
		new EncodingInfo(10008,       "x-mac-chinesesimp", "MAC Simplified Chinese (GB 2312); Chinese Simplified (Mac)", new String[]{}),
		new EncodingInfo(10010,          "x-mac-romanian", "Romanian (Mac)", new String[]{}),
		new EncodingInfo(10017,         "x-mac-ukrainian", "Ukrainian (Mac)", new String[]{}),
		new EncodingInfo(10021,              "x-mac-thai", "Thai (Mac)", new String[]{}),
		new EncodingInfo(10029,                "x-mac-ce", "MAC Latin 2; Central European (Mac)", new String[]{}),
		new EncodingInfo(10079,         "x-mac-icelandic", "Icelandic (Mac)", new String[]{}),
		new EncodingInfo(10081,           "x-mac-turkish", "Turkish (Mac)", new String[]{}),
		new EncodingInfo(10082,          "x-mac-croatian", "Croatian (Mac)", new String[]{}),
		new EncodingInfo(12000,                  "utf-32", "Unicode UTF-32, little endian byte order; available only to managed applications", new String[]{"UTF-32LE", "UTF-32"}),
		new EncodingInfo(12001,                "utf-32BE", "Unicode UTF-32, big endian byte order; available only to managed applications", new String[]{"UTF-32BE"}),
		new EncodingInfo(20000,           "x-Chinese_CNS", "CNS Taiwan; Chinese Traditional (CNS)", new String[]{}),
		new EncodingInfo(20001,               "x-cp20001", "TCA Taiwan", new String[]{}),
		new EncodingInfo(20002,          "x_Chinese-Eten", "Eten Taiwan; Chinese Traditional (Eten)", new String[]{}),
		new EncodingInfo(20003,               "x-cp20003", "IBM5550 Taiwan", new String[]{}),
		new EncodingInfo(20004,               "x-cp20004", "TeleText Taiwan", new String[]{}),
		new EncodingInfo(20005,               "x-cp20005", "Wang Taiwan", new String[]{}),
		new EncodingInfo(20105,                   "x-IA5", "IA5 (IRV International Alphabet No. 5, 7-bit); Western European (IA5)", new String[]{}),
		new EncodingInfo(20106,            "x-IA5-German", "IA5 German (7-bit)", new String[]{}),
		new EncodingInfo(20107,           "x-IA5-Swedish", "IA5 Swedish (7-bit)", new String[]{}),
		new EncodingInfo(20108,         "x-IA5-Norwegian", "IA5 Norwegian (7-bit)", new String[]{}),
		new EncodingInfo(20127,                "us-ascii", "US-ASCII (7-bit)",	new String[]{"US-ASCII", "ANSI_X3.4-1968", "iso-ir-6", "ANSI_X3.4-1986", "ISO_646.irv:1991", "ASCII", "ISO646-US", "us", "IBM367", "cp367", "csASCII"}),
		new EncodingInfo(20261,               "x-cp20261", "T.61", new String[]{}),
		new EncodingInfo(20269,               "x-cp20269", "ISO 6937 Non-Spacing Accent", new String[]{}),
		new EncodingInfo(20273,                  "IBM273", "IBM EBCDIC Germany", new String[]{}),
		new EncodingInfo(20277,                  "IBM277", "IBM EBCDIC Denmark-Norway", new String[]{}),
		new EncodingInfo(20278,                  "IBM278", "IBM EBCDIC Finland-Sweden", new String[]{}),
		new EncodingInfo(20280,                  "IBM280", "IBM EBCDIC Italy", new String[]{}),
		new EncodingInfo(20284,                  "IBM284", "IBM EBCDIC Latin America-Spain", new String[]{}),
		new EncodingInfo(20285,                  "IBM285", "IBM EBCDIC United Kingdom", new String[]{}),
		new EncodingInfo(20290,                  "IBM290", "IBM EBCDIC Japanese Katakana Extended", new String[]{}),
		new EncodingInfo(20297,                  "IBM297", "IBM EBCDIC France", new String[]{}),
		new EncodingInfo(20420,                  "IBM420", "IBM EBCDIC Arabic", new String[]{}),
		new EncodingInfo(20423,                  "IBM423", "IBM EBCDIC Greek", new String[]{}),
		new EncodingInfo(20424,                  "IBM424", "IBM EBCDIC Hebrew", new String[]{}),
		new EncodingInfo(20833, "x-EBCDIC-KoreanExtended", "IBM EBCDIC Korean Extended", new String[]{}),
		new EncodingInfo(20838,                "IBM-Thai", "IBM EBCDIC Thai", new String[]{}),
		new EncodingInfo(20866,                  "koi8-r", "Russian (KOI8-R); Cyrillic (KOI8-R)", new String[]{}),
		new EncodingInfo(20871,                  "IBM871", "IBM EBCDIC Icelandic", new String[]{}),
		new EncodingInfo(20880,                  "IBM880", "IBM EBCDIC Cyrillic Russian", new String[]{}),
		new EncodingInfo(20905,                  "IBM905", "IBM EBCDIC Turkish", new String[]{}),
		new EncodingInfo(20924,                "IBM00924", "IBM EBCDIC Latin 1/Open System (1047 + Euro symbol)", new String[]{}),
		new EncodingInfo(20932,                  "EUC-JP", "Japanese (JIS 0208-1990 and 0212-1990)",				new String[]{"EUC-JP", "Extended_UNIX_Code_Packed_Format_for_Japanese", "csEUCPkdFmtJapanese"}),
		new EncodingInfo(20936,               "x-cp20936", "Simplified Chinese (GB2312); Chinese Simplified (GB2312-80)", new String[]{}),
		new EncodingInfo(20949,               "x-cp20949", "Korean Wansung", new String[]{}),
		new EncodingInfo(21025,                  "cp1025", "IBM EBCDIC Cyrillic Serbian-Bulgarian", new String[]{}),
		new EncodingInfo(21027,                      null, "(deprecated)", new String[]{}),
		new EncodingInfo(21866,                  "koi8-u", "Ukrainian (KOI8-U); Cyrillic (KOI8-U)", new String[]{}),
		new EncodingInfo(28591,              "iso-8859-1", "ISO 8859-1 Latin 1; Western European (ISO)",            new String[]{"ISO-8859-1", "ISO_8859-1:1987", "iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "CP819", "csISOLatin1"}),
		new EncodingInfo(28592,              "iso-8859-2", "ISO 8859-2 Central European; Central European (ISO)",   new String[]{"ISO-8859-2", "ISO_8859-2:1987", "iso-ir-101", "ISO_8859-2", "latin2", "l2", "csISOLatin2"}),
		new EncodingInfo(28593,              "iso-8859-3", "ISO 8859-3 Latin 3",                                    new String[]{"ISO-8859-3", "ISO_8859-3:1988", "iso-ir-109", "ISO_8859-3", "latin3", "l3", "csISOLatin3"}),
		new EncodingInfo(28594,              "iso-8859-4", "ISO 8859-4 Baltic",                                     new String[]{"ISO-8859-4", "ISO_8859-4:1988", "iso-ir-110", "ISO_8859-4", "latin4", "l4", "csISOLatin4"}),
		new EncodingInfo(28595,              "iso-8859-5", "ISO 8859-5 Cyrillic",                                   new String[]{"ISO-8859-5", "ISO_8859-5:1988", "iso-ir-144", "ISO_8859-5", "cyrillic", "csISOLatinCyrillic"}),
		new EncodingInfo(28596,              "iso-8859-6", "ISO 8859-6 Arabic",                                     new String[]{"ISO-8859-6", "ISO_8859-6:1987", "iso-ir-127", "ISO_8859-6", "ECMA-114", "ASMO-708", "arabic", "csISOLatinArabic"}),
		new EncodingInfo(28597,              "iso-8859-7", "ISO 8859-7 Greek",                                      new String[]{"ISO-8859-7", "ISO_8859-7:1987", "iso-ir-126", "ISO_8859-7", "ELOT_928", "ECMA-118", "greek", "greek8", "csISOLatinGreek"}),
		new EncodingInfo(28598,              "iso-8859-8", "ISO 8859-8 Hebrew; Hebrew (ISO-Visual)",				new String[]{"ISO-8859-8", "ISO_8859-8:1988", "iso-ir-138", "ISO_8859-8", "hebrew", "csISOLatinHebrew"}),
		new EncodingInfo(28599,              "iso-8859-9", "ISO 8859-9 Turkish",									new String[]{"ISO-8859-9", "ISO_8859-9:1989", "iso-ir-148", "ISO_8859-9", "latin5", "l5", "csISOLatin5"}),
		new EncodingInfo(28603,             "iso-8859-13", "ISO 8859-13 Estonian", new String[]{}),
		new EncodingInfo(28605,             "iso-8859-15", "ISO 8859-15 Latin 9", new String[]{}),
		new EncodingInfo(29001,                "x-Europa", "Europa 3", new String[]{}),
		new EncodingInfo(38598,            "iso-8859-8-i", "ISO 8859-8 Hebrew; Hebrew (ISO-Logical)", new String[]{}),
		new EncodingInfo(50220,             "iso-2022-jp", "ISO 2022 Japanese with no halfwidth Katakana; Japanese (JIS)", new String[]{"JIS_Encoding", "csJISEncoding"}),
		new EncodingInfo(50221,             "csISO2022JP", "ISO 2022 Japanese with halfwidth Katakana; Japanese (JIS-Allow 1 byte Kana)", new String[]{}),
		new EncodingInfo(50222,             "iso-2022-jp", "ISO 2022 Japanese JIS X 0201-1989; Japanese (JIS-Allow 1 byte Kana - SO/SI)", new String[]{"ISO-2022-JP", "csISO2022JP"}),
		new EncodingInfo(50225,             "iso-2022-kr", "ISO 2022 Korean",										new String[]{"ISO-2022-KR", "csISO2022KR"}),
		new EncodingInfo(50227,               "x-cp50227", "ISO 2022 Simplified Chinese; Chinese Simplified (ISO 2022)", new String[]{}),
		new EncodingInfo(50229,                      null, "ISO 2022 Traditional Chinese", new String[]{}),
		new EncodingInfo(50930,                      null, "EBCDIC Japanese (Katakana) Extended", new String[]{}),
		new EncodingInfo(50931,                      null, "EBCDIC US-Canada and Japanese", new String[]{}),
		new EncodingInfo(50933,                      null, "EBCDIC Korean Extended and Korean", new String[]{}),
		new EncodingInfo(50935,                      null, "EBCDIC Simplified Chinese Extended and Simplified Chinese", new String[]{}),
		new EncodingInfo(50936,                      null, "EBCDIC Simplified Chinese", new String[]{}),
		new EncodingInfo(50937,                      null, "EBCDIC US-Canada and Traditional Chinese", new String[]{}),
		new EncodingInfo(50939,                      null, "EBCDIC Japanese (Latin) Extended and Japanese", new String[]{}),
		new EncodingInfo(51932,	      	           null, "EUC Japanese", new String[]{}),
		new EncodingInfo(51936,                  "EUC-CN", "EUC Simplified Chinese; Chinese Simplified (EUC)", new String[]{}),
		new EncodingInfo(51949,                  "euc-kr", "EUC Korean",											new String[]{"EUC-KR", "csEUCKR"}),
		new EncodingInfo(51950,                      null, "EUC Traditional Chinese", new String[]{}),
		new EncodingInfo(52936,              "hz-gb-2312", "HZ-GB2312 Simplified Chinese; Chinese Simplified (HZ)", new String[]{}),
		new EncodingInfo(54936,                 "GB18030", "GB18030 Simplified Chinese (4 byte); Chinese Simplified (GB18030)", new String[]{}),
		new EncodingInfo(57002,              "x-iscii-de", "ISCII Devanagari", new String[]{}),
		new EncodingInfo(57003,              "x-iscii-be", "ISCII Bengali", new String[]{}),
		new EncodingInfo(57004,              "x-iscii-ta", "ISCII Tamil", new String[]{}),
		new EncodingInfo(57005,              "x-iscii-te", "ISCII Telugu", new String[]{}),
		new EncodingInfo(57006,              "x-iscii-as", "ISCII Assamese", new String[]{}),
		new EncodingInfo(57007,              "x-iscii-or", "ISCII Oriya", new String[]{}),
		new EncodingInfo(57008,              "x-iscii-ka", "ISCII Kannada", new String[]{}),
		new EncodingInfo(57009,              "x-iscii-ma", "ISCII Malayalam", new String[]{}),
		new EncodingInfo(57010,              "x-iscii-gu", "ISCII Gujarati", new String[]{}),
		new EncodingInfo(57011,              "x-iscii-pa", "ISCII Punjabi", new String[]{}),
		new EncodingInfo(65000,                   "utf-7", "Unicode (UTF-7)", new String[]{"UTF-7"}),
		new EncodingInfo(65001,                   "utf-8", "Unicode (UTF-8)",new String[]{"UTF-8"})
		);

	public EncodingFactory()
	{
		this.encMap = new HashMap<String, EncodingInfo>();
		EncodingInfo enc;
		int i = encInfo.size();
		int j;
		while (i-- > 0)
		{
			enc = encInfo.get(i);
			j = enc.internetNames.length;
			while (j-- > 0)
			{
				this.encMap.put(enc.internetNames[j].toLowerCase(), enc);
			}
		}
	}

	public int getCodePage(String shortName)
	{
		if (shortName.length() > MAX_SHORT_LEN)
		{
			return 0;
		}
		EncodingInfo enc = this.encMap.get(shortName.toLowerCase());
		if (enc != null)
		{
			return enc.codePage;
		}
		else
		{
			return 0;
		}
	}

	public static String getName(int codePage)
	{
		int i = 0;
		int j = encInfo.size() - 1;
		int k;
		int l;
		while (i <= j)
		{
			k = (i + j) >> 1;
			l = encInfo.get(k).codePage;
			if (codePage > l)
			{
				i = k + 1;
			}
			else if (codePage < l)
			{
				j = k - 1;
			}
			else
			{
				return encInfo.get(k).desc;
			}
		}
		return "Unknown";
	}

	public static String getInternetName(int codePage)
	{
		int i = 0;
		int j = encInfo.size() - 1;
		int k;
		int l;
		while (i <= j)
		{
			k = (i + j) >> 1;
			l = encInfo.get(k).codePage;
			if (codePage > l)
			{
				i = k + 1;
			}
			else if (codePage < l)
			{
				j = k - 1;
			}
			else
			{
				if (encInfo.get(k).internetNames.length > 0)
					return encInfo.get(k).internetNames[0];
				break;
			}
		}
		return "UTF-8";
	}

	public static String getDotNetName(int codePage)
	{
		int i = 0;
		int j = encInfo.size() - 1;
		int k;
		int l;
		while (i <= j)
		{
			k = (i + j) >> 1;
			l = encInfo.get(k).codePage;
			if (codePage > l)
			{
				i = k + 1;
			}
			else if (codePage < l)
			{
				j = k - 1;
			}
			else
			{
				if (encInfo.get(k).dotNetName != null)
					return encInfo.get(k).dotNetName;
				break;
			}
		}
		return "UTF-8";
	}

	public static int getSystemCodePage()
	{
		return 65001;
	}

	public static int getSystemLCID()
	{
		return 0x0409;
	}

	public static void getCodePages(List<Integer> codePages)
	{
		codePages.add(1200);
		codePages.add(1201);
		codePages.add(65001);
	}
}
