package org.sswr.util.media.cs;

public class TransferFuncBT2100 extends TransferFunc
{
	public final double csBT2100m1 = (2610.0/16384.0);
	public final double csBT2100m2 = (2523.0/4096.0*128.0);
	public final double csBT2100c1 = (3424.0/4096.0);
	public final double csBT2100c2 = (2413.0/4096.0*32.0);
	public final double csBT2100c3 = (2392.0/4096.0*32.0);
	
	//Normally 1.0 means 100 cdm2
	public final double csBT2100Rate = 10.0;
	
	public TransferFuncBT2100()
	{
		super(TransferType.BT2100, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		double vTmp = Math.pow(linearVal / csBT2100Rate, csBT2100m1);
		double v1 = csBT2100c1 + csBT2100c2 * vTmp;
		double v2 = 1 + csBT2100c3 * vTmp;
		return Math.pow(v1 / v2, csBT2100m2);
	}
	
	public double inverseTransfer(double gammaVal)
	{
		double vTmp = Math.pow(gammaVal, 1 / csBT2100m2);
		double v1 = vTmp - csBT2100c1;
		double v2 = csBT2100c2 - csBT2100c3 * vTmp;
		if (v1 < 0)
			v1 = 0;
	
		return csBT2100Rate * Math.pow(v1 / v2, 1 / csBT2100m1);
	}
}
