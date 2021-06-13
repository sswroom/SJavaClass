package org.sswr.util.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.SharedInt;

public class SNMPClient implements UDPPacketListener
{
	private UDPServer svr;
	private int reqId;
	private ThreadEvent respEvt;
	private boolean hasResp;
	private SNMPErrorStatus respStatus;
	private List<SNMPBindingItem> respList;
	private Object scanMut;
	private List<InetAddress> scanList;

	public void udpPacketReceived(InetAddress addr, int port, byte []buff, int ofst, int dataSize)
	{
		List<SNMPBindingItem> itemList = new ArrayList<SNMPBindingItem>();
		SharedInt reqId = new SharedInt();
		SNMPErrorStatus err;
		synchronized (this.scanMut)
		{
			if (this.scanList != null)
			{
				if (port == 161)
				{
					this.scanList.add(addr);
				}
				return;
			}
		}
		err = SNMPUtil.pduParseMessage(buff, ofst, dataSize, reqId, itemList);
		if (!this.hasResp && (err != SNMPErrorStatus.NOERROR || reqId.value == this.reqId))
		{
			if (this.respList != null)
			{
				this.respList.addAll(itemList);
			}
			this.respStatus = err;
			this.hasResp = true;
			this.respEvt.set();
		}
	}

	public SNMPClient()
	{
		this.scanMut = new Object();
		this.scanList = null;
		this.svr = new UDPServer(null, 0, null, this, null, null, 1, false);
		this.reqId = 1;
		this.respEvt = new ThreadEvent(true);
		this.hasResp = true;
		this.respStatus = SNMPErrorStatus.NOERROR;
		this.respList = null;
	}

	public void close()
	{
		this.svr.close();
	}

	public boolean isError()
	{
		return this.svr.isError();
	}

	public SNMPErrorStatus v1GetRequest(InetAddress agentAddr, String community, String oid, List<SNMPBindingItem> itemList)
	{
		byte[] pduBuff = new byte[64];
		int oidLen;
		oidLen = SNMPUtil.oidText2PDU(oid, pduBuff, 0);
		return v1GetRequestPDU(agentAddr, community, pduBuff, oidLen, itemList);
	}

	public SNMPErrorStatus v1GetRequestPDU(InetAddress agentAddr, String community, byte[] oid, int oidLen, List<SNMPBindingItem> itemList)
	{
		SharedInt buffSize = new SharedInt();
		byte[] buff;
		SNMPErrorStatus ret;
		synchronized(this)
		{
			ASN1PDUBuilder pdu = new ASN1PDUBuilder();
			pdu.sequenceBegin((byte)0x30);
			pdu.appendInt32(0);
			pdu.appendString(community);
			pdu.sequenceBegin((byte)0xA0);
			pdu.appendUInt32(this.reqId);
			pdu.appendInt32(0);
			pdu.appendInt32(0);
			pdu.sequenceBegin((byte)0x30);
			pdu.sequenceBegin((byte)0x30);
			pdu.appendOID(oid, oidLen);
			pdu.appendNull();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			buff = pdu.getBuff(buffSize);
			this.respList = itemList;
			this.respStatus = SNMPErrorStatus.NORESP;
			this.hasResp = false;
			this.respEvt.clear();
			this.svr.sendTo(agentAddr, 161, buff, 0, buffSize.value);
			this.respEvt.waitEvent(5000);
			this.respList = null;
			ret = this.respStatus;
			this.hasResp = true;
		
			this.reqId++;
		}
		return ret;
	}

	public SNMPErrorStatus v1GetNextRequest(InetAddress agentAddr, String community, String oid, List<SNMPBindingItem> itemList)
	{
		byte[] pduBuff = new byte[64];
		int oidLen;
		oidLen = SNMPUtil.oidText2PDU(oid, pduBuff, 0);
		return v1GetNextRequestPDU(agentAddr, community, pduBuff, oidLen, itemList);
	}

	public SNMPErrorStatus v1GetNextRequestPDU(InetAddress agentAddr, String community, byte[] oid, int oidLen, List<SNMPBindingItem> itemList)
	{
		SharedInt buffSize = new SharedInt();
		byte[] buff;
		SNMPErrorStatus ret;
		synchronized(this)
		{
			ASN1PDUBuilder pdu = new ASN1PDUBuilder();
			pdu.sequenceBegin((byte)0x30);
			pdu.appendInt32(0);
			pdu.appendString(community);
			pdu.sequenceBegin((byte)0xA1);
			pdu.appendUInt32(this.reqId);
			pdu.appendInt32(0);
			pdu.appendInt32(0);
			pdu.sequenceBegin((byte)0x30);
			pdu.sequenceBegin((byte)0x30);
			pdu.appendOID(oid, oidLen);
			pdu.appendNull();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			buff = pdu.getBuff(buffSize);
			this.respList = itemList;
			this.respStatus = SNMPErrorStatus.NORESP;
			this.hasResp = false;
			this.respEvt.clear();
			this.svr.sendTo(agentAddr, 161, buff, 0, buffSize.value);
			this.respEvt.waitEvent(5000);
			this.respList = null;
			ret = this.respStatus;
			this.hasResp = true;
		
			this.reqId++;
		}
		return ret;
	}

	public SNMPErrorStatus v1Walk(InetAddress agentAddr, String community, String oid, List<SNMPBindingItem> itemList)
	{
		SNMPErrorStatus ret;
		List<SNMPBindingItem> thisList = new ArrayList<SNMPBindingItem>();
		SNMPBindingItem item;
		SNMPBindingItem lastItem = null;
		ret = this.v1GetNextRequest(agentAddr, community, oid, thisList);
		if (ret != SNMPErrorStatus.NOERROR)
		{
			itemList.addAll(thisList);
			return ret;
		}
		while (thisList.size() == 1)
		{
			item = thisList.get(0);
			if (lastItem != null && lastItem.getOidLen() == item.getOidLen() && SNMPUtil.oidCompare(lastItem.getOid(), lastItem.getOidLen(), item.getOid(), item.getOidLen()) == 0)
			{
				break;
			}
			thisList.clear();
			itemList.add(item);
			lastItem = item;
			ret = this.v1GetNextRequestPDU(agentAddr, community, item.getOid(), item.getOidLen(), thisList);
			if (ret == SNMPErrorStatus.NORESP)
			{
				thisList.clear();
				ret = this.v1GetNextRequestPDU(agentAddr, community, item.getOid(), item.getOidLen(), thisList);
			}
			if (ret == SNMPErrorStatus.NOSUCHNAME)
			{
				thisList.clear();
				break;
			}
			else if (ret != SNMPErrorStatus.NOERROR)
			{
				break;
			}
		}
		itemList.addAll(thisList);
		return SNMPErrorStatus.NOERROR;
	}

	public int v1ScanGetRequest(InetAddress broadcastAddr, String community, String oid, List<InetAddress> addrList, int timeoutMS, boolean scanIP)
	{
		byte pduBuff[] = new byte[64];
		int oidLen;
		SharedInt buffSize = new SharedInt();
		byte []buff;
		int initCnt = addrList.size();
		synchronized(this)
		{
			ASN1PDUBuilder pdu = new ASN1PDUBuilder();
			pdu.sequenceBegin((byte)0x30);
			pdu.appendInt32(0);
			pdu.appendString(community);
			pdu.sequenceBegin((byte)0xA0);
			pdu.appendUInt32(this.reqId);
			pdu.appendInt32(0);
			pdu.appendInt32(0);
			pdu.sequenceBegin((byte)0x30);
			pdu.sequenceBegin((byte)0x30);
			oidLen = SNMPUtil.oidText2PDU(oid, pduBuff, 0);
			pdu.appendOID(pduBuff, oidLen);
			pdu.appendNull();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			pdu.sequenceEnd();
			buff = pdu.getBuff(buffSize);
			synchronized(this.scanMut)
			{
				this.scanList = addrList;
			}
			if (scanIP && broadcastAddr instanceof Inet4Address)
			{
				byte[] addr = ((Inet4Address)broadcastAddr).getAddress();
				addr[3] = 1;
				while ((addr[3] & 0xff) < 255)
				{
					try
					{
						this.svr.sendTo(Inet4Address.getByAddress(addr), 161, buff, 0, buffSize.value);
					}
					catch (UnknownHostException ex)
					{
						ex.printStackTrace();
					}
					addr[3]++;
				}
			}
			else
			{
				this.svr.setBroadcast(true);
				this.svr.sendTo(broadcastAddr, 161, buff, 0, buffSize.value);
			}
			try
			{
				Thread.sleep(timeoutMS);
			}
			catch (InterruptedException ex)
			{
				ex.printStackTrace();
			}
			synchronized(this.scanMut)
			{
				this.scanList = null;
			}
		}
		return addrList.size() - initCnt;
	}
}
