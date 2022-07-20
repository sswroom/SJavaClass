package org.sswr.util.net.email;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.crypto.CRC32R;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Base64Enc;

public class SMTPMessage
{
	private String fromAddr;
	private List<String> recpList;
	private List<String> headerList;
	private byte[] content;

	private int getHeaderIndex(String name)
	{
		String header;
		int i = 0;
		int j = this.headerList.size();
		while (i < j)
		{
			header = this.headerList.get(i);
			if (header.length() + 2 >= name.length() && header.startsWith(name) && header.charAt(name.length()) == ':' && header.charAt(name.length() + 1) == ' ')
			{
				return i;
			}
			i++;
		}
		return -1;
	}
		
	private boolean setHeader(String name, String val)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(':');
		sb.append(' ');
		sb.append(val);
		int i = this.getHeaderIndex(name);
		if (i == -1)
		{
			this.headerList.add(sb.toString());
		}
		else
		{
			this.headerList.set(i, sb.toString());
		}
		return true;
	}
	private boolean appendUTF8Header(StringBuilder sb, String val)
	{
		Base64Enc b64 = new Base64Enc();
		sb.append("=?UTF-8?B?");
		sb.append(b64.encodeBin(val.getBytes(StandardCharsets.UTF_8)));
		sb.append("?=");
		return true;
	}

	public SMTPMessage()
	{
		this.fromAddr = null;
		this.recpList = new ArrayList<String>();
		this.headerList = new ArrayList<String>();
		this.content = null;
	}

	public boolean setSubject(String subject)
	{
		if (StringUtil.isNonASCII(subject))
		{
			StringBuilder sb = new StringBuilder();
			this.appendUTF8Header(sb, subject);
			this.setHeader("Subject", sb.toString());
		}
		else
		{
			this.setHeader("Subject", subject);
		}
		return true;
	}

	public boolean setContent(String content, String contentType)
	{
		this.setHeader("Content-Type", contentType);
		this.content = content.getBytes(StandardCharsets.UTF_8);
		return true;
	
	}

	public boolean setSentDate(ZonedDateTime dt)
	{
		String sbuff;
		switch (dt.getDayOfWeek())
		{
		case SUNDAY:
			sbuff = "Sun, ";
			break;
		case MONDAY:
			sbuff = "Mon, ";
			break;
		case TUESDAY:
			sbuff = "Tue, ";
			break;
		case WEDNESDAY:
			sbuff = "Wed, ";
			break;
		case THURSDAY:
			sbuff = "Thu, ";
			break;
		case FRIDAY:
			sbuff = "Fri, ";
			break;
		case SATURDAY:
			sbuff = "Sat, ";
			break;
		default:
			sbuff = "";
			break;
		};
		sbuff = sbuff + DateTimeUtil.toString(dt, "dd MMM yyyy HH:mm:ss zzz");
		return this.setHeader("Date", sbuff);
	}

	public boolean setMessageId(String msgId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		sb.append(msgId);
		sb.append('>');
		return this.setHeader("Message-ID", sb.toString());
	}

	public boolean setFrom(String name, String addr)
	{
		StringBuilder sb = new StringBuilder();
		if (name != null && name.length() > 0)
		{
			if (StringUtil.isNonASCII(name))
			{
				this.appendUTF8Header(sb, name);
			}
			else
			{
				sb.append('"');
				sb.append(name);
				sb.append('"');
			}
			sb.append(' ');
		}
		sb.append('<');
		sb.append(addr);
		sb.append('>');
		this.setHeader("From", sb.toString());
		this.fromAddr = addr;
		return true;
	}

	public boolean addTo(String name, String addr)
	{
		int i = this.getHeaderIndex("To");
		StringBuilder sb = new StringBuilder();
		if (i != -1)
		{
			sb.append(this.headerList.get(i).substring(4));
			sb.append(", ");
		}
		if (name != null && name.length() > 0)
		{
			if (StringUtil.isNonASCII(name))
			{
				this.appendUTF8Header(sb, name);
			}
			else
			{
				sb.append('"');
				sb.append(name);
				sb.append('"');
			}
			sb.append(' ');
		}
		sb.append('<');
		sb.append(addr);
		sb.append('>');
		this.setHeader("To", sb.toString());
		this.recpList.add(addr);
		return true;		
	}

	public boolean addToList(String addrs)
	{
		boolean succ;
		String[] toAddrs = StringUtil.split(addrs, ",");
		succ = true;
		int i = 0;
		int j = toAddrs.length;
		while (i < j)
		{
			toAddrs[i] = toAddrs[i].trim();
			if (!StringUtil.isEmailAddress(toAddrs[i]))
			{
				succ = false;
			}
			else
			{
				succ = succ && this.addTo(null, toAddrs[i]);
			}
			i++;
		}
		return succ;
	}

	public boolean addCcList(String addrs)
	{
		boolean succ;
		String[] ccAddrs = StringUtil.split(addrs, ",");
		succ = true;
		int i = 0;
		int j = ccAddrs.length;
		while (i < j)
		{
			ccAddrs[i] = ccAddrs[i].trim();
			if (!StringUtil.isEmailAddress(ccAddrs[i]))
			{
				succ = false;
			}
			else
			{
				succ = succ && this.addCc(null, ccAddrs[i]);
			}
			i++;
		}
		return succ;
	}

	public boolean addCc(String name, String addr)
	{
		int i = this.getHeaderIndex("Cc");
		StringBuilder sb = new StringBuilder();
		if (i != -1)
		{
			String s = this.headerList.get(i);
			sb.append(s.substring(4));
			sb.append(", ");
		}
		if (name != null && name.length() > 0)
		{
			if (StringUtil.isNonASCII(name))
			{
				this.appendUTF8Header(sb, name);
			}
			else
			{
				sb.append('"');
				sb.append(name);
				sb.append('"');
			}
			sb.append(' ');
		}
		sb.append('<');
		sb.append(addr);
		sb.append('>');
		this.setHeader("Cc", sb.toString());
		this.recpList.add(addr);
		return true;
	}

	public boolean addBcc(String addr)
	{
		this.recpList.add(addr);
		return true;
	}

	public boolean completedMessage()
	{
		if (this.fromAddr == null || this.recpList.size() == 0 || this.content == null)
		{
			return false;
		}
		return true;
	}

	public String getFromAddr()
	{
		return this.fromAddr;
	}

	public List<String> getRecpList()
	{
		return this.recpList;
	}

	public boolean writeToStream(OutputStream stm)
	{
		if (!this.completedMessage())
		{
			return false;
		}
		String header;
		byte[] newLine = "\r\n".getBytes(StandardCharsets.UTF_8);
		try
		{
			int i = 0;
			int j = this.headerList.size();
			while (i < j)
			{
				header = this.headerList.get(i);
				stm.write(header.getBytes(StandardCharsets.UTF_8));
				stm.write(newLine);
				i++;
			}
			stm.write(newLine);
			stm.write(this.content);
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static boolean generateMessageID(StringBuilder sb, String mailFrom)
	{
		sb.append(StringUtil.toHex64(System.currentTimeMillis()));
		sb.append('.');
		CRC32R crc = new CRC32R();
		int i;
		i = mailFrom.indexOf('@');
		crc.calc(mailFrom.getBytes(StandardCharsets.UTF_8), 0, i);
		;
		sb.append(StringUtil.toHex32(ByteTool.readMInt32(crc.getValue(), 0)));
		sb.append(mailFrom.substring(i));
		return true;
	}
}
