package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

public class SNMPBindingItem
{
	private byte[] oid;
	private int oidLen;
	private byte valType;
	private int valLen;
	private byte[] valBuff;

	public SNMPBindingItem()
	{
		this.oid = new byte[64];
	}

	public byte[] getOid() {
		return this.oid;
	}

	public int getOidLen() {
		return this.oidLen;
	}

	public void setOidLen(int oidLen) {
		this.oidLen = oidLen;
	}

	public void setOid(byte[] buff, int ofst, int len)
	{
		ByteTool.copyArray(this.oid, 0, buff, ofst, len);
		this.oidLen = len;
	}

	public byte getValType() {
		return this.valType;
	}

	public void setValType(byte valType) {
		this.valType = valType;
	}

	public int getValLen() {
		return this.valLen;
	}

	public void setValLen(int valLen) {
		this.valLen = valLen;
	}

	public byte[] getValBuff() {
		return this.valBuff;
	}

	public void setValBuff(byte[] valBuff) {
		this.valBuff = valBuff;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SNMPBindingItem)) {
			return false;
		}
		SNMPBindingItem sNMPBindingItem = (SNMPBindingItem) o;
		return Objects.equals(oid, sNMPBindingItem.oid) && oidLen == sNMPBindingItem.oidLen && valType == sNMPBindingItem.valType && valLen == sNMPBindingItem.valLen && Objects.equals(valBuff, sNMPBindingItem.valBuff);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oid, oidLen, valType, valLen, valBuff);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
