package org.sswr.util.data;

import java.sql.Timestamp;

public class ArrayListTS extends SortableArrayList<Timestamp>
{
	@Override
	public int compare(Timestamp a, Timestamp b) {
		return a.compareTo(b);
	}

	@Override
	public double toValue(Timestamp v) {
		return (double)(v.getTime() / 1000) + (v.getNanos() / 1000000000.0);
	}
}
