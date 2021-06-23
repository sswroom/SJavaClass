package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.DataTools;

public class MQTTPacketInfo
{
	private byte packetType;
	private byte[] content;


	public MQTTPacketInfo() {
	}

	public MQTTPacketInfo(byte packetType, byte[] content) {
		this.packetType = packetType;
		this.content = content;
	}

	public byte getPacketType() {
		return this.packetType;
	}

	public void setPacketType(byte packetType) {
		this.packetType = packetType;
	}

	public byte[] getContent() {
		return this.content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof MQTTPacketInfo)) {
			return false;
		}
		MQTTPacketInfo mQTTPacketInfo = (MQTTPacketInfo) o;
		return packetType == mQTTPacketInfo.packetType && Objects.equals(content, mQTTPacketInfo.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(packetType, content);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
