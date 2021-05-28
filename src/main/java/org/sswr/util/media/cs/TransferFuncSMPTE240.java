package org.sswr.util.media.cs;

public class TransferFuncSMPTE240 extends TransferFunc
{
	public final double cs240MK1 = 0.0228;
	public final double cs240MK2 = 4.0;
	public final double cs240MK3 = 0.1115;
	public final double cs240MK4 = 0.45;
	
	public final double cs240MC1 = (cs240MK1 * cs240MK2);
	public final double cs240MC2 = (1.0 / cs240MK4);
	
	public TransferFuncSMPTE240()
	{
		super(TransferType.SMPTE240, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal <= -cs240MK1)
			return (-1 - cs240MK3) * Math.pow(-linearVal, cs240MK4) + cs240MK3;
		else if (linearVal < cs240MK1)
			return cs240MK2 * linearVal;
		else
			return (1 + cs240MK3) * Math.pow(linearVal, cs240MK4) - cs240MK3;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal <= -cs240MC1)
			return -Math.pow((-gammaVal + cs240MK3) / (1 + cs240MK3), cs240MC2);
		else if (gammaVal < cs240MC1)
			return gammaVal / cs240MK2;
		else
			return Math.pow((gammaVal + cs240MK3) / (1 + cs240MK3), cs240MC2);
	}
}
