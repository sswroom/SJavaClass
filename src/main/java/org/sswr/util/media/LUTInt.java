package org.sswr.util.media;

public class LUTInt extends LUT
{
	public enum DataFormat
	{
		UINT8,
		UINT16
	}

	private int luTable[];
	private DataFormat fmt;

	public LUTInt(int inputCh, int inputLev, int outputCh, DataFormat fmt)
	{
		super(inputCh, inputLev, outputCh);
		this.fmt = fmt;
		this.luTable = new int[getTableSize()];
	}

	public void getValueUInt8(int inputVals[], int inputIndex, int outVals[], int outputIndex)
	{
		int indexBase = 1;
		int index = 0;
		int ofst;
		int i;
		i = 0;
		while (i < this.inputCh)
		{
			index += inputVals[i + inputIndex] * indexBase;
			indexBase = indexBase * this.inputLev;
			i++;
		}
	
		if (this.fmt == DataFormat.UINT8)
		{
			ofst = index * this.outputCh;
			i = 0;
			while (i < this.outputCh)
			{
				outVals[i + outputIndex] = this.luTable[ofst + i];
				i++;
			}
		}
		else if (this.fmt == DataFormat.UINT16)
		{
			ofst = index * this.outputCh * 2;
			i = 0;
			while (i < this.outputCh)
			{
				outVals[i + outputIndex] = this.luTable[ofst + i] >> 8;
				i++;
			}
		}
	}

	public void getValueUInt16(int inputVals[], int inputIndex, int outVals[], int outputIndex)
	{
		int indexBase = 1;
		int index = 0;
		int ofst;
		int i;
		i = 0;
		while (i < this.inputCh)
		{
			index += inputVals[i + inputIndex] * indexBase;
			indexBase = indexBase * this.inputLev;
			i++;
		}
	
		if (this.fmt == DataFormat.UINT8)
		{
			ofst = index * this.outputCh;
			i = 0;
			while (i < this.outputCh)
			{
				int v = this.luTable[ofst + i];
				outVals[i + outputIndex] = v | (v << 8);
				i++;
			}
		}
		else if (this.fmt == DataFormat.UINT16)
		{
			ofst = index * this.outputCh * 2;
			i = 0;
			while (i < this.outputCh)
			{
				outVals[i + outputIndex] = this.luTable[ofst + i];
				i++;
			}
		}
	}

	public void getValueSingle(int inputVals[], int inputIndex, float outVals[], int outputIndex)
	{
		int indexBase = 1;
		int index = 0;
		int ofst;
		int i;
		i = 0;
		while (i < this.inputCh)
		{
			index += inputVals[i + inputIndex] * indexBase;
			indexBase = indexBase * this.inputLev;
			i++;
		}
	
		if (this.fmt == DataFormat.UINT8)
		{
			ofst = index * this.outputCh;
			i = 0;
			while (i < this.outputCh)
			{
				outVals[i + outputIndex] = (float)(this.luTable[ofst + i] / 255.0);
				i++;
			}
		}
		else if (this.fmt == DataFormat.UINT16)
		{
			ofst = index * this.outputCh * 2;
			i = 0;
			while (i < this.outputCh)
			{
				outVals[i + outputIndex] = (float)(this.luTable[ofst + i] / 65535.0);
				i++;
			}
		}
	}

	public LUT clone()
	{
		LUTInt newLut = new LUTInt(this.inputCh, this.inputLev, this.outputCh, this.fmt);
		if (this.remark != null)
		{
			newLut.setRemark(this.remark);
		}
		int i;
		i = getTableSize();
		while (i-- > 0)
		{
			newLut.luTable[i] = this.luTable[i];
		}
		return newLut;
	}

	public boolean tableEquals(LUT lut)
	{
		LUTInt lutInt = (LUTInt)lut;
		if (lutInt.fmt != this.fmt)
			return false;
		int i = getTableSize();
		while (i-- > 0)
		{
			if (lutInt.luTable[i] != this.luTable[i])
				return false;
		}
		return true;
	}
}
