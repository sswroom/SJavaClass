package org.sswr.util.media.cs;

public class TransferFuncSLog1 extends TransferFunc
{
	public TransferFuncSLog1()
	{
		super(TransferType.SLOG1, 2.2);
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal >= 0)
		{
			return (0.432699 * Math.log10(linearVal + 0.037584) + 0.616596) + 0.03;
		}
		else
		{
			return linearVal * 5.0 + 0.030001222851889303;
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal >= 0.030001222851889303)
		{
			return Math.pow(10, ((gammaVal - 0.616596 - 0.03) / 0.432699)) - 0.037584;
		}
		else
		{
			return (gammaVal - 0.030001222851889303) / 5.0;
		}
	}
}
