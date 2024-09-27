package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SNMPReadingInfo
{
	private String name;
	private int index;
	private byte[] objId;
	private int objIdLen;
	private double mulVal;
	private int invVal;
	private SMonitorReadingType readingType;
	private boolean valValid;
	private double currVal;

	public SNMPReadingInfo()
	{
		this.objId = new byte[64];
		this.readingType = SMonitorReadingType.UNKNOWN;
	}

	@Nullable
	public String getName() {
		return this.name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public double getMulVal() {
		return this.mulVal;
	}

	public void setMulVal(double mulVal) {
		this.mulVal = mulVal;
	}

	public int getInvVal() {
		return this.invVal;
	}

	public void setInvVal(int invVal) {
		this.invVal = invVal;
	}

	@Nonnull
	public SMonitorReadingType getReadingType() {
		return this.readingType;
	}

	public void setReadingType(@Nonnull SMonitorReadingType readingType) {
		this.readingType = readingType;
	}

	public boolean isValValid() {
		return this.valValid;
	}

	public boolean getValValid() {
		return this.valValid;
	}

	public void setValValid(boolean valValid) {
		this.valValid = valValid;
	}

	public double getCurrVal() {
		return this.currVal;
	}

	public void setCurrVal(double currVal) {
		this.currVal = currVal;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SNMPReadingInfo)) {
			return false;
		}
		SNMPReadingInfo sNMPReadingInfo = (SNMPReadingInfo) o;
		return Objects.equals(name, sNMPReadingInfo.name) && index == sNMPReadingInfo.index && Objects.equals(objId, sNMPReadingInfo.objId) && objIdLen == sNMPReadingInfo.objIdLen && mulVal == sNMPReadingInfo.mulVal && invVal == sNMPReadingInfo.invVal && Objects.equals(readingType, sNMPReadingInfo.readingType) && valValid == sNMPReadingInfo.valValid && currVal == sNMPReadingInfo.currVal;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, index, objId, objIdLen, mulVal, invVal, readingType, valValid, currVal);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

}
