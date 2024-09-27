package org.sswr.util.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
	private @Nonnull List<SNMPReadingInfo> readingList;


	public SNMPAgentInfo() {
		this.objId = new byte[64];
		this.mac = new byte[6];
		this.readingList = new ArrayList<SNMPReadingInfo>();
	}

	@Nullable
	public InetAddress getAddr() {
		return this.addr;
	}

	public void setAddr(@Nullable InetAddress addr) {
		this.addr = addr;
	}

	@Nullable
	public String getCommunity() {
		return this.community;
	}

	public void setCommunity(@Nullable String community) {
		this.community = community;
	}

	@Nonnull
	public byte[] getObjId() {
		return this.objId;
	}

	public void setObjId(@Nonnull byte[] objId, int ofst, int len) {
		ByteTool.copyArray(this.objId, 0, objId, ofst, len);
		this.objIdLen = len;
	}

	public int getObjIdLen() {
		return this.objIdLen;
	}

	public void setObjIdLen(int objIdLen) {
		this.objIdLen = objIdLen;
	}

	@Nullable
	public String getDescr() {
		return this.descr;
	}

	public void setDescr(@Nullable String descr) {
		this.descr = descr;
	}

	@Nullable
	public String getContact() {
		return this.contact;
	}

	public void setContact(@Nullable String contact) {
		this.contact = contact;
	}

	@Nullable
	public String getName() {
		return this.name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	@Nullable
	public String getLocation() {
		return this.location;
	}

	public void setLocation(@Nullable String location) {
		this.location = location;
	}

	@Nullable
	public String getModel() {
		return this.model;
	}

	public void setModel(@Nullable String model) {
		this.model = model;
	}

	@Nullable
	public String getVendor() {
		return this.vendor;
	}

	public void setVendor(@Nullable String vendor) {
		this.vendor = vendor;
	}

	@Nullable
	public String getCpuName() {
		return this.cpuName;
	}

	public void setCpuName(@Nullable String cpuName) {
		this.cpuName = cpuName;
	}

	@Nonnull
	public byte[] getMac() {
		return this.mac;
	}

	public void setMac(@Nonnull byte[] mac, int ofst) {
		ByteTool.copyArray(this.mac, 0, mac, ofst, 6);
	}

	@Nonnull
	public List<SNMPReadingInfo> getReadingList() {
		return this.readingList;
	}

	public void setReadingList(@Nonnull List<SNMPReadingInfo> readingList) {
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
