package org.sswr.util.net;

import jakarta.annotation.Nonnull;

public enum MQTTConnectStatus
{
	TIMEDOUT,
	ACCEPTED,
	VERERR,
	CLIENTID_ERR,
	SVR_UNAVAIL,
	BAD_LOGIN,
	NOT_AUTH;

	public static int toInt(@Nonnull MQTTConnectStatus status)
	{
		switch (status)
		{
			case TIMEDOUT:
				return -1;
			case ACCEPTED:
				return 0;
			case VERERR:
				return 1;
			case CLIENTID_ERR:
				return 2;
			case SVR_UNAVAIL:
				return 3;
			case BAD_LOGIN:
				return 4;
			case NOT_AUTH:
				return 5;
		}
		return 0;
	}

	@Nonnull
	public static MQTTConnectStatus fromByte(byte b)
	{
		switch (b)
		{
			case 0:
				return ACCEPTED;
			case 1:
				return VERERR;
			case 2:
				return CLIENTID_ERR;
			case 3:
				return SVR_UNAVAIL;
			case 4:
				return BAD_LOGIN;
			case 5:
				return NOT_AUTH;
			default:
				return TIMEDOUT;
		}
	}
}
