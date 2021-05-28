package org.sswr.util.media.cs;

public class TransferFuncHLG extends TransferFunc
{
	public final double csHLGa = (0.17883277);
	public final double csHLGb = (0.28466892);
	public final double csHLGc = (0.55991073);

	public TransferFuncHLG()
	{
		super(TransferType.HLG, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal <= (1 / 12.0))
		{
			return Math.sqrt(3 * linearVal);
		}
		else
		{
			return csHLGc + csHLGa * Math.log(12 * linearVal - csHLGb);
		}
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal <= 0.5)
		{
			return (gammaVal * gammaVal) / 3.0;
		}
		else
		{
			return (Math.exp((gammaVal - csHLGc) / csHLGa) + csHLGb) / 12.0;
		}
	}
}
