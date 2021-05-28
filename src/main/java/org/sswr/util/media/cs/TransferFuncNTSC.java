package org.sswr.util.media.cs;

public class TransferFuncNTSC extends TransferFunc
{
	public final double csGammaC1 = (1 / 0.45);
	public final double csGammaC2 = (0.45);
	
	public TransferFuncNTSC()
	{
		super(TransferType.NTSC, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal < 0)
			return -Math.pow(-linearVal, csGammaC2) * 0.925 + 0.075;
		else if (linearVal == 0)
			return 0.075;
		else
			return Math.pow(linearVal, csGammaC2) * 0.925 + 0.075;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal < 0.075)
			return -Math.pow(-(gammaVal - 0.075) / 0.925, csGammaC1);
		else if (gammaVal == 0.075)
			return 0;
		else
			return Math.pow((gammaVal - 0.075) / 0.925, csGammaC1);
	}
}
