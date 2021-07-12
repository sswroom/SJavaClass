package org.sswr.util.net;

public interface MQTTEventHdlr extends MQTTPublishMessageHdlr
{
	public void onDisconnect();
}
