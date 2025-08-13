package org.sswr.util.data;

import java.sql.Timestamp;

import jakarta.annotation.Nonnull;

public class DataSetMonthGrouper extends DataSetGrouper<Timestamp>
{
	public DataSetMonthGrouper(@Nonnull DataSet ds)
	{
		super(ds);
	}

	protected @Nonnull SortableArrayList<Timestamp> createKeyIndex()
	{
		SortableArrayList<Timestamp> tsList = new ArrayListTS();
		int i = this.ds.getCount();
		if (i == 0)
		{
			return tsList;
		}
		boolean hasNull = false;
		Timestamp min = null;
		Timestamp max = null;
		Timestamp ts;
		Object item;
		while (i-- > 0)
		{
			if ((item = this.ds.getKey(i)) != null)
			{
				ts = VariItemUtil.asTimestamp(item);
				if (ts == null)
				{
					hasNull = true;
				}
				else if (min == null)
				{
					min = ts;
					max = ts;
				}
				else
				{
					if (ts.compareTo(min) < 0)
					{
						min = ts;
					}
					if (ts.compareTo(max) > 0)
					{
						max = ts;
					}
				}
			}
		}
		if (hasNull)
		{
			tsList.add(null);
		}
		if (min != null)
		{
			ts = DateTimeUtil.clearDayOfMonth(min);
			while (ts.compareTo(max) <= 0)
			{
				tsList.add(ts);
				ts = DateTimeUtil.addMonth(ts, 1);
			}
		}
		return tsList;
	}

	protected int getKeyIndex(@Nonnull SortableArrayList<Timestamp> keyIndex, int dataIndex)
	{
		Object item;
		if ((item = this.ds.getKey(dataIndex)) == null)
			return 0;
		Timestamp ts = VariItemUtil.asTimestamp(item);
		int i = keyIndex.sortedIndexOf(ts);
		if (i == -1)
			return 0;
		if (i < 0)
			return ~i - 1;
		return i;
	}
}
