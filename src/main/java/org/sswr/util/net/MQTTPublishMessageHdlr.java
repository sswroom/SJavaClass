package org.sswr.util.net;

import jakarta.annotation.Nonnull;

public interface MQTTPublishMessageHdlr
{
	public void onPublishMessage(@Nonnull String topic, @Nonnull byte[] buff, int buffOfst, int buffSize);
}
