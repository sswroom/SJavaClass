package org.sswr.util.media.cs;

public class TransferFuncNLog extends TransferFunc
{
	public TransferFuncNLog()
	{
		super(TransferType.NLOG, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal < 0.328)
		{
			return 650 * Math.pow((linearVal + 0.0075), 1 / 3.0) / 1023.0;
		}
		else
		{
			return (150 * Math.log(linearVal) + 619) / 1023.0;
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		Double iVal = gammaVal * 1023.0;
		if (iVal < 452)
		{
			return Math.pow(iVal / 650.0, 3) - 0.0075;
		}
		else
		{
			return Math.exp((iVal - 619.0) / 150.0);
		}
	}
}
