package org.sswr.util.net;

import java.util.List;
import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

public class ASN1ObjectInfo
{
	private String objectName;
	private String typeName;
	private String typeVal;
	private byte []oid;
	private int oidLen;
	private List<String> valName;
	private List<String> valCont;

	public ASN1ObjectInfo()
	{
		oid = new byte[32];
		this.oidLen = 0;
	}

	public String getObjectName() {
		return this.objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeVal() {
		return this.typeVal;
	}

	public void setTypeVal(String typeVal) {
		this.typeVal = typeVal;
	}

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

	public List<String> getValName() {
		return this.valName;
	}

	public void setValName(List<String> valName) {
		this.valName = valName;
	}

	public List<String> getValCont() {
		return this.valCont;
	}

	public void setValCont(List<String> valCont) {
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

	public void copyOidFrom(ASN1ObjectInfo obj)
	{
		ByteTool.copyArray(this.oid, 0, obj.oid, 0, obj.oidLen);
		this.oidLen = obj.oidLen;
	}
}
