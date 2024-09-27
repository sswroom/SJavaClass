package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SNMPTrapInfo
{
	private byte[] entOID;
	private int entOIDLen;
	private int agentIPv4;
	private int genericTrap;
	private int specificTrap;
	private int timeStamp;
	private String community;

	public SNMPTrapInfo()
	{
		this.entOID = new byte[64];
	}

	@Nonnull
	public byte[] getEntOID() {
		return this.entOID;
	}

	public void setEntOID(@Nonnull byte[] entOID, int entOIDOfst, int entOIDLen) {
		ByteTool.copyArray(this.entOID, 0, entOID, entOIDOfst, entOIDLen);
		this.entOIDLen = entOIDLen;
	}

	public int getEntOIDLen() {
		return this.entOIDLen;
	}

	public void setEntOIDLen(int entOIDLen) {
		this.entOIDLen = entOIDLen;
	}

	public int getAgentIPv4() {
		return this.agentIPv4;
	}

	public void setAgentIPv4(int agentIPv4) {
		this.agentIPv4 = agentIPv4;
	}

	public int getGenericTrap() {
		return this.genericTrap;
	}

	public void setGenericTrap(int genericTrap) {
		this.genericTrap = genericTrap;
	}

	public int getSpecificTrap() {
		return this.specificTrap;
	}

	public void setSpecificTrap(int specificTrap) {
		this.specificTrap = specificTrap;
	}

	public int getTimeStamp() {
		return this.timeStamp;
	}

	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Nullable
	public String getCommunity() {
		return this.community;
	}

	public void setCommunity(@Nullable String community) {
		this.community = community;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SNMPTrapInfo)) {
			return false;
		}
		SNMPTrapInfo sNMPTrapInfo = (SNMPTrapInfo) o;
		return Objects.equals(entOID, sNMPTrapInfo.entOID) && entOIDLen == sNMPTrapInfo.entOIDLen && agentIPv4 == sNMPTrapInfo.agentIPv4 && genericTrap == sNMPTrapInfo.genericTrap && specificTrap == sNMPTrapInfo.specificTrap && timeStamp == sNMPTrapInfo.timeStamp && Objects.equals(community, sNMPTrapInfo.community);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entOID, entOIDLen, agentIPv4, genericTrap, specificTrap, timeStamp, community);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
