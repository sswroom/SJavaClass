package org.sswr.util.net;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface MQTTClient
{
	public void subscribe(@Nonnull String topic, @Nullable MQTTPublishMessageHdlr hdlr);
	public boolean publish(@Nonnull String topic, @Nonnull String message);
	public void handleEvents(@Nonnull MQTTEventHdlr hdlr);
}
