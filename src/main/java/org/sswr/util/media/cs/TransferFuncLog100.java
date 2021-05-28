package org.sswr.util.media.cs;

public class TransferFuncLog100 extends TransferFunc
{
	public TransferFuncLog100()
	{
		super(TransferType.LOG100, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal >= 0.01)
			return 1.0 + Math.log10(linearVal) * 0.5;
		else
			return 0;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal > 0)
			return Math.pow(10, (gammaVal - 1.0) * 2.0);
		else
			return 0;
	}
}
