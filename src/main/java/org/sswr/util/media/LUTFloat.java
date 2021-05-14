package org.sswr.util.media;

import org.sswr.util.data.ByteTool;

public class LUTFloat extends LUT
{
	private float luTable[];

	public LUTFloat(int inputCh, int inputLev, int outputCh)
	{
		super(inputCh, inputLev, outputCh);
		this.luTable = new float[getTableSize()];
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
	
		ofst = index * this.outputCh * 2;
		i = 0;
		while (i < this.outputCh)
		{
			outVals[i + outputIndex] = ByteTool.toSUInt8(this.luTable[ofst + i] * 255.0);
			i++;
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

		ofst = index * this.outputCh * 2;
		i = 0;
		while (i < this.outputCh)
		{
			outVals[i + outputIndex] = ByteTool.toSUInt16(this.luTable[ofst + i] * 65535.0);
			i++;
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
	
		ofst = index * this.outputCh;
		i = 0;
		while (i < this.outputCh)
		{
			outVals[i + outputIndex] = this.luTable[ofst + i];
			i++;
		}
	}

	public LUT clone()
	{
		LUTFloat newLut = new LUTFloat(this.inputCh, this.inputLev, this.outputCh);
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
		LUTFloat lutInt = (LUTFloat)lut;
		int i = getTableSize();
		while (i-- > 0)
		{
			if (lutInt.luTable[i] != this.luTable[i])
				return false;
		}
		return true;
	}	
}
