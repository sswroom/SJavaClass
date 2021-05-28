package org.sswr.util.media.cs;

import org.sswr.util.media.LUT;
import org.sswr.util.media.LUTFloat;
import org.sswr.util.media.LUTInt;
import org.sswr.util.media.LUTInt.DataFormat;

public class TransferFuncLUT extends TransferFunc
{
	private int srcCnt;
	private double invLUT[];
	private double fwdLUT[];

	public TransferFuncLUT(LUT lut)
	{
		super(lut);
		this.srcCnt = lut.getInputLevel();
		this.fwdLUT = new double[65536];
		this.invLUT = new double[this.srcCnt];
		int i;
		int j;
		int k;
		if (lut.getClass().equals(LUTInt.class))
		{
			LUTInt lutInt = (LUTInt)lut;
			if (lutInt.getFormat() == DataFormat.UINT8)
			{
				double srcMul = 1 / 255.0;
				double currV;
				double valAdd;
				int lastV = 0;
				int thisV;
				int srcTab[] = lutInt.getTablePtr();
				double destMul = 1 / (double)(this.srcCnt - 1);
				currV = 0;
				valAdd = 1 / 65535.0;
				i = 0;
				while (i < this.srcCnt)
				{
					this.invLUT[i] = srcTab[i] * srcMul;
					thisV = srcTab[i];
					j = (lastV | (lastV << 8));
					k = (thisV | (thisV << 8));
					if (k > j)
					{
						currV = (i - 1) * destMul;
						valAdd = 1 / (double)(k - j) * destMul;
						while (j < k)
						{
							this.fwdLUT[j] = currV;
		
							currV += valAdd;
							j++;
						}
					}
		
					lastV = thisV;
					i++;
				}
				k = 65536;
				j = (lastV | (lastV << 8));
				while (j < k)
				{
					this.fwdLUT[j] = currV;
		
					currV += valAdd;
					j++;
				}
			}
			else if (lutInt.getFormat() == DataFormat.UINT16)
			{
				double srcMul = 1 / 65535.0;
				double currV;
				double valAdd;
				int lastV = 0;
				int thisV;
				int srcTab[] = lutInt.getTablePtr();
				double destMul = 1 / (double)(this.srcCnt - 1);
				currV = 0;
				valAdd = 1 / 65535.0;
				i = 0;
				while (i < this.srcCnt)
				{
					this.invLUT[i] = srcTab[i] * srcMul;
					thisV = srcTab[i];
					j = lastV;
					k = thisV;
					if (k > j)
					{
						currV = (i - 1) * destMul;
						valAdd = 1 / (double)(k - j) * destMul;
						while (j < k)
						{
							this.fwdLUT[j] = currV;
		
							currV += valAdd;
							j++;
						}
					}
		
					lastV = thisV;
					i++;
				}
				k = 65536;
				j = lastV;
				while (j < k)
				{
					this.fwdLUT[j] = currV;
		
					currV += valAdd;
					j++;
				}
			}
		}
		else if (lut.getClass().equals(LUTFloat.class))
		{
			LUTFloat lutFloat = (LUTFloat)lut;
			double currV;
			double valAdd;
			double lastV = 0;
			double thisV;
			float srcTab[] = lutFloat.getTablePtr();
			double destMul = 1 / (double)(this.srcCnt - 1);
			currV = 0;
			valAdd = 1 / 65535.0;
			i = 0;
			while (i < this.srcCnt)
			{
				this.invLUT[i] = srcTab[i];
				thisV = srcTab[i];
				j = (int)(lastV * 65535.0);
				k = (int)(thisV * 65535.0);
				if (k > j)
				{
					currV = (i - 1) * destMul;
					valAdd = 1 / (thisV - lastV) * destMul;
					while (j < k)
					{
						this.fwdLUT[j] = currV;
	
						currV += valAdd;
						j++;
					}
				}
	
				lastV = thisV;
				i++;
			}
			k = 65536;
			j = (int)(lastV * 65535.0);
			while (j < k)
			{
				this.fwdLUT[j] = currV;
	
				currV += valAdd;
				j++;
			}
		}
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal < 0.0)
		{
			double v1 = this.fwdLUT[0];
			double v2 = this.fwdLUT[1];
			return (v2 - v1) * linearVal * 65535.0;
		}
		else if (linearVal >= 1.0)
		{
			double v1 = this.fwdLUT[65534];
			double v2 = this.fwdLUT[65535];
			return v2 + (v2 - v1) * (linearVal - 1.0) * 65535.0;
		}
		else
		{
			double v = linearVal * 65535.0;
			int iv = (int)v;
			double v1 = this.fwdLUT[iv];
			double v2 = this.fwdLUT[iv + 1];
			return v1 + (v2 - v1) * (v - iv);
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal < 0.0)
		{
			double v1 = this.invLUT[0];
			double v2 = this.invLUT[1];
			return (v2 - v1) * gammaVal * (double)(this.srcCnt - 1);
		}
		else if (gammaVal >= 1.0)
		{
			double v1 = this.invLUT[this.srcCnt - 2];
			double v2 = this.invLUT[this.srcCnt - 1];
			return v2 + (v2 - v1) * (gammaVal - 1.0) * (double)(this.srcCnt - 1);
		}
		else
		{
			double v = gammaVal * (double)(this.srcCnt - 1);
			int iv = (int)v;
			double v1 = this.invLUT[iv];
			double v2 = this.invLUT[iv + 1];
			return v1 + (v2 - v1) * (v - iv);
		}
	}
}
