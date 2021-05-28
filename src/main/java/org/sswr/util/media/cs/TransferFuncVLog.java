package org.sswr.util.media.cs;

public class TransferFuncVLog extends TransferFunc
{
	public TransferFuncVLog()
	{
		super(TransferType.VLOG, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal >= 0.01)
		{
			return 0.598206 + Math.log10(linearVal + 0.00873) * 0.241514;
		}
		else
		{
			return 5.6 * linearVal + 0.125;
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal >= 0.181)
		{
			return Math.pow(10.0, ((gammaVal - 0.598206) / 0.241514)) - 0.00873;
		}
		else
		{
			return (gammaVal - 0.125) / 5.6;
		}
	}
}
