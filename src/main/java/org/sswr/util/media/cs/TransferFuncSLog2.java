package org.sswr.util.media.cs;

public class TransferFuncSLog2 extends TransferFunc
{
	public TransferFuncSLog2()
	{
		super(TransferType.SLOG2, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal >= 0)
		{
			return (0.432699 * Math.log10(linearVal * 155.0 / 219.0 + 0.037584) + 0.616596) + 0.03;
		}
		else
		{
			return linearVal * 3.53881278538813 + 0.030001222851889303;
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal >= 0.030001222851889303)
		{
			return 219.0 * (Math.pow(10, ((gammaVal - 0.616596 - 0.03) / 0.432699)) - 0.037584) / 155.0;
		}
		else
		{
			return (gammaVal - 0.030001222851889303) / 3.53881278538813;
		}
	}
}
