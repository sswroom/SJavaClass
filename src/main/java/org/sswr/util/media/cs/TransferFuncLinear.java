package org.sswr.util.media.cs;

public class TransferFuncLinear extends TransferFunc
{
	public TransferFuncLinear()
	{
		super(TransferType.LINEAR, 1.0);
	}


	public double forwardTransfer(double linearVal)
	{
		return linearVal;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		return gammaVal;
	}
}
