package org.sswr.util.net;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

public class SNMPAgentInfo
{
	private InetAddress addr;
	private String community;
	private byte[] objId;
	private int objIdLen;
	private String descr;
	private String contact;
	private String name;
	private String location;
	private String model;
	private String vendor;
	private String cpuName;
	private byte[] mac;
	private List<SNMPReadingInfo> readingList;


	public SNMPAgentInfo() {
		this.objId = new byte[64];
		this.mac = new byte[6];
	}

	public InetAddress getAddr() {
		return this.addr;
	}

	public void setAddr(InetAddress addr) {
		this.addr = addr;
	}

	public String getCommunity() {
		return this.community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public byte[] getObjId() {
		return this.objId;
	}

	public void setObjId(byte[] objId, int ofst, int len) {
		ByteTool.copyArray(this.objId, 0, objId, ofst, len);
		this.objIdLen = len;
	}

	public int getObjIdLen() {
		return this.objIdLen;
	}

	public void setObjIdLen(int objIdLen) {
		this.objIdLen = objIdLen;
	}

	public String getDescr() {
		return this.descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVendor() {
		return this.vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getCpuName() {
		return this.cpuName;
	}

	public void setCpuName(String cpuName) {
		this.cpuName = cpuName;
	}

	public byte[] getMac() {
		return this.mac;
	}

	public void setMac(byte[] mac, int ofst) {
		ByteTool.copyArray(this.mac, 0, mac, ofst, 6);
	}

	public List<SNMPReadingInfo> getReadingList() {
		return this.readingList;
	}

	public void setReadingList(List<SNMPReadingInfo> readingList) {
		this.readingList = readingList;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SNMPAgentInfo)) {
			return false;
		}
		SNMPAgentInfo sNMPAgentInfo = (SNMPAgentInfo) o;
		return Objects.equals(addr, sNMPAgentInfo.addr) && Objects.equals(community, sNMPAgentInfo.community) && Objects.equals(objId, sNMPAgentInfo.objId) && objIdLen == sNMPAgentInfo.objIdLen && Objects.equals(descr, sNMPAgentInfo.descr) && Objects.equals(contact, sNMPAgentInfo.contact) && Objects.equals(name, sNMPAgentInfo.name) && Objects.equals(location, sNMPAgentInfo.location) && Objects.equals(model, sNMPAgentInfo.model) && Objects.equals(vendor, sNMPAgentInfo.vendor) && Objects.equals(cpuName, sNMPAgentInfo.cpuName) && Objects.equals(mac, sNMPAgentInfo.mac) && Objects.equals(readingList, sNMPAgentInfo.readingList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(addr, community, objId, objIdLen, descr, contact, name, location, model, vendor, cpuName, mac, readingList);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

}
