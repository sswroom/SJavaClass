package org.sswr.util.net;

import java.util.ArrayList;

import org.sswr.util.io.OSType;

import jakarta.servlet.http.HttpServletRequest;

public class BrowserInfo
{
	public enum BrowserType
	{
		Unknown,
		IE,
		Firefox,
		Chrome,
		Safari,
		UCBrowser,
		CFNetwork,
		SogouWeb,
		Baidu,
		Semrush,
		Dalvik,
		Indy,
		GoogleBots,
		AndroidWV,
		Samsung,
		WestWind,
		Yandex,
		Bing,
		Masscan,
		PyRequests,
		Zgrab,
		Edge,
		PyURLLib,
		GoogleBotD,
		DotNet,
		WinDiag,
		SSWR,
		SmartTV,
		BlexBot,
		SogouPic,
		Nutch,
		Yisou,
		Wget,
		Scrapy,
		GoHTTP,
		WinHTTP,
		NLPProject,
		ApacheHTTP,
		BannerDet,
		NetcraftWeb,
		NetcraftAG,
		AhrefsBot,
		Mj12Bot,
		NetSysRes,
		Whatsapp,
		Curl,
		GSA,
		Facebook,
		Netseen,
		MSNBot,
		LibwwwPerl,
		HuaweiBrowser,
		Opera,
		MiBrowser
	}

	public static class UserAgentInfo
	{
		public OSType os;
		public String osVer;
		public BrowserType browser;
		public String browserVer;
		public String devName;
	}

	public static UserAgentInfo parseUserAgent(String userAgent)
	{
		UserAgentInfo ent = new UserAgentInfo();
		ent.os = OSType.Unknown;
		ent.browser = BrowserType.Unknown;
		int i;
		if (userAgent == "Microsoft Windows Network Diagnostics")
		{
			ent.browser = BrowserType.WinDiag;
			return ent;
		}
		else if (userAgent.startsWith("West Wind Internet Protocols "))
		{
			ent.browser = BrowserType.WestWind;
			ent.browserVer = userAgent.substring(29);
			return ent;
		}
		else if (userAgent.startsWith("Sogou web spider/"))
		{
			ent.browser = BrowserType.SogouWeb;
			i = userAgent.indexOf('(');
			if (i >= 0)
			{
				ent.browserVer = userAgent.substring(17, i);
			}
			else
			{
				ent.browserVer = userAgent.substring(17);
			}
			return ent;
		}
		else if (userAgent.startsWith("Sogou Pic Spider/"))
		{
			ent.browser = BrowserType.SogouPic;
			i = userAgent.indexOf('(');
			if (i >= 0)
			{
				ent.browserVer = userAgent.substring(17, i);
			}
			else
			{
				ent.browserVer = userAgent.substring(17);
			}
			return ent;
		}
		else if (userAgent == "Nutch Master Test/Dolphin-0.1-Beta")
		{
			ent.browser = BrowserType.Nutch;
			ent.browserVer = "0.1";
			return ent;
		}
		else if (userAgent == "YisouSpider")
		{
			ent.browser = BrowserType.Yisou;
			return ent;
		}
		else if (userAgent.startsWith("HTTP Banner Detection"))
		{
			ent.browser = BrowserType.BannerDet;
			return ent;
		}
		else if (userAgent.startsWith("NetSystemsResearch "))
		{
			ent.browser = BrowserType.NetSysRes;
			return ent;
		}
		if (userAgent.startsWith("\"") && userAgent.endsWith("\""))
		{
			userAgent.substring(1, userAgent.length() - 1);
		}
		userAgent = userAgent.trim();
		if (userAgent == "nlpproject.info research")
		{
			ent.browser = BrowserType.NLPProject;
			return ent;
		}
	
		ArrayList<String> strs = new ArrayList<String>();
		boolean bst = false;
		int j;
		char c;
		j = 0;
		i = 0;
		while (i < userAgent.length())
		{
			c = userAgent.charAt(i);
			if (c == ' ' && !bst)
			{
				strs.add(userAgent.substring(j, i));
				j = i + 1;
			}
			else if (c == ')' && bst)
			{
				bst = false;
			}
			else if (c == '(')
			{
				bst = true;
			}
			i++;
		}
		if (j < userAgent.length())
		{
			strs.add(userAgent.substring(j));
		}
		i = 0;
		while (i < strs.size())
		{
			if (strs.get(i).charAt(0) == '(')
			{
				if (strs.get(i).endsWith(")"))
				{
					strs.set(i, strs.get(i).substring(0, strs.get(i).length() - 1));
				}
				String[] strs2 = strs.get(i).substring(1).split(";");
				j = 0;
				boolean lastIsAndroid = false;
				while (j < strs2.length)
				{
					strs2[j] = strs2[j].trim();
					if (strs2[j].startsWith("MSIE "))
					{
						ent.browser = BrowserType.IE;
						ent.browserVer = strs2[j].substring(5);
					}
					else if (strs2[j].startsWith("Windows NT "))
					{
						ent.os = OSType.WindowsNT;
						ent.osVer = strs2[j].substring(11);
					}
					else if (strs2[j] == "WOW64")
					{
						if (ent.os == OSType.WindowsNT)
						{
							ent.os = OSType.WindowsNT64;
						}
					}
					else if (strs2[j] == "Win64")
					{
						if (ent.os == OSType.WindowsNT)
						{
							ent.os = OSType.WindowsNT64;
						}
					}
					else if (strs2[j] == "iPad")
					{
						ent.os = OSType.iPad;
					}
					else if (ent.os == OSType.Unknown && strs2[j] == "Linux i686")
					{
						ent.os = OSType.Linux_i686;
					}
					else if (ent.os == OSType.Unknown && strs2[j] == "Linux x86_64")
					{
						ent.os = OSType.Linux_X86_64;
					}
					else if (strs2[j] == "Android")
					{
						ent.os = OSType.Android;
					}
					else if (strs2[j] == "wv")
					{
						if (ent.os == OSType.Android)
						{
							ent.browser = BrowserType.AndroidWV;
						}
					}
					else if (strs2[j].startsWith("Android "))
					{
						ent.os = OSType.Android;
						ent.osVer = strs2[j].substring(8);
						lastIsAndroid = true;
					}
					else if (strs2[j].startsWith("CrOS "))
					{
						ent.os = OSType.ChromeOS;
						int k = strs2[j].indexOf(' ', 5);
						ent.osVer = strs2[j].substring(k + 1);
					}
					else if (ent.os == OSType.iPad && strs2[j] == "U")
					{
						ent.os = OSType.Android;
					}
					else if (strs2[j] == "iPhone")
					{
						ent.os = OSType.iPhone;
					}
					else if (strs2[j] == "Macintosh")
					{
						ent.os = OSType.MacOS;
					}
					else if ((ent.os == OSType.iPad || ent.os == OSType.iPhone) && strs2[j].startsWith("CPU OS "))
					{
						int k = strs2[j].indexOf(' ', 7);
						if (k >= 0)
						{
							strs2[j] = strs2[j].substring(0, k);
						}
						strs2[j] = strs2[j].replaceAll("_", ".");
						ent.osVer = strs2[j].substring(7);
					}
					else if (ent.os == OSType.iPhone && strs2[j].startsWith("CPU iPhone OS "))
					{
						int k = strs2[j].indexOf(' ', 14);
						if (k >= 0)
						{
							strs2[j].substring(0, k);
						}
						k = strs2[j].indexOf(" like Mac OS");
						if (k >= 0)
						{
							strs2[j] = strs2[j].substring(0, k);
						}
						strs2[j] = strs2[j].replaceAll("_", ".");
						ent.osVer = strs2[j].substring(14);
					}
					else if (ent.os == OSType.MacOS && strs2[j].startsWith("Intel Mac OS X "))
					{
						int k = strs2[j].indexOf(' ', 15);
						if (k >= 0)
						{
							strs2[j].substring(0, k);
						}
						strs2[j] = strs2[j].replaceAll("_", ".");
						ent.osVer = strs2[j].substring(15);
					}
					else if (strs2[j] == "Trident/7.0")
					{
						ent.browser = BrowserType.IE;
						ent.browserVer = "11.0";
					}
					else if (strs2[j] == "JuziBrowser") //JuziBrowser
					{
					}
					else if (strs2[j] == "SE 2.X MetaSr 1.0") //Sugou Browser
					{
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j] == "Indy Library")
					{
						ent.browser = BrowserType.Indy;
					}
					else if (strs2[j].startsWith("Googlebot/"))
					{
						if (ent.os == OSType.Android)
						{
							ent.browser = BrowserType.GoogleBots;
							ent.browserVer = strs2[j].substring(10);
						}
						else if (ent.os == OSType.Unknown)
						{
							ent.browser = BrowserType.GoogleBotD;
							ent.browserVer = strs2[j].substring(10);
						}
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("Baiduspider-render/"))
					{
						ent.browser = BrowserType.Baidu;
						ent.browserVer = strs2[j].substring(19);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("SemrushBot/"))
					{
						ent.browser = BrowserType.Semrush;
						ent.browserVer = strs2[j].substring(11);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("YandexBot/"))
					{
						ent.browser = BrowserType.Yandex;
						ent.browserVer = strs2[j].substring(10);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("BLEXBot/"))
					{
						ent.browser = BrowserType.BlexBot;
						ent.browserVer = strs2[j].substring(10);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("bingbot/"))
					{
						ent.browser = BrowserType.Bing;
						ent.browserVer = strs2[j].substring(8);
					}
					else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.IE) && strs2[j].startsWith("MS Web Services Client Protocol "))
					{
						ent.browser = BrowserType.DotNet;
						ent.browserVer = strs2[j].substring(32);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("WinHttp.WinHttpRequest."))
					{
						ent.browser = BrowserType.WinHTTP;
						ent.browserVer = strs2[j].substring(23);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("NetcraftSurveyAgent/"))
					{
						ent.browser = BrowserType.NetcraftAG;
						ent.browserVer = strs2[j].substring(20);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("AhrefsBot/"))
					{
						ent.browser = BrowserType.AhrefsBot;
						ent.browserVer = strs2[j].substring(10);
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j].startsWith("MJ12bot/"))
					{
						ent.browser = BrowserType.Mj12Bot;
						if (strs2[j].charAt(8) == 'v')
						{
							ent.browserVer = strs2[j].substring(9);
						}
						else
						{
							ent.browserVer = strs2[j].substring(8);
						}
					}
					else if (ent.browser == BrowserType.Unknown && strs2[j] == "Netcraft Web Server Survey")
					{
						ent.browser = BrowserType.NetcraftWeb;
					}
					else if (strs2[j] == "Mobile")
					{
					}
					else if (strs2[j] == "rv:")
					{
					}
					else if (lastIsAndroid)
					{
						int k = strs2[j].indexOf(" Build/");
						if (k >= 0)
						{
							strs2[j] = strs2[j].substring(0, k);
						}
	
						k = strs2[j].indexOf(" MIUI/");
						if (k >= 0)
						{
							strs2[j] = strs2[j].substring(0, k);
						}
						if (strs2[j].startsWith("SAMSUNG SM-"))
						{
							ent.devName = strs2[j].substring(8);
						}
						else if (strs2[j].startsWith("HUAWEI "))
						{
							ent.devName = strs2[j].substring(7);
						}
						else if (strs2[j] == "K")
						{
						}
						else
						{
							ent.devName = strs2[j];
						}
						lastIsAndroid = false;
					}
					j++;
				}
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Firefox/"))
			{
				ent.browser = BrowserType.Firefox;
				ent.browserVer = strs.get(i).substring(8);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Safari) && strs.get(i).startsWith("SamsungBrowser/"))
			{
				ent.browser = BrowserType.Samsung;
				ent.browserVer = strs.get(i).substring(15);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Safari) && strs.get(i).startsWith("GSA/"))
			{
				ent.browser = BrowserType.GSA;
				ent.browserVer = strs.get(i).substring(4);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Safari) && strs.get(i).startsWith("CriOS/"))
			{
				ent.browser = BrowserType.Chrome;
				ent.browserVer = strs.get(i).substring(6);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Safari) && strs.get(i).startsWith("FxiOS/"))
			{
				ent.browser = BrowserType.Firefox;
				ent.browserVer = strs.get(i).substring(6);
			}
			else if (strs.get(i).startsWith("Chrome/"))
			{
				if (ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Safari)
				{
					ent.browser = BrowserType.Chrome;
					ent.browserVer = strs.get(i).substring(7);
				}
				else if (ent.browser == BrowserType.AndroidWV)
				{
					ent.browserVer = strs.get(i).substring(7);
				}
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("Edge/"))
			{
				ent.browser = BrowserType.Edge;
				ent.browserVer = strs.get(i).substring(5);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("Edg/"))
			{
				ent.browser = BrowserType.Edge;
				ent.browserVer = strs.get(i).substring(4);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("EdgiOS/"))
			{
				ent.browser = BrowserType.Edge;
				ent.browserVer = strs.get(i).substring(7);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("OPT/"))
			{
				ent.browser = BrowserType.Opera;
				ent.browserVer = strs.get(i).substring(4);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("OPR/"))
			{
				ent.browser = BrowserType.Opera;
				ent.browserVer = strs.get(i).substring(4);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("HuaweiBrowser/"))
			{
				ent.browser = BrowserType.HuaweiBrowser;
				ent.browserVer = strs.get(i).substring(14);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("XiaoMi/MiuiBrowser/"))
			{
				ent.browser = BrowserType.MiBrowser;
				ent.browserVer = strs.get(i).substring(19);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Safari/"))
			{
				ent.browser = BrowserType.Safari;
				ent.browserVer = strs.get(i).substring(7);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("UCBrowser/"))
			{
				ent.browser = BrowserType.UCBrowser;
				ent.browserVer = strs.get(i).substring(10);
			}
			else if ((ent.browser == BrowserType.Unknown || ent.browser == BrowserType.Chrome) && strs.get(i).startsWith("baidu.sogo.uc.UCBrowser/"))
			{
				ent.browser = BrowserType.UCBrowser;
				ent.browserVer = strs.get(i).substring(24);
			}
			else if (strs.get(i).startsWith("UBrowser/"))
			{
			}
			else if (strs.get(i).startsWith("baiduboxapp/"))
			{
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Dalvik/"))
			{
				ent.browser = BrowserType.Dalvik;
				ent.browserVer = strs.get(i).substring(7);
			}
			else if (ent.os == OSType.Unknown && strs.get(i).startsWith("Darwin/"))
			{
				ent.os = OSType.Darwin;
				ent.osVer = strs.get(i).substring(7);
			}
			else if (ent.os == OSType.Unknown && strs.get(i).startsWith("SmartTV/"))
			{
				ent.os = OSType.Netcast;
				ent.osVer = strs.get(i).substring(8);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("CFNetwork/"))
			{
				ent.browser = BrowserType.CFNetwork;
				ent.browserVer = strs.get(i).substring(10);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Version/"))
			{
				ent.browser = BrowserType.Safari;
				ent.browserVer = strs.get(i).substring(8);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("masscan/"))
			{
				ent.browser = BrowserType.Masscan;
				ent.browserVer = strs.get(i).substring(8);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("zgrab/"))
			{
				ent.browser = BrowserType.Zgrab;
				ent.browserVer = strs.get(i).substring(6);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("python-requests/"))
			{
				ent.browser = BrowserType.PyRequests;
				ent.browserVer = strs.get(i).substring(16);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Python-urllib/"))
			{
				ent.browser = BrowserType.PyURLLib;
				ent.browserVer = strs.get(i).substring(14);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Wget/"))
			{
				ent.browser = BrowserType.Wget;
				ent.browserVer = strs.get(i).substring(5);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Scrapy/"))
			{
				ent.browser = BrowserType.Scrapy;
				ent.browserVer = strs.get(i).substring(7);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Go-http-client/"))
			{
				ent.browser = BrowserType.GoHTTP;
				ent.browserVer = strs.get(i).substring(15);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("Apache-HttpClient/"))
			{
				ent.browser = BrowserType.ApacheHTTP;
				ent.browserVer = strs.get(i).substring(18);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("WhatsApp/"))
			{
				ent.browser = BrowserType.Whatsapp;
				ent.browserVer = strs.get(i).substring(9);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("curl/"))
			{
				ent.browser = BrowserType.Curl;
				ent.browserVer = strs.get(i).substring(5);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("sswr/"))
			{
				ent.browser = BrowserType.SSWR;
				ent.browserVer = strs.get(i).substring(5);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("facebookexternalhit/"))
			{
				ent.browser = BrowserType.Facebook;
				ent.browserVer = strs.get(i).substring(20);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("NetSeen/"))
			{
				ent.browser = BrowserType.Netseen;
				ent.browserVer = strs.get(i).substring(8);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("msnbot/"))
			{
				ent.browser = BrowserType.MSNBot;
				ent.browserVer = strs.get(i).substring(7);
			}
			else if (ent.browser == BrowserType.Unknown && strs.get(i).startsWith("libwww-perl/"))
			{
				ent.browser = BrowserType.LibwwwPerl;
				ent.browserVer = strs.get(i).substring(12);
			}
			else if (strs.get(i) == "LBBROWSER")
			{
			}
			i++;
		}
		return ent;
	}

	public static UserAgentInfo parseReq(HttpServletRequest req)
	{
		String ua = req.getHeader("User-Agent");
		if (ua != null)
		{
			return parseUserAgent(ua);
		}
		UserAgentInfo uai = new UserAgentInfo();
		uai.os = OSType.Unknown;
		uai.browser = BrowserType.Unknown;
		return uai;
	}

	public static String getBrowserTypeName(BrowserType browser)
	{
		switch (browser)
		{
		case IE:
			return "IE";
		case Firefox:
			return "Firefox";
		case Chrome:
			return "Chrome";
		case Safari:
			return "Safari";
		case UCBrowser:
			return "UC Browser";
		case CFNetwork:
			return "CFNetwork";
		case SogouWeb:
			return "Sogou Web Spider";
		case Baidu:
			return "Baidu Spider";
		case Semrush:
			return "Semrush Bot";
		case Dalvik:
			return "Dalvik";
		case Indy:
			return "Indy Library";
		case GoogleBots:
			return "GoogleBot (Smartphone)";
		case AndroidWV:
			return "Android WebView";
		case Samsung:
			return "Samsung Browser";
		case WestWind:
			return "West Wind Internet Protocols";
		case Yandex:
			return "Yandex Bot";
		case Bing:
			return "Bing Bot";
		case Masscan:
			return "masscan";
		case PyRequests:
			return "Python Requests";
		case Zgrab:
			return "zgrab";
		case Edge:
			return "Edge";
		case PyURLLib:
			return "Python urllib";
		case GoogleBotD:
			return "GoogleBot (Desktop)";
		case DotNet:
			return ".NET Web Client";
		case WinDiag:
			return "Microsoft Windows Network Diagnostics";
		case SSWR:
			return "SSWR";
		case SmartTV:
			return "SmartTV";
		case BlexBot:
			return "BLEXBot";
		case SogouPic:
			return "Sogou Pic Spider";
		case Nutch:
			return "Apache Nutch";
		case Yisou:
			return "Yisou Spider";
		case Wget:
			return "Wget";
		case Scrapy:
			return "Scrapy";
		case GoHTTP:
			return "Golang HTTP Client";
		case WinHTTP:
			return "WinHTTP Client";
		case NLPProject:
			return "NLPProject";
		case ApacheHTTP:
			return "Apache HTTP Client";
		case BannerDet:
			return "HTTP Banner Detection";
		case NetcraftWeb:
			return "Netcraft Web Server Survey";
		case NetcraftAG:
			return "Netcraft Survey Agent";
		case AhrefsBot:
			return "Ahrefs Bot";
		case Mj12Bot:
			return "MJ12Bot";
		case NetSysRes:
			return "NetSystemsResearch";
		case Whatsapp:
			return "WhatsApp";
		case Curl:
			return "Curl";
		case GSA:
			return "Google Search App";
		case Facebook:
			return "Facebook External Hit";
		case Netseen:
			return "NetSeen";
		case MSNBot:
			return "MSNBOT";
		case LibwwwPerl:
			return "libwww-perl";
		case HuaweiBrowser:
			return "HuaweiBrowser";
		case Opera:
			return "Opera";
		case MiBrowser:
			return "MiBrowser";
		case Unknown:
		default:
			return "Unknown";
		}
	}
}
