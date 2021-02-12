package org.sswr.util.data;

import java.sql.Timestamp;
import java.util.Comparator;

public class TimestampComparer implements Comparator<Timestamp>
{
	@Override
	public int compare(Timestamp arg0, Timestamp arg1) {
		return arg0.compareTo(arg1);
	}
}
