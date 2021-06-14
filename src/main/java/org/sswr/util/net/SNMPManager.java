package org.sswr.util.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;

public class SNMPManager
{
	private SNMPClient cli;
	private List<SNMPAgentInfo> agentList;
	private Map<Integer, SNMPAgentInfo> ipv4Agents;

	public SNMPManager(InetAddress bindaddr)
	{
		this.agentList = new ArrayList<SNMPAgentInfo>();
		this.ipv4Agents = new HashMap<Integer, SNMPAgentInfo>();
		this.cli = new SNMPClient(bindaddr);
	}

	public void close()
	{
		this.cli.close();
	}

	public boolean isError()
	{
		return this.cli.isError();
	}

	public void updateValues()
	{
		int i = this.agentList.size();
		int j;
		SNMPAgentInfo agent;
		SNMPReadingInfo reading;
		SharedInt iVal = new SharedInt();
		List<SNMPBindingItem> itemList = new ArrayList<SNMPBindingItem>();
		SNMPBindingItem item;
	
		SNMPErrorStatus err;
		while (i-- > 0)
		{
			agent = this.agentList.get(i);
			j = agent.getReadingList().size();
			while (j-- > 0)
			{
				reading = agent.getReadingList().get(j);
				err = this.cli.v1GetRequestPDU(agent.getAddr(), agent.getCommunity(), reading.getObjId(), reading.getObjIdLen(), itemList);
				if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
				{
					item = itemList.get(0);
					if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
					{
						reading.setCurrVal(iVal.value * reading.getMulVal());
						reading.setValValid(iVal.value != reading.getInvVal());
					}
					else
					{
						reading.setValValid(false);
					}
				}
				itemList.clear();
			}
		}
	}

	public int getAgentList(List<SNMPAgentInfo> agentList)
	{
		int ret;
		synchronized(this.agentList)
		{
			ret = this.agentList.size();
			agentList.addAll(this.agentList);
		}
		return ret;
	}

	public SNMPAgentInfo addAgent(InetAddress addr, String community)
	{
		List<SNMPBindingItem> itemList = new ArrayList<SNMPBindingItem>();
		SNMPErrorStatus err;
		SNMPAgentInfo agent = null;
		SNMPBindingItem item;
		int i;
		if (addr instanceof Inet4Address)
		{
			int ipv4 = ByteTool.readMInt32(((Inet4Address)addr).getAddress(), 0);
			if (this.ipv4Agents.get(ipv4) != null)
			{
				return null;
			}
		}
		err = this.cli.v1GetRequest(addr, community, "1.3.6.1.2.1.1.1.0", itemList); //sysDescr
		i = itemList.size();
		if (err == SNMPErrorStatus.NOERROR && i == 1)
		{
			item = itemList.get(0);
			if (item.getValType() == 4 && item.getValLen() > 0)
			{
				agent = new SNMPAgentInfo();
				agent.setAddr(addr);
				agent.setCommunity(community);
				agent.setDescr(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
				agent.setObjIdLen(0);
				agent.setName(null);
				agent.setLocation(null);
				agent.setContact(null);
				agent.setModel(null);
				agent.setVendor(null);
				agent.setCpuName(null);
				agent.setMac(new byte[6], 0);
				agent.setReadingList(new ArrayList<SNMPReadingInfo>());
				synchronized(this.agentList)
				{
					this.agentList.add(agent);
					if (addr instanceof Inet4Address)
					{
						int ipv4 = ByteTool.readMInt32(((Inet4Address)addr).getAddress(), 0);
						this.ipv4Agents.put(ipv4, agent);
					}
				}
			}
		}
		itemList.clear();
		if (agent != null)
		{
			err = this.cli.v1GetRequest(addr, community, "1.3.6.1.2.1.1.2.0", itemList); //sysObjectID
			if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
			{
				item = itemList.get(0);
				if (item.getValType() == 6 && item.getValLen() > 0)
				{
					agent.setObjId(item.getValBuff(), 0, item.getValLen());
				}
			}
			itemList.clear();
	
			err = this.cli.v1GetRequest(addr, community, "1.3.6.1.2.1.1.4.0", itemList); //sysContact
			if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
			{
				item = itemList.get(0);
				if (item.getValType() == 4 && item.getValLen() > 0)
				{
					agent.setContact(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
				}
			}
			itemList.clear();
	
			err = this.cli.v1GetRequest(addr, community, "1.3.6.1.2.1.1.5.0", itemList); //sysName
			if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
			{
				item = itemList.get(0);
				if (item.getValType() == 4 && item.getValLen() > 0)
				{
					agent.setName(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
				}
			}
			itemList.clear();
	
			err = this.cli.v1GetRequest(addr, community, "1.3.6.1.2.1.1.6.0", itemList); //sysLocation
			if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
			{
				item = itemList.get(0);
				if (item.getValType() == 4 && item.getValLen() > 0)
				{
					agent.setLocation(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
				}
			}
			itemList.clear();
	
			int iv = 1;
			while (true)
			{
				err = this.cli.v1GetRequest(addr, community, "1.3.6.1.2.1.2.2.1.6." + iv, itemList); //sysLocation
				if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
				{
					item = itemList.get(0);
					if (item.getValType() == 4 && item.getValLen() == 6)
					{
						agent.setMac(item.getValBuff(), 0);
					}
				}
				else if (err == SNMPErrorStatus.NOSUCHNAME)
				{
					itemList.clear();
					break;
				}
				itemList.clear();
	
				if (agent.getMac()[5] != 0 || agent.getMac()[4] != 0 || agent.getMac()[3] != 0)
				{
					break;
				}
				iv++;
			}
	
			if (agent.getObjIdLen() > 0)
			{
				String soid;
				boolean found = false;
				int pduSize;
				byte[] oidPDU = new byte[64];
				if (!found)
				{
					pduSize = SNMPUtil.oidText2PDU("1.3.6.1.4.1.24681", oidPDU, 0); //QNAP
					if (SNMPUtil.oidCompare(oidPDU, pduSize, agent.getObjId(), agent.getObjIdLen()) == 0)
					{
						SNMPReadingInfo reading;
						SharedInt slotCnt = new SharedInt();
						SharedInt iVal = new SharedInt();
						StringBuilder sb = new StringBuilder();
						found = true;
						agent.setVendor("QNAP");
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.24681.1.4.1.1.1.1.1.2.1.5.1", itemList); //enclosureSlot.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), slotCnt);
						}
						itemList.clear();
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.24681.1.4.1.1.1.1.1.2.1.3.1", itemList); //enclosureModel.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (item.getValType() == 4 && item.getValLen() > 0)
							{
								agent.setModel(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
							}
						}
						itemList.clear();
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.24681.1.4.1.1.1.1.1.2.1.7.1", itemList); //enclosureSystemTemp.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
							{
								reading = new SNMPReadingInfo();
								reading.setName("System Temp");
								reading.setIndex(0);
								reading.setObjIdLen(SNMPUtil.oidText2PDU("1.3.6.1.4.1.24681.1.4.1.1.1.1.1.2.1.7.1", reading.getObjId(), 0));
								reading.setMulVal(1.0);
								reading.setInvVal(-1);
								reading.setReadingType(SMonitorReadingType.TEMPERATURE);
								reading.setValValid(true);
								reading.setCurrVal(iVal.value);
								agent.getReadingList().add(reading);
							}
						}
						itemList.clear();
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.24681.1.4.1.1.1.1.2.2.1.5.1", itemList); //systemFanSpeed.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
							{
								reading = new SNMPReadingInfo();
								reading.setName("System Fan Speed");
								reading.setIndex(1);
								reading.setObjIdLen(SNMPUtil.oidText2PDU("1.3.6.1.4.1.24681.1.4.1.1.1.1.2.2.1.5.1", reading.getObjId(), 0));
								reading.setMulVal(1.0);
								reading.setInvVal(-1);
								reading.setReadingType(SMonitorReadingType.ENGINERPM);
								reading.setValValid(true);
								reading.setCurrVal(iVal.value);
								agent.getReadingList().add(reading);
							}
						}
						itemList.clear();
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.24681.1.4.1.1.1.1.3.2.1.5.1", itemList); //systemPowerFanSpeed.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
							{
								reading = new SNMPReadingInfo();
								reading.setName("Power Fan Speed");
								reading.setIndex(2);
								reading.setObjIdLen(SNMPUtil.oidText2PDU("1.3.6.1.4.1.24681.1.4.1.1.1.1.3.2.1.5.1", reading.getObjId(), 0));
								reading.setMulVal(1.0);
								reading.setInvVal(-1);
								reading.setReadingType(SMonitorReadingType.ENGINERPM);
								reading.setValValid(true);
								reading.setCurrVal(iVal.value);
								agent.getReadingList().add(reading);
							}
						}
						itemList.clear();
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.24681.1.4.1.1.1.1.3.2.1.6.1", itemList); //systemPowerTemp.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
							{
								reading = new SNMPReadingInfo();
								reading.setName("Power Temp");
								reading.setIndex(3);
								reading.setObjIdLen(SNMPUtil.oidText2PDU("1.3.6.1.4.1.24681.1.4.1.1.1.1.3.2.1.6.1", reading.getObjId(), 0));
								reading.setMulVal(1.0);
								reading.setInvVal(-1);
								reading.setReadingType(SMonitorReadingType.TEMPERATURE);
								reading.setValValid(true);
								reading.setCurrVal(iVal.value);
								agent.getReadingList().add(reading);
							}
						}
						itemList.clear();
	
						if (slotCnt.value == 0)
						{
							slotCnt.value = 4;
						}
						i = 0;
						while (i < slotCnt.value)
						{
							soid = "1.3.6.1.4.1.24681.1.4.1.1.1.1.5.2.1.6." + i;
							err = this.cli.v1GetRequest(addr, community, soid, itemList); //diskTemperature
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
								{
									reading = new SNMPReadingInfo();
									reading.setName(null);
									reading.setIndex(4 + i);
									reading.setObjIdLen(SNMPUtil.oidText2PDU(soid, reading.getObjId(), 0));
									reading.setMulVal(1.0);
									reading.setInvVal(-1);
									reading.setReadingType(SMonitorReadingType.TEMPERATURE);
									reading.setValValid(true);
									reading.setCurrVal(iVal.value);
									agent.getReadingList().add(reading);
									itemList.clear();
	
									sb.setLength(0);
									sb.append("Disk ");
									sb.append(i);
									soid = "1.3.6.1.4.1.24681.1.4.1.1.1.1.5.2.1.8." + i;
									err = this.cli.v1GetRequest(addr, community, soid, itemList); //diskModel
									if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
									{
										item = itemList.get(0);
										if (item.getValType() == 4 && item.getValLen() > 0)
										{
											sb.append(' ');
											sb.append(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
										}
									}
									reading.setName(sb.toString());
	
									soid = "1.3.6.1.4.1.24681.1.4.1.1.1.1.5.2.1.2." + i;
									err = this.cli.v1GetRequest(addr, community, soid, itemList); //diskID
									if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
									{
										item = itemList.get(0);
										if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
										{
											reading.setIndex(3 + iVal.value);
										}
									}
								}
							}
							itemList.clear();
	
							i++;
						}
					}
				}
				if (!found)
				{
					pduSize = SNMPUtil.oidText2PDU("1.3.6.1.4.1.8072.3.2.10", oidPDU, 0); //Linux
					if (SNMPUtil.oidCompare(oidPDU, pduSize, agent.getObjId(), agent.getObjIdLen()) == 0)
					{
						if (agent.getMac()[0] == 0x00 && agent.getMac()[1] == 0x11 && agent.getMac()[2] == 0x32) //Synology
						{
							SNMPReadingInfo reading;
							SharedInt iVal = new SharedInt();
							StringBuilder sb = new StringBuilder();
							found = true;
							agent.setVendor("Synology");
	
							err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.6574.1.5.1.0", itemList); //modelName.1
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (item.getValType() == 4 && item.getValLen() > 0)
								{
									agent.setModel(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
								}
							}
							itemList.clear();
	
							err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.6574.1.2.0", itemList); //temperature.0
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal) && iVal.value > 0)
								{
									reading = new SNMPReadingInfo();
									reading.setName("System Temp");
									reading.setIndex(0);
									reading.setObjIdLen(SNMPUtil.oidText2PDU("1.3.6.1.4.1.6574.1.2.0", reading.getObjId(), 0));
									reading.setMulVal(1.0);
									reading.setInvVal(0);
									reading.setReadingType(SMonitorReadingType.TEMPERATURE);
									reading.setValValid(true);
									reading.setCurrVal(iVal.value);
									agent.getReadingList().add(reading);
								}
							}
							itemList.clear();
	
							i = 0;
							while (true)
							{
								soid = "1.3.6.1.4.1.6574.2.1.1.6." + i;
								err = this.cli.v1GetRequest(addr, community, soid, itemList); //diskTemperature
								if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
								{
									item = itemList.get(0);
									if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
									{
										reading = new SNMPReadingInfo();
										reading.setName(null);
										reading.setIndex(4 + i);
										reading.setObjIdLen(SNMPUtil.oidText2PDU(soid, reading.getObjId(), 0));
										reading.setMulVal(1.0);
										reading.setInvVal(0);
										reading.setReadingType(SMonitorReadingType.TEMPERATURE);
										reading.setValValid(iVal.value > 0);
										reading.setCurrVal(iVal.value);
										agent.getReadingList().add(reading);
										itemList.clear();
	
										sb.setLength(0);
										soid = "1.3.6.1.4.1.6574.2.1.1.2." + i;
										err = this.cli.v1GetRequest(addr, community, soid, itemList); //diskID
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (item.getValType() == 4 && item.getValLen() > 0)
											{
												sb.append(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
												if (item.getValLen() >= 6)
												{
													if (item.getValBuff()[5] >= '1' && item.getValBuff()[5] <= '9')
													{
														reading.setIndex(3 + item.getValBuff()[5] - 0x30);
													}
												}
											}
										}
										if (sb.length() == 0)
										{
											sb.append("Disk ");
											sb.append(i + 1);
										}
										itemList.clear();
	
										soid = "1.3.6.1.4.1.6574.2.1.1.3." + i;
										err = this.cli.v1GetRequest(addr, community, soid, itemList); //diskModel
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (item.getValType() == 4 && item.getValLen() > 0)
											{
												sb.append(' ');
												sb.append(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
												StringUtil.trimRight(sb);
											}
										}
										reading.setName(sb.toString());
									}
								}
								else
								{
									itemList.clear();
									break;
								}
								itemList.clear();
								i++;
							}
						}
					}
				}
				if (!found)
				{
					pduSize = SNMPUtil.oidText2PDU("1.3.6.1.4.1.1602", oidPDU, 0); //Canon
					if (SNMPUtil.oidStartsWith(agent.getObjId(), agent.getObjIdLen(), oidPDU, pduSize))
					{
						SharedInt iVal = new SharedInt();
						SNMPReadingInfo reading;
	
						found = true;
						agent.setVendor("Canon");
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.1602.1.1.1.2.0", itemList); //enclosureSlot.1
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (item.getValType() == 4 && item.getValLen() > 0)
							{
								agent.setModel(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
							}
						}
						itemList.clear();
	
						i = 1;
						while (true)
						{
							soid = "1.3.6.1.4.1.1602.1.11.2.1.1.3." + i;
							err = this.cli.v1GetRequest(addr, community, soid, itemList); //
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
								{
									reading = new SNMPReadingInfo();
									reading.setName(null);
									reading.setIndex(i - 1);
									reading.setObjIdLen(SNMPUtil.oidText2PDU(soid, reading.getObjId(), 0));
									reading.setMulVal(1.0);
									reading.setInvVal(-1);
									reading.setReadingType(SMonitorReadingType.COUNT);
									reading.setValValid(iVal.value >= 0);
									reading.setCurrVal(iVal.value);
									agent.getReadingList().add(reading);
									itemList.clear();
	
									soid = "1.3.6.1.4.1.1602.1.11.2.1.1.2." + i;
									err = this.cli.v1GetRequest(addr, community, soid, itemList);
									if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
									{
										item = itemList.get(0);
										if (item.getValType() == 4 && item.getValLen() > 0)
										{
											reading.setName(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
										}
									}
									if (reading.getName() == null)
									{
										reading.setName("Count " + i);
									}
								}
								itemList.clear();
							}
							else
							{
								itemList.clear();
								break;
							}
							i++;
						}
					}
				}
				if (!found)
				{
					pduSize = SNMPUtil.oidText2PDU("1.3.6.1.4.1.26696", oidPDU, 0); //HP
					if (SNMPUtil.oidStartsWith(agent.getObjId(), agent.getObjIdLen(), oidPDU, pduSize))
					{
						found = true;
						agent.setVendor("HP");
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.11.2.4.3.1.10.0", itemList); //npSysModelNumber.0
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (item.getValType() == 4 && item.getValLen() > 0)
							{
								agent.setModel(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
							}
						}
						itemList.clear();
					}
				}
				if (!found)
				{
					pduSize = SNMPUtil.oidText2PDU("1.3.6.1.4.1.3854.1", oidPDU, 0); //AKCP sensorProbe
					if (SNMPUtil.oidStartsWith(agent.getObjId(), agent.getObjIdLen(), oidPDU, pduSize))
					{
						SNMPReadingInfo reading;
						SharedInt iVal = new SharedInt();
						found = true;
						agent.setVendor("AKCP");
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.3854.1.1.8.0", itemList); //npSysModelNumber.0
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (item.getValType() == 4 && item.getValLen() > 0)
							{
								agent.setModel(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
							}
						}
						itemList.clear();
	
						i = 0;
						while (true)
						{
							soid = "1.3.6.1.4.1.3854.1.2.2.1.16.1.5." + i;
							err = this.cli.v1GetRequest(addr, community, soid, itemList); //
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
								{
									if (iVal.value == 1)
									{
										soid = "1.3.6.1.4.1.3854.1.2.2.1.16.1.14." + i;
										reading = new SNMPReadingInfo();
										reading.setName(null);
										reading.setIndex(i);
										reading.setObjIdLen(SNMPUtil.oidText2PDU(soid, reading.getObjId(), 0));
										reading.setMulVal(0.1);
										reading.setInvVal(-512);
										reading.setReadingType(SMonitorReadingType.TEMPERATURE);
										reading.setValValid(false);
										reading.setCurrVal(0);
										agent.getReadingList().add(reading);
										itemList.clear();
	
										err = this.cli.v1GetRequest(addr, community, soid, itemList);
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
											{
												reading.setCurrVal(iVal.value * reading.getMulVal());
												reading.setValValid(true);
											}
										}
										itemList.clear();
	
										soid = "1.3.6.1.4.1.3854.1.2.2.1.16.1.1." + i;
										err = this.cli.v1GetRequest(addr, community, soid, itemList);
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (item.getValType() == 4 && item.getValLen() > 0)
											{
												reading.setName(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
											}
										}
										if (reading.getName() == null)
										{
											reading.setName("Temperature" + (i + 1));
										}
									}
								}
								itemList.clear();
							}
							else
							{
								itemList.clear();
								break;
							}
							i++;
						}
	
						i = 0;
						while (true)
						{
							soid = "1.3.6.1.4.1.3854.1.2.2.1.17.1.5." + i;
							err = this.cli.v1GetRequest(addr, community, soid, itemList); //
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
								{
									if (iVal.value == 1)
									{
										soid = "1.3.6.1.4.1.3854.1.2.2.1.17.1.3." + i;
										reading = new SNMPReadingInfo();
										reading.setName(null);
										reading.setIndex(i);
										reading.setObjIdLen(SNMPUtil.oidText2PDU(soid, reading.getObjId(), 0));
										reading.setMulVal(1.0);
										reading.setInvVal(-1);
										reading.setReadingType(SMonitorReadingType.RHUMIDITY);
										reading.setValValid(false);
										reading.setCurrVal(0);
										agent.getReadingList().add(reading);
										itemList.clear();
	
										err = this.cli.v1GetRequest(addr, community, soid, itemList);
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
											{
												reading.setCurrVal(iVal.value * reading.getMulVal());
												reading.setValValid(true);
											}
										}
										itemList.clear();
	
										soid = "1.3.6.1.4.1.3854.1.2.2.1.17.1.1." + i;
										err = this.cli.v1GetRequest(addr, community, soid, itemList);
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (item.getValType() == 4 && item.getValLen() > 0)
											{
												reading.setName(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
											}
										}
										if (reading.getName() == null)
										{
											reading.setName("Humidity" + (i + 1));
										}
									}
								}
								itemList.clear();
							}
							else
							{
								itemList.clear();
								break;
							}
							i++;
						}
	
						i = 0;
						while (true)
						{
							soid = "1.3.6.1.4.1.3854.1.2.2.1.18.1.4." + i;
							err = this.cli.v1GetRequest(addr, community, soid, itemList); //
							if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
							{
								item = itemList.get(0);
								if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
								{
									if (iVal.value == 1)
									{
										soid = "1.3.6.1.4.1.3854.1.2.2.1.18.1.3." + i;
										reading = new SNMPReadingInfo();
										reading.setName(null);
										reading.setIndex(i);
										reading.setObjIdLen(SNMPUtil.oidText2PDU(soid, reading.getObjId(), 0));
										reading.setMulVal(1.0);
										reading.setInvVal(-1);
										reading.setReadingType(SMonitorReadingType.ONOFF);
										reading.setValValid(false);
										reading.setCurrVal(0);
										agent.getReadingList().add(reading);
										itemList.clear();
	
										err = this.cli.v1GetRequest(addr, community, soid, itemList);
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (SNMPUtil.valueToInt32(item.getValType(), item.getValBuff(), 0, item.getValLen(), iVal))
											{
												reading.setCurrVal(iVal.value * reading.getMulVal());
												reading.setValValid(true);
											}
										}
										itemList.clear();
	
										soid = "1.3.6.1.4.1.3854.1.2.2.1.18.1.1." + i;
										err = this.cli.v1GetRequest(addr, community, soid, itemList);
										if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
										{
											item = itemList.get(0);
											if (item.getValType() == 4 && item.getValLen() > 0)
											{
												reading.setName(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
											}
										}
										if (reading.getName() == null)
										{
											reading.setName("Dry Contact Switch" + (i + 1));
										}
									}
								}
								itemList.clear();
							}
							else
							{
								itemList.clear();
								break;
							}
							i++;
						}
					}
				}
				if (!found)
				{
					pduSize = SNMPUtil.oidText2PDU("1.3.6.1.4.1.311.1.1.3.1.1", oidPDU, 0); //workstation (Windows NT)
					if (SNMPUtil.oidStartsWith(agent.getObjId(), agent.getObjIdLen(), oidPDU, pduSize))
					{
						found = true;
						agent.setVendor("Microsoft");
	
						StringBuilder sb = new StringBuilder();
						sb.append("WindowsNT");
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.77.1.1.1.0", itemList); //comVersionMaj.0
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (item.getValType() == 4 && item.getValLen() > 0)
							{
								sb.append(' ');
								sb.append(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
							}
						}
						itemList.clear();
	
						err = this.cli.v1GetRequest(addr, community, "1.3.6.1.4.1.77.1.1.2.0", itemList); //comVersionMin.0
						if (err == SNMPErrorStatus.NOERROR && itemList.size() == 1)
						{
							item = itemList.get(0);
							if (item.getValType() == 4 && item.getValLen() > 0)
							{
								sb.append('.');
								sb.append(new String(item.getValBuff(), 0, item.getValLen(), StandardCharsets.UTF_8));
							}
						}
						itemList.clear();
						agent.setModel(sb.toString());
					}
				}
			}
		}
		return agent;
	}

	public int addAgents(InetAddress addr, String community, List<SNMPAgentInfo> agentList, boolean scanIP)
	{
		SNMPAgentInfo agent;
		int ret = 0;
		if (addr instanceof Inet4Address)
		{
			if (addr.isMulticastAddress())
			{
				InetAddress agentAddr;
				List<InetAddress> addrList = new ArrayList<InetAddress>();
				int i;
				int j;
				this.cli.v1ScanGetRequest(addr, community, "1.3.6.1.2.1.1.1.0", addrList, 3000, scanIP);
				i = 0;
				j = addrList.size();
				while (i < j)
				{
					agentAddr = addrList.get(i);
					agent = this.addAgent(agentAddr, community);
					if (agent != null)
					{
						agentList.add(agent);
						ret++;
					}
					i++;
				}
				return ret;
			}
		}
		agent = this.addAgent(addr, community);
		if (agent != null)
		{
			agentList.add(agent);
			ret++;
		}
		return ret;
	}

/*	public static SMonDevRecord2 agent2Record(SNMPAgentInfo agent, SharedLong cliId)
	{
		cliId = agent2CliId(agent);

		Map<Integer, Integer> readingIdMap = new HashMap<Integer, Integer>();
		int currId;
		SNMPReadingInfo reading;
		ZonedDateTime dt = ZonedDateTime.now();
		rec.recTime = dt.ToTicks();
		rec.recvTime = rec.recTime;
		rec.ndigital = 0;
		rec.nreading = agent.readingList.size();
		rec.nOutput = 0;
		rec.profileId = 5;
		rec.digitalVals = 0;
		MemClear(rec.readings, sizeof(SSWR::SMonitor::ISMonitorCore::ReadingInfo) * SMONITORCORE_DEVREADINGCNT);
		UOSInt i = 0;
		UOSInt j = agent.readingList.size();
		while (i < j)
		{
			reading = agent.readingList.get(i);
			WriteInt16(&rec.readings[i].status[0], reading.index);
			WriteInt16(&rec.readings[i].status[2], SSWR::SMonitor::SAnalogSensor::ST_SNMP);
			currId = readingIdMap.Get((UInt32)reading.index);
			WriteInt16(&rec.readings[i].status[4], currId);
			readingIdMap.Put((UInt32)reading.index, currId + 1);
			if (reading.valValid)
			{
				WriteInt16(&rec.readings[i].status[6], reading.readingType);
			}
			else
			{
				WriteInt16(&rec.readings[i].status[6], 0);
			}
			rec.readings[i].reading = reading.currVal;
			i++;
		}
	}*/

	public static long agent2CliId(SNMPAgentInfo agent)
	{
		byte[] ibuff = new byte[8];
		ByteTool.writeMInt16(ibuff, 0, 161);
		ByteTool.copyArray(ibuff, 2, agent.getMac(), 0, 6);
		return ByteTool.readMInt64(ibuff, 0);
	}
}
