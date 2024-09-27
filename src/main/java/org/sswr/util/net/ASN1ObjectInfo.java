package org.sswr.util.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ASN1ObjectInfo
{
	private String objectName;
	private String typeName;
	private String typeVal;
	private @Nonnull byte []oid;
	private int oidLen;
	private @Nonnull List<String> valName;
	private @Nonnull List<String> valCont;

	public ASN1ObjectInfo()
	{
		oid = new byte[32];
		this.oidLen = 0;
		this.valName = new ArrayList<String>();
		this.valCont = new ArrayList<String>();
	}

	@Nullable
	public String getObjectName() {
		return this.objectName;
	}

	public void setObjectName(@Nullable String objectName) {
		this.objectName = objectName;
	}

	@Nullable
	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(@Nullable String typeName) {
		this.typeName = typeName;
	}

	@Nullable
	public String getTypeVal() {
		return this.typeVal;
	}

	public void setTypeVal(@Nullable String typeVal) {
		this.typeVal = typeVal;
	}

	@Nonnull
	public byte []getOid() {
		return this.oid;
	}

	public byte getOidByte(int ofst)
	{
		return this.oid[ofst];
	}

	public void setOid(int ofst, byte v)
	{
		this.oid[ofst] = v;
	}

	public int getOidLen()
	{
		return this.oidLen;
	}

	public void setOidLen(int oidLen)
	{
		this.oidLen = oidLen;
	}

	public void appendOid(byte oidByte)
	{
		this.oid[this.oidLen++] = oidByte;
	}

	@Nonnull
	public List<String> getValName() {
		return this.valName;
	}

	public void setValName(@Nonnull List<String> valName) {
		this.valName = valName;
	}

	@Nonnull
	public List<String> getValCont() {
		return this.valCont;
	}

	public void setValCont(@Nonnull List<String> valCont) {
		this.valCont = valCont;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ASN1ObjectInfo)) {
			return false;
		}
		ASN1ObjectInfo sNMPObjectInfo = (ASN1ObjectInfo) o;
		return Objects.equals(objectName, sNMPObjectInfo.objectName) && Objects.equals(typeName, sNMPObjectInfo.typeName) && Objects.equals(typeVal, sNMPObjectInfo.typeVal) && Objects.equals(oid, sNMPObjectInfo.oid) && Objects.equals(valName, sNMPObjectInfo.valName) && Objects.equals(valCont, sNMPObjectInfo.valCont);
	}

	@Override
	public int hashCode() {
		return Objects.hash(objectName, typeName, typeVal, oid, valName, valCont);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

	public void copyOidFrom(@Nonnull ASN1ObjectInfo obj)
	{
		ByteTool.copyArray(this.oid, 0, obj.oid, 0, obj.oidLen);
		this.oidLen = obj.oidLen;
	}
}
