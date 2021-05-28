package org.sswr.util.media.cs;

public class TransferFuncProtune extends TransferFunc
{
	public TransferFuncProtune()
	{
		super(TransferType.PROTUNE, 2.2);
	}

	public double forwardTransfer(double linearVal)
	{
		return Math.log10(linearVal * 112.0 + 1.0) / Math.log10(113.0);
	}
	
	public double inverseTransfer(double gammaVal)
	{
		return (Math.pow(113.0, gammaVal) - 1.0) / 112.0;
	}
}
