package org.sswr.util.net;

public interface MQTTPublishMessageHdlr
{
	public void onPublishMessage(String topic, byte[] buff, int buffOfst, int buffSize);
}
