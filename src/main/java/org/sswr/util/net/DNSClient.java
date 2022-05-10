package org.sswr.util.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;

public class DNSClient implements UDPPacketListener
{
	class RequestStatus
	{
		public byte[] respBuff;
		public int respSize;
		public ThreadEvent finEvt;

		public RequestStatus()
		{
			this.respBuff = new byte[512];
			this.finEvt = new ThreadEvent();
		}
	};

	private UDPServer svr;
	private int lastID;
	private InetAddress serverAddr;
	private HashMap<Integer, RequestStatus> reqMap;

	@Override
	public void udpPacketReceived(InetAddress addr, int port, byte[] buff, int ofst, int length) {
		RequestStatus req;
		synchronized(this.reqMap)
		{
			req = this.reqMap.get(ByteTool.readMUInt16(buff, ofst));
			if (req != null)
			{
				ByteTool.copyArray(req.respBuff, 0, buff, ofst, length);
				req.respSize = length;
				req.finEvt.set();
			}
		}
	}

	private RequestStatus newReq(int id)
	{
		RequestStatus req = new RequestStatus();
		req.respSize = 0;
		synchronized (this.reqMap)
		{
			this.reqMap.put(id, req);
		}
		return req;
	
	}

	private void delReq(int id)
	{
		synchronized (this.reqMap)
		{
			this.reqMap.remove(id);			
		}
	}

	private int nextId()
	{
		this.lastID++;
		if (this.lastID >= 65536)
			this.lastID = 1;
		return this.lastID;
	}

	public DNSClient(InetAddress serverAddr)
	{
		Random random = new Random();
		this.serverAddr = serverAddr;
		this.lastID = random.nextInt() & 0xffff;
		this.reqMap = new HashMap<Integer, RequestStatus>();
		this.svr = new UDPServer(null, 0, null, this, null, null, 1, false);
	}

	public void close()
	{
		this.svr.close();
	}

	public int getByEmailDomainName(List<DNSRequestAnswer> answers, String domain)
	{
		return this.getByType(answers, domain, 15);
	}
	
	public int getByDomainName(List<DNSRequestAnswer> answers, String domain)
	{
		return this.getByType(answers, domain, 1);
	}

	public int getByType(List<DNSRequestAnswer> answers, String domain, int reqType)
	{
		int ret = 0;
		byte buff[] = new byte[512];
		byte sbuff[] = new byte[256];
		int ptr1;
		int ptr2;
		byte[] cbuff;
		int cptr1;
		int cptr2;
		byte c;
		int currId = this.nextId();
	
		ByteTool.writeMInt16(buff, 0, currId);
		ByteTool.writeMInt16(buff, 2, 0x100); //flags
		ByteTool.writeMInt16(buff, 4, 1); //reqCount
		ByteTool.writeMInt16(buff, 6, 0); //respCount;
		ByteTool.writeMInt16(buff, 8, 0); //authorCount
		ByteTool.writeMInt16(buff, 10, 0); //extraCount
		ptr1 = 12;
		if (reqType == 12)
		{
			InetAddress addr;
			try
			{
				addr = InetAddress.getByName(domain);
				if (addr instanceof Inet4Address)
				{
					byte[] ip4 = addr.getAddress();
					int sptr = 0;
					sptr = StringUtil.concat(sbuff, sptr, String.valueOf(ip4[3]));
					sbuff[sptr++] = '.';
					sptr = StringUtil.concat(sbuff, sptr, String.valueOf(ip4[2]));
					sbuff[sptr++] = '.';
					sptr = StringUtil.concat(sbuff, sptr, String.valueOf(ip4[1]));
					sbuff[sptr++] = '.';
					sptr = StringUtil.concat(sbuff, sptr, String.valueOf(ip4[0]));
					sptr = StringUtil.concat(sbuff, sptr, ".in-addr.arpa");
					cbuff = sbuff;
					cptr1 = 0;
				}
				else if (addr instanceof Inet6Address)
				{
					byte[] ip6 = addr.getAddress();
					int sptr = 0;
					int i = 16;
					while (i-- > 0)
					{
						sbuff[sptr++] = (byte)StringUtil.hex_array[ip6[i] & 15];
						sbuff[sptr++] = '.';
						sbuff[sptr++] = (byte)StringUtil.hex_array[ip6[i] >> 4];
						sbuff[sptr++] = '.';
					}
					sptr = StringUtil.concat(sbuff, sptr, "ip6.arpa");
					cbuff = sbuff;
					cptr1 = 0;
				}
				else
				{
					cbuff = domain.getBytes(StandardCharsets.UTF_8);
					cptr1 = 0;
				}
			}
			catch (UnknownHostException ex)
			{
				cbuff = domain.getBytes(StandardCharsets.UTF_8);
				cptr1 = 0;
			}
		}
		else
		{
			cbuff = domain.getBytes(StandardCharsets.UTF_8);
			cptr1 = 0;
		}
		while (true)
		{
			cptr2 = cptr1;
			while (cptr2 < cbuff.length)
			{
				c = cbuff[cptr2];
				if (c == '.')
					break;
				cptr2++;
			}
			buff[ptr1++] = (byte)(cptr2 - cptr1);
			while (cptr1 < cptr2)
			{
				buff[ptr1++] = cbuff[cptr1++];
			}
			if (cptr1 >= cbuff.length || cbuff[cptr1] == 0)
			{
				buff[ptr1++] = 0;
				break;
			}
			else
			{
				cptr1++;
			}
		}
		ByteTool.writeMInt16(buff, ptr1 + 0, reqType);
		ByteTool.writeMInt16(buff, ptr1 + 2, 1);
		ptr1 += 4;
		ptr2 = 0;
		
		RequestStatus req = this.newReq(currId);
		this.svr.sendTo(this.serverAddr, 53, buff, 0, (ptr1 - ptr2));
		req.finEvt.waitEvent(2000);
		if (req.respSize > 12)
		{
			ret = parseAnswers(req.respBuff, 0, req.respSize, answers);
		}
		this.delReq(currId);
		return ret;
	}

	public int getByIPv4Name(List<DNSRequestAnswer> answers, int ip)
	{
		int ret = 0;
		byte buff[] = new byte[512];
		byte[] localIP = new byte[4];
		int ptr1;
		int ptr2;
		int currId = this.nextId();
		ByteTool.writeInt32(localIP, 0, ip);
	
		ByteTool.writeMInt16(buff, 0, currId);
		ByteTool.writeMInt16(buff, 2, 0x100); //flags
		ByteTool.writeMInt16(buff, 4, 1); //reqCount
		ByteTool.writeMInt16(buff, 6, 0);
		ByteTool.writeMInt32(buff, 8, 0);
		ptr1 = 12;
		ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(localIP[3]));
		buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
		ptr1 = ptr2;
		ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(localIP[2]));
		buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
		ptr1 = ptr2;
		ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(localIP[1]));
		buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
		ptr1 = ptr2;
		ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(localIP[0]));
		buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
		ptr1 = ptr2;
		buff[ptr1++] = 7;
		ptr1 = StringUtil.concat(buff, ptr1, "in-addr");
		buff[ptr1++] = 4;
		ptr1 = StringUtil.concat(buff, ptr1, "arpa");
		buff[ptr1++] = 0;
		ByteTool.writeMInt16(buff, ptr1 + 0, 12);
		ByteTool.writeMInt16(buff, ptr1 + 2, 1);
		ptr1 += 4;
		ptr2 = 0;
		
		RequestStatus req = this.newReq(currId);
		this.svr.sendTo(this.serverAddr, 53, buff, 0, (ptr1 - ptr2));
		req.finEvt.waitEvent(2000);
		if (req.respSize > 12)
		{
			ret = parseAnswers(req.respBuff, 0, req.respSize, answers);
		}
		this.delReq(currId);
		return ret;
	}

	public int getByAddrName(List<DNSRequestAnswer> answers, InetAddress addr)
	{
		int ret = 0;
		byte buff[] = new byte[512];
		int ptr1;
		int ptr2;
		int currId = this.nextId();
	
		if (addr instanceof Inet4Address)
		{
			Inet4Address ip4 = (Inet4Address)addr;
			byte[] ip4b = ip4.getAddress();
			ByteTool.writeMInt16(buff, 0, currId);
			ByteTool.writeMInt16(buff, 2, 0x100); //flags
			ByteTool.writeMInt16(buff, 4, 1); //reqCount
			ByteTool.writeMInt16(buff, 6, 0);
			ByteTool.writeMInt32(buff, 8, 0);
			ptr1 = 12;
			ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(ip4b[3]));
			buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
			ptr1 = ptr2;
			ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(ip4b[2]));
			buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
			ptr1 = ptr2;
			ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(ip4b[1]));
			buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
			ptr1 = ptr2;
			ptr2 = StringUtil.concat(buff, ptr1 + 1, String.valueOf(ip4b[0]));
			buff[ptr1] = (byte)(ptr2 - ptr1 - 1);
			ptr1 = ptr2;
			buff[ptr1++] = 7;
			ptr1 = StringUtil.concat(buff, ptr1, "in-addr");
			buff[ptr1++] = 4;
			ptr1 = StringUtil.concat(buff, ptr1, "arpa");
			buff[ptr1++] = 0;
			ByteTool.writeMInt16(buff, ptr1 + 0, 12);
			ByteTool.writeMInt16(buff, ptr1 + 2, 1);
			ptr1 += 4;
			ptr2 = 0;
		}
		else if (addr instanceof Inet6Address)
		{
			Inet6Address ip6 = (Inet6Address)addr;
			byte[] ip6b = ip6.getAddress();
			ByteTool.writeMInt16(buff, 0, currId);
			ByteTool.writeMInt16(buff, 2, 0x100); //flags
			ByteTool.writeMInt16(buff, 4, 1); //reqCount
			ByteTool.writeMInt16(buff, 6, 0);
			ByteTool.writeMInt32(buff, 8, 0);
			ptr1 = 12;
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[15] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[15] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[14] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[14] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[13] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[13] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[12] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[12] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[11] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[11] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[10] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[10] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[9] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[9] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[8] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[8] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[7] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[7] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[6] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[6] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[5] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[5] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[4] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[4] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[3] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[3] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[2] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[2] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[1] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[1] >> 4];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[0] & 15];
			buff[ptr1++] = 1;
			buff[ptr1++] = (byte)StringUtil.hex_array[ip6b[0] >> 4];
			buff[ptr1++] = 3;
			ptr1 = StringUtil.concat(buff, ptr1, "ip6");
			buff[ptr1++] = 4;
			ptr1 = StringUtil.concat(buff, ptr1, "arpa");
			buff[ptr1++] = 0;
			ByteTool.writeMInt16(buff, ptr1 + 0, 12);
			ByteTool.writeMInt16(buff, ptr1 + 2, 1);
			ptr1 += 4;
			ptr2 = 0;
		}
		else
		{
			return 0;
		}
		
		RequestStatus req = this.newReq(currId);
		this.svr.sendTo(this.serverAddr, 53, buff, 0, ptr1 - ptr2);
		req.finEvt.waitEvent(2000);
		if (req.respSize > 12)
		{
			ret = parseAnswers(req.respBuff, 0, req.respSize, answers);
		}
		this.delReq(currId);
		return ret;
	}

	public int getServerName(List<DNSRequestAnswer> answers)
	{
		return this.getByAddrName(answers, this.serverAddr);
	}

	public int getCAARecord(List<DNSRequestAnswer> answers, String domain)
	{
		return this.getByType(answers, domain, 257);
	}

	public void UpdateDNSAddr(InetAddress serverAddr)
	{
		this.serverAddr = serverAddr;
	}

	public static int parseString(byte[] sbuff, int sbuffOfst, byte[] buff, int stringOfst, int endOfst, SharedInt sbuffEndOfst)
	{
		boolean found = false;
		int i = stringOfst;
		int j;
		int l;
		while (i < endOfst)
		{
			j = buff[i] & 0xff;
			if (j == 0)
			{
				i++;
				break;
			}
			if (j >= 0xc0)
			{
				l = ((j - 0xc0) << 8) + buff[i + 1];
				while (l < endOfst)
				{
					j = buff[l];
					if (j == 0)
					{
						break;
					}
					if (j >= 0xc0)
					{
						l = ((j - 0xc0) << 8) + buff[l + 1];
					}
					else
					{
						if (found)
							sbuff[sbuffOfst++] = '.';
						l++;
						while (j-- > 0)
						{
							sbuff[sbuffOfst++] = buff[l++];
						}
						found = true;
					}
				}
				i += 2;
				break;
			}
			else
			{
				if (found)
					sbuff[sbuffOfst++] = '.';
				i++;
				while (j-- > 0)
				{
					sbuff[sbuffOfst++] = buff[i++];
				}
				found = true;
			}
		}
		sbuff[sbuffOfst] = 0;
		if (sbuffEndOfst != null)
		{
			sbuffEndOfst.value = sbuffOfst;
		}
		return i;
	}

	public static int parseAnswers(byte[] buff, int ofst, int dataSize, List<DNSRequestAnswer> answers)
	{
		byte[] sbuff = new byte[512];
		DNSRequestAnswer ans;
		int ansCount = ByteTool.readMUInt16(buff, ofst + 6);
		int cnt2 = ByteTool.readMUInt16(buff, ofst + 8);
		int cnt3 = ByteTool.readMUInt16(buff, ofst + 10);
		ansCount += cnt2 + cnt3;
		SharedInt i = new SharedInt();
		int j;
		i.value = parseString(sbuff, 0, buff, ofst + 12, dataSize, null);
		i.value += 4;
	
		j = 0;
		while (j < ansCount && i.value < ofst + dataSize)
		{
			ans = parseAnswer(buff, ofst + dataSize, i);
			answers.add(ans);
	
			j++;
		}
		return ansCount;
	}

	public static DNSRequestAnswer parseAnswer(byte[] buff, int dataSize, SharedInt index)
	{
		byte[] sbuff = new byte[512];
		SharedInt sptr = new SharedInt();
		DNSRequestAnswer ans;
		int i = index.value;
		int k;
		i = parseString(sbuff, 0, buff, i, dataSize, sptr);
		ans = new DNSRequestAnswer();
		ans.name = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
		ans.recType = ByteTool.readMUInt16(buff, i);
		ans.recClass = ByteTool.readMUInt16(buff, i + 2);
		ans.ttl = ByteTool.readMInt32(buff, i + 4);
		ans.addr = null;
		k = ByteTool.readMUInt16(buff, i + 8);
		switch (ans.recType)
		{
		case 1: // A - a host address
			try
			{
				ans.addr = InetAddress.getByAddress(Arrays.copyOfRange(buff, i + 10, i + 14));
			}
			catch (UnknownHostException ex)
			{
				ex.printStackTrace();
			}
			ans.rd = ans.addr.toString();
			break;
		case 2: // NS - an authoritative name server
		case 5: // CNAME - the canonical name for an alias
		case 12: // PTR - a domain name pointer
			parseString(sbuff, 0, buff, i + 10, i + 10 + k, sptr);
			ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			break;
		case 6: // SOA - Start of [a zone of] authority
			{
				int l;
				StringBuilder sb = new StringBuilder();
				l = parseString(sbuff, 0, buff, i + 10, i + 10 + k, sptr);
				sb.append(new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8));
				sb.append(", MailAddr=");
				l = parseString(sbuff, 0, buff, l, i + 10 + k, sptr);
				sb.append(new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8));
				if (l + 20 <= i + 10 + k)
				{
					sb.append(", SN=");
					sb.append(ByteTool.readMInt32(buff, l));
					sb.append(", Refresh=");
					sb.append(ByteTool.readMInt32(buff, l + 4));
					sb.append(", Retry=");
					sb.append(ByteTool.readMInt32(buff, l + 8));
					sb.append(", Expire=");
					sb.append(ByteTool.readMInt32(buff, l + 12));
					sb.append(", DefTTL=");
					sb.append(ByteTool.readMInt32(buff, l + 16));
				}
				ans.rd = sb.toString();
			}
			break;
		case 15: // MX - mail exchange
			ans.priority = ByteTool.readMUInt16(buff, i + 10);
			parseString(sbuff, 0, buff, i + 12, i + 10 + k, sptr);
			ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			break;
		case 16: // TXT - Text strings
			{
				sptr.value = 0;
				sbuff[0] = 0;
				int currInd = i + 10;
				int endInd = i + 10 + k;
				while (currInd < endInd)
				{
					if (sptr.value != 0)
					{
						sptr.value = StringUtil.concat(sbuff, sptr.value, ", ");
					}
					sptr.value = StringUtil.concat(sbuff, sptr.value, buff, currInd + 1, buff[currInd]);
					currInd += 1 + (buff[currInd] & 0xff);
				}
				ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			}
			break;
		case 28: // AAAA
			{
				try
				{
					ans.addr = InetAddress.getByAddress(Arrays.copyOfRange(buff, i + 10, i + 26));
				}
				catch (UnknownHostException ex)
				{
					ex.printStackTrace();
				}
				ans.rd = ans.addr.toString();
			}
			break;
		case 33: // SRV - Server Selection
			{
				ans.priority = ByteTool.readMUInt16(buff, i + 10);
				sptr.value = StringUtil.concat(sbuff, 0, "Weight = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMUInt16(buff, i + 12)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Port = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMUInt16(buff, i + 14)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Target = ");
				parseString(sbuff, sptr.value, buff, i + 16, i + 10 + k, sptr);
				ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			}
			break;
		case 48: // DNSKEY - DNS Key record
			{
				sptr.value = StringUtil.concat(sbuff, 0, "Flags = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMUInt16(buff, i + 10)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Protocol = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(buff[i + 12]));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Algorithm = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(buff[i + 13]));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Public Key = ");
				sptr.value = StringUtil.concatHexBytes(sbuff, sptr.value, buff, i + 14, k - 4, (byte)' ');
				ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			}
			break;
		case 46: // RRSIG - DNSSEC signature
			{
				sptr.value = StringUtil.concat(sbuff, 0, "Type Covered = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMUInt16(buff, i + 10)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Algorithm = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(buff[i + 12]));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Labels = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(buff[i + 13]));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Original TTL = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMInt32(buff, i + 14)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Signature Expiration = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMInt32(buff, i + 18)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Signature Inception = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMInt32(buff, i + 22)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Key Tag = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMUInt16(buff, i + 26)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Signer's Name = ");
				int tmpPtr = i + 28;
				while ((buff[sptr.value++] = buff[tmpPtr++]) != 0);
				sptr.value--;
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Signature = ");
				sptr.value = StringUtil.concatHexBytes(sbuff, sptr.value, buff, tmpPtr, k - (tmpPtr - (i + 10)), (byte)' ');
				ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			}
			break;
		case 43: // DS - Delegation signer
			{
				sptr.value = StringUtil.concat(sbuff, 0, "Key Tag = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(ByteTool.readMUInt16(buff, i + 10)));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Algorithm = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(buff[i + 12]));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Digest Type = ");
				sptr.value = StringUtil.concat(sbuff, sptr.value, String.valueOf(buff[i + 13]));
				sptr.value = StringUtil.concat(sbuff, sptr.value, ", Digest = ");
				sptr.value = StringUtil.concatHexBytes(sbuff, sptr.value, buff, i + 14, k - 4, (byte)' ');
				ans.rd = new String(sbuff, 0, sptr.value, StandardCharsets.UTF_8);
			}
			break;
		case 257: // CAA - Certification Authority Authorization
			{
				StringBuilder sb = new StringBuilder();
				sb.append("CAA ");
				sb.append(buff[i + 10]);
				sb.append(' ');
				sb.append(new String(buff, i + 12, buff[i + 11], StandardCharsets.UTF_8));
				sb.append(' ');
				int l = i + 12 + buff[i + 11];
				sb.append(new String(buff, l, i + 10 + k - l, StandardCharsets.UTF_8));
				ans.rd = sb.toString();
			}
			break;
		case 47: // NSEC - Next Secure record
		default:
			ans.rd = null;
			break;
		}
		index.value = i + k + 10;
		return ans;
	}
}
