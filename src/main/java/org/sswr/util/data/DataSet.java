package org.sswr.util.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.sswr.util.basic.ArrayListInt32;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class DataSet {
	private @Nonnull Object[] items;
	private int capacity;
	private int itemCnt;

	public DataSet()
	{
		this.capacity = 128;
		this.itemCnt = 0;
		this.items = new Object[this.capacity << 1];
	}
	
	public void addItem(@Nullable Object key, @Nullable Object value)
	{
		int i;
		if (this.itemCnt >= this.capacity)
		{
			this.capacity = this.capacity << 1;
			Object[] newItems = new Object[this.capacity << 1];
			i = 0;
			int j = this.itemCnt * 2;
			while (i < j)
			{
				newItems[i] = this.items[i];
				i++;
			}
			this.items = newItems;
		}
		i = this.itemCnt;
		this.items[i << 1] = key;
		this.items[(i << 1) + 1] = value;
		this.itemCnt = i + 1;		
	}

	public int getCount()
	{
		return this.itemCnt;
	}

	public @Nullable Object getKey(int index)
	{
		if (index >= 0 && index < itemCnt)
			return items[index << 1];
		return null;
	}

	public @Nullable Object getValue(int index)
	{
		if (index >= 0 && index < itemCnt)
			return items[(index << 1) + 1];
		return null;
	}

	public DataSetMonthGrouper groupKeyByMonth()
	{
		return new DataSetMonthGrouper(this);
	}

	static class ValueItem
	{
		public Object item;
		public int count;
	}

	@Nonnull
	public ArrayListInt32 valueCounts()
	{
		VariItemHashCalc hashCalc = new VariItemHashCalc();
		HashMap<Long, ValueItem> itemMap = new HashMap<Long, ValueItem>();
		ValueItem vitem;
		long hash;
		Object item;
		int i = 0;
		int j = this.getCount();
		while (i < j)
		{
			if ((item = this.getValue(i)) != null)
			{
				hash = hashCalc.hash(item);
				if ((vitem = itemMap.get(hash)) == null)
				{
					vitem = new ValueItem();
					vitem.item = item;
					vitem.count = 1;
					itemMap.put(hash, vitem);
				}
				else
				{
					while (!vitem.item.equals(item))
					{
						hash++;
						if ((vitem = itemMap.get(hash)) == null)
						{
							vitem = new ValueItem();
							vitem.item = item;
							vitem.count = 0;
							itemMap.put(hash, vitem);
							break;
						}
					}
					vitem.count++;
				}
			}
			else
			{
				System.out.println("Failed to get the value");
			}
			i++;
		}
		ArrayListInt32 result = new ArrayListInt32();
		Iterator<Entry<Long, ValueItem>> it = itemMap.entrySet().iterator();
		Entry<Long, ValueItem> entry;
		while (it.hasNext())
		{
			entry = it.next();
			vitem = entry.getValue();
			result.add(vitem.count);
			i++;
		}
		return result;
	}

	public @Nonnull DataSet valueCountsAsDS()
	{
		VariItemHashCalc hashCalc = new VariItemHashCalc();
		HashMap<Long, ValueItem> itemMap = new HashMap<Long, ValueItem>();
		ValueItem vitem;
		long hash;
		Object item;
		int i = 0;
		int j = this.getCount();
		while (i < j)
		{
			if ((item = this.getValue(i)) != null)
			{
				hash = hashCalc.hash(item);
				if ((vitem = itemMap.get(hash)) == null)
				{
					vitem = new ValueItem();
					vitem.item = item;
					vitem.count = 1;
					itemMap.put(hash, vitem);
				}
				else
				{
					while (!vitem.item.equals(item))
					{
						hash++;
						if ((vitem = itemMap.get(hash)) == null)
						{
							vitem = new ValueItem();
							vitem.item = item;
							vitem.count = 0;
							itemMap.put(hash, vitem);
							break;
						}
					}
					vitem.count++;
				}
			}
			else
			{
				System.out.println("Failed to get the value");
			}
			i++;
		}
		DataSet newDS;
		newDS = new DataSet();
		Iterator<Entry<Long, ValueItem>> it = itemMap.entrySet().iterator();
		Entry<Long, ValueItem> ent;
		while (it.hasNext())
		{
			ent = it.next();
			vitem = ent.getValue();
			newDS.addItem(vitem.item, vitem.count);
			i++;
		}
		return newDS;
	}

	public void sortByValue(@Nonnull Comparator<Object> comparator)
	{
		this.sort(this.items, 1, this.items, 0, 0, this.itemCnt - 1, comparator);
	}

	public void sortByKey(@Nonnull Comparator<Object> comparator)
	{
		this.sort(this.items, 0, this.items, 1, 0, this.itemCnt - 1, comparator);
	}

	public void sortByValue()
	{
		VariItemComparator comparator = new VariItemComparator();
		this.sortByValue(comparator);
	}

	public void sortByValueInv()
	{
		VariItemComparatorInv comparator = new VariItemComparatorInv();
		this.sortByValue(comparator);
	}

	public void sortByKey()
	{
		VariItemComparator comparator = new VariItemComparator();
		this.sortByKey(comparator);
	}

	public void sortByKeyInv()
	{
		VariItemComparatorInv comparator = new VariItemComparatorInv();
		this.sortByKey(comparator);
	}

	private void presort(@Nonnull Object[] keyArr, int keyIndex, @Nonnull Object[] valArr, int valIndex, int left, int right, @Nonnull Comparator<Object> comparator)
	{
		Object temp = keyArr[keyIndex + left * 2];
		Object temp2;
		Object v;
		while (left < right)
		{
			temp = keyArr[keyIndex + left * 2];
			temp2 = keyArr[keyIndex + right * 2];
			if (comparator.compare(temp, temp2) > 0)
			{
				keyArr[keyIndex + left * 2] = temp2;
				keyArr[keyIndex + right * 2] = temp;
				v = valArr[valIndex + left * 2];
				valArr[valIndex + left * 2] = valArr[valIndex + right * 2];
				valArr[valIndex + right * 2] = v;
			}
			left++;
			right--;
		}
	}

	private void sort(@Nonnull Object[] keyArr, int keyIndex, @Nonnull Object[] valArr, int valIndex, int firstIndex, int lastIndex, @Nonnull Comparator<Object> comparator)
	{
		int[] levi = new int[32768];
		int[] desni = new int[32768];
		int index;
		int i;
		int left;
		int right;
		Object meja;
		Object mejaV;
		int left1;
		int right1;
		Object temp;
		Object tempV;

		this.presort(keyArr, keyIndex, valArr, valIndex, firstIndex, lastIndex, comparator);

		index = 0;
		levi[index] = firstIndex;
		desni[index] = lastIndex;

		while ( index >= 0 )
		{
			left = levi[index];
			right = desni[index];
			i = right - left;
			if (i <= 0)
			{
				index--;
			}
			else if (i <= 64)
			{
				this.iSortB(keyArr, keyIndex, valArr, valIndex, left, right, comparator);
				index--;
			}
			else
			{
				meja = keyArr[keyIndex + ((left + right) & ~1) ];
				mejaV = valArr[valIndex + ((left + right) & ~1) ];
				left1 = left;
				right1 = right;
				while (true)
				{
					while (comparator.compare(keyArr[keyIndex + right1 * 2], meja) >= 0)
					{
						if (--right1 < left1)
							break;
					}
					while (comparator.compare(keyArr[keyIndex + left1 * 2], meja) < 0)
					{
						if (++left1 > right1)
							break;
					}
					if (left1 > right1)
						break;

					temp = keyArr[keyIndex + right1 * 2];
					tempV = valArr[valIndex + right1 * 2];
					keyArr[keyIndex + right1 * 2] = keyArr[keyIndex + left1 * 2];
					valArr[valIndex + (right1--) * 2] = valArr[valIndex + left1 * 2];
					keyArr[keyIndex + left1 * 2] = temp;
					valArr[valIndex + (left1++) * 2] = tempV;
				}
				if (left1 == left)
				{
					keyArr[keyIndex + (left + right) & ~1] = keyArr[keyIndex + left * 2];
					valArr[valIndex + (left + right) & ~1] = valArr[valIndex + left * 2];
					keyArr[keyIndex + left * 2] = meja;
					valArr[valIndex + left * 2] = mejaV;
					levi[index] = left + 1;
					desni[index] = right;
				}
				else
				{
					desni[index] = --left1;
					right1++;
					index++;
					levi[index] = right1;
					desni[index] = right;
				}
			}
		}
	}

	private void iSortB(@Nonnull Object[] keyArr, int keyIndex, @Nonnull Object[] valArr, int valIndex, int left, int right, Comparator<Object> comparator)
	{
		int i;
		int j;
		int k;
		int l;
		Object temp;
		Object temp1;
		Object temp2;
		Object val2;
		temp1 = keyArr[keyIndex + left * 2];
		i = left + 1;
		while (i <= right)
		{
			temp2 = keyArr[keyIndex + i * 2];
			if ( comparator.compare(temp1, temp2) > 0)
			{
				val2 = valArr[valIndex + i * 2];
				j = left;
				k = i - 1;
				while (j <= k)
				{
					l = (j + k) >> 1;
					temp = keyArr[keyIndex + l * 2];
					if (comparator.compare(temp, temp2) > 0)
					{
						k = l - 1;
					}
					else
					{
						j = l + 1;
					}
				}
				k = i;
				while (k > j)
				{
					k--;
					keyArr[keyIndex + k * 2 + 2] = keyArr[keyIndex + k * 2];
					valArr[valIndex + k * 2 + 2] = valArr[valIndex + k * 2];
				}
				keyArr[keyIndex + j * 2] = temp2;
				valArr[valIndex + j * 2] = val2;
			}
			else
			{
				temp1 = temp2;
			}
			i++;
		}
	}
}
