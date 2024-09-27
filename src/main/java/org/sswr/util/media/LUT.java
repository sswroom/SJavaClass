package org.sswr.util.media;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class LUT
{
	protected String remark;
	protected int inputCh;
	protected int inputLev;
	protected int outputCh;

	protected int getTableSize()
	{
		int tableSize = 1;
		int i;
		i = inputCh;
		while (i-- > 0)
		{
			tableSize *= inputLev;
		}
		return tableSize * this.outputCh;
	}

	public LUT(int inputCh, int inputLev, int outputCh)
	{
		this.inputCh = inputCh;
		this.inputLev = inputLev;
		this.outputCh = outputCh;
		this.remark = null;
	}

	public void setRemark(@Nullable String remark)
	{
		this.remark = remark;
	}

	@Nullable
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

	public abstract void getValueUInt8(@Nonnull int inputVals[], int inputIndex, @Nonnull int outVals[], int outputIndex);
	public abstract void getValueUInt16(@Nonnull int inputVals[], int inputIndex, @Nonnull int outVals[], int outputIndex);
	public abstract void getValueSingle(@Nonnull int inputVals[], int inputIndex, @Nonnull float outVals[], int outputIndex);

	@Nonnull
	public abstract LUT clone();
	public abstract boolean tableEquals(@Nonnull LUT lut);

	public boolean equals(@Nonnull LUT lut)
	{
		if (!this.getClass().equals(lut.getClass()))
			return false;
		if (this.inputLev != lut.inputLev)
			return false;
		if (this.outputCh != lut.outputCh || this.inputCh != lut.inputCh)
			return false;
		return tableEquals(lut);
	}
}
