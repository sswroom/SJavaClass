package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;

public class ASN1OIDInfo
{
	private String name;
	private int len;
	private byte[] oid;

	public ASN1OIDInfo() {
	}

	public ASN1OIDInfo(@Nonnull String name, int len, @Nonnull byte[] oid) {
		this.name = name;
		this.len = len;
		this.oid = oid;
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public int getLen() {
		return this.len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	@Nonnull
	public byte[] getOid() {
		return this.oid;
	}

	public void setOid(@Nonnull byte[] oid) {
		this.oid = oid;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ASN1OIDInfo)) {
			return false;
		}
		ASN1OIDInfo sNMPOIDInfo = (ASN1OIDInfo) o;
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
