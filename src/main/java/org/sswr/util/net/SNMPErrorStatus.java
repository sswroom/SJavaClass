package org.sswr.util.net;

import jakarta.annotation.Nullable;

public enum SNMPErrorStatus
{
	NOERROR,
	TOOBIG,
	NOSUCHNAME,
	BADVALUE,
	READONLY,
	GENERROR,
	NORESP, //-1
	UNKRESP; //-2

	@Nullable
	public static SNMPErrorStatus getStatus(int val)
	{
		if (val == -2)
		{
			return UNKRESP;
		}
		else if (val == -1)
		{
			return NORESP;
		}
		else if (val < 6)
		{
			return values()[val];
		}
		else
		{
			return null;
		}
	}
}
