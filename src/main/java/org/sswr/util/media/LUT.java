package org.sswr.util.media;

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

	public abstract void getValueUInt8(int inputVals[], int inputIndex, int outVals[], int outputIndex);
	public abstract void getValueUInt16(int inputVals[], int inputIndex, int outVals[], int outputIndex);
	public abstract void getValueSingle(int inputVals[], int inputIndex, float outVals[], int outputIndex);

	public abstract LUT clone();
	public abstract boolean tableEquals(LUT lut);

	public boolean equals(LUT lut)
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
