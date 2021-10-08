package org.sswr.util.net;

public interface MQTTClient
{
	public void subscribe(String topic, MQTTPublishMessageHdlr hdlr);
	public boolean publish(String topic, String message);
	public void handleEvents(MQTTEventHdlr hdlr);
}
