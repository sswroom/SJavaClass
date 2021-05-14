package org.sswr.util.media;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;

public class LUT<T>
{
	private String remark;
	private int inputCh;
	private int inputLev;
	private int outputCh;
	private T luTable[];

	public LUT(int inputCh, int inputLev, int outputCh)
	{
		this.inputCh = inputCh;
		this.inputLev = inputLev;
		this.outputCh = outputCh;
		this.remark = null;
		int tableSize = 1;
		int i;
		i = inputCh;
		while (i-- > 0)
		{
			tableSize *= inputLev;
		}
		tableSize = tableSize * this.outputCh;
		Class<?> persistentClass = (Class<?>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		@SuppressWarnings("unchecked")
		T[] arr = (T[])Array.newInstance(persistentClass, tableSize);
		this.luTable = arr;
	}

	public void setRemark(String remark)
	{
		this.remark = remark;
	}

	public String getRemark()
	{
		return this.remark;
	}

	public int getInputCh()
	{
		return this.inputCh;
	}

	public int getInputLevel()
	{
		return this.inputLev;
	}

	public int getOutputCh()
	{
		return this.outputCh;
	}

	public T[] getTable()
	{
		return this.luTable;
	}

	public int[] getValueUInt8(int inputVals[], int index)
	{
		//////////////////////////
		return null;
	}

	public int[] getValueUInt16(int inputVals[], int index)
	{
		//////////////////////////
		return null;
	}

	public float[] getValueSingle(int inputVals[], int index)
	{
		//////////////////////////
		return null;
	}

	public LUT<T> clone()
	{
		LUT<T> newLut = new LUT<T>(this.inputCh, this.inputLev, this.outputCh);
		if (this.remark != null)
		{
			newLut.setRemark(this.remark);
		}
		int tableSize = 1;
		int i;
		tableSize = 1;
		i = this.inputCh;
		while (i-- > 0)
		{
			tableSize *= inputLev;
		}
		tableSize = tableSize * this.outputCh;
		i = tableSize;
		while (i-- > 0)
		{
			newLut.luTable[i] = this.luTable[i];
		}
		return newLut;
	}

	public boolean equals(LUT<T> lut)
	{
		if (this.inputLev != lut.inputLev)
			return false;
		if (this.outputCh != lut.outputCh || this.inputCh != lut.inputCh)
			return false;
		int i;
		int j = 1;
		i = inputCh;
		while (i-- > 0)
		{
			j *= this.inputLev;
		}
		j = j * this.outputCh;
		T stab[] = this.luTable;
		T dtab[] = lut.luTable;
		while (i < j)
		{
			if (stab[i] != dtab[i])
				return false;
			i++;
		}
		return true;
	}
}
