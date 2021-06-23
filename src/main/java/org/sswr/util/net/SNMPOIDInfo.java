package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.DataTools;

public class SNMPOIDInfo
{
	private String name;
	private int len;
	private byte[] oid;

	public SNMPOIDInfo() {
	}

	public SNMPOIDInfo(String name, int len, byte[] oid) {
		this.name = name;
		this.len = len;
		this.oid = oid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLen() {
		return this.len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public byte[] getOid() {
		return this.oid;
	}

	public void setOid(byte[] oid) {
		this.oid = oid;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SNMPOIDInfo)) {
			return false;
		}
		SNMPOIDInfo sNMPOIDInfo = (SNMPOIDInfo) o;
		return Objects.equals(name, sNMPOIDInfo.name) && len == sNMPOIDInfo.len && Objects.equals(oid, sNMPOIDInfo.oid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, len, oid);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

}
