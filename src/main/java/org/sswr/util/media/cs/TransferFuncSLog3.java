package org.sswr.util.media.cs;

public class TransferFuncSLog3 extends TransferFunc
{
	public TransferFuncSLog3()
	{
		super(TransferType.SLOG3, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal >= 0.0112500)
		{
			return (420.0 + Math.log10((linearVal + 0.01) / (0.18 + 0.01)) * 261.5) / 1023.0;
		}
		else
		{
			return (linearVal * (171.2102946929 - 95.0) / 0.01125000 + 95.0) / 1023.0;
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal >= 171.2102946929 / 1023.0)
		{
			return Math.pow(10.0, ((gammaVal * 1023.0 - 420.0) / 261.5)) * (0.18 + 0.01) - 0.01;
		}
		else
		{
			return (gammaVal * 1023.0 - 95.0) * 0.01125000 / (171.2102946929 - 95.0);
		}
	}
}
