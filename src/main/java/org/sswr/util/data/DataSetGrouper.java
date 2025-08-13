package org.sswr.util.data;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;

public abstract class DataSetGrouper<K>
{
	protected @Nonnull DataSet ds;
	public DataSetGrouper(@Nonnull DataSet ds)
	{
		this.ds = ds;
	}

	protected abstract @Nonnull SortableArrayList<K> createKeyIndex();
	protected abstract int getKeyIndex(@Nonnull SortableArrayList<K> keyIndex, int dataIndex);
	public void sum(@Nonnull List<TwinItem<K, Double>> result)
	{
		SortableArrayList<K> indexVal = this.createKeyIndex();
		ArrayList<Double> sumVal = new ArrayList<Double>();
		while (sumVal.size() < indexVal.size())
		{
			sumVal.add(Double.valueOf(0));
		}
		Object item;
		int i = 0;
		int j = this.ds.getCount();
		int k;
		while (i < j)
		{
			if ((item = this.ds.getValue(i)) != null)
			{
				k = this.getKeyIndex(indexVal, i);
				sumVal.set(k, sumVal.get(k) + VariItemUtil.asF64(item));
			}
			else
			{
				System.out.println("Failed to get the value");
			}
			i++;
		}
		i = 0;
		j = indexVal.size();
		while (i < j)
		{
			result.add(new TwinItem<K, Double>(indexVal.get(i), sumVal.get(i)));
			i++;
		}
	}

	public void count(@Nonnull List<TwinItem<K, Integer>> result)
	{
		SortableArrayList<K> indexVal = this.createKeyIndex();
		List<Integer> countVal = new ArrayList<Integer>();
		while (countVal.size() < indexVal.size())
		{
			countVal.add(0);
		}
		int i = 0;
		int j = this.ds.getCount();
		int k;
		while (i < j)
		{
			if (this.ds.getValue(i) != null)
			{
				k = this.getKeyIndex(indexVal, i);
				countVal.set(k, countVal.get(k) + 1);
			}
			else
			{
				System.out.println("Failed to get the value");
			}
			i++;
		}
		i = 0;
		j = indexVal.size();
		while (i < j)
		{
			result.add(new TwinItem<K, Integer>(indexVal.get(i), countVal.get(i)));
			i++;
		}
	}
}
