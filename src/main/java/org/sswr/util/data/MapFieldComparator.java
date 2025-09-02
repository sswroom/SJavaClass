package org.sswr.util.data;

import java.util.Comparator;
import java.util.Map;

import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;

public class MapFieldComparator implements Comparator<Map<String, Object>>
{
	private String fieldNames[];
	private int dirs[];
	public MapFieldComparator(@Nonnull String compareConds)
	{
		String conds[] = StringUtil.split(compareConds, ",");
		String cond[];
		dirs = new int[conds.length];
		fieldNames = new String[conds.length];
		int i = 0;
		int j = conds.length;
		while (i < j)
		{
			cond = StringUtil.split(conds[i].trim(), " ");
			dirs[i] = 1;
			if (cond.length > 2)
			{
				throw new IllegalArgumentException("\""+conds[i]+"\" is not supported");
			}
			else if (cond.length == 2)
			{
				cond[1] = cond[1].toUpperCase();
				if (cond[1].equals("ASC"))
				{
					dirs[i] = 1;
				}
				else if (cond[1].equals("DESC"))
				{
					dirs[i] = -1;
				}
				else
				{
					throw new IllegalArgumentException("\""+conds[i]+"\" is not supported");
				}
			}
			fieldNames[i] = cond[0];
			i++;
		}
	}
	@Override
	public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
		int i = 0;
		int j = this.fieldNames.length;
		int k;
		while (i < j)
		{
			k = DataTools.objectCompare(arg0.get(fieldNames[i]), arg1.get(fieldNames[i])) * this.dirs[i];
			if (k != 0)
			{
				return k;
			}
			i++;
		}
		return 0;
	}

	@Nonnull
	public String toOrderClause(@Nonnull Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j  = this.fieldNames.length;
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			DBColumnInfo col = colsMap.get(this.fieldNames[i]);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			if (this.dirs[i] == -1)
			{
				sb.append(" desc");
			}
			i++;
		}
		return sb.toString();
	}
}
