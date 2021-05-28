package org.sswr.util.media.cs;

public class TransferFuncCGamma extends TransferFunc
{
	public TransferFuncCGamma(double rgbGamma)
	{
		super(TransferType.GAMMA, rgbGamma);
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal < 0)
			return -Math.pow(-linearVal, (1 / this.param.getGamma()));
		else if (linearVal == 0)
			return 0;
		else
			return Math.pow(linearVal, (1 / this.param.getGamma()));
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal < 0)
			return -Math.pow(-gammaVal, (this.param.getGamma()));
		else if (gammaVal == 0)
			return 0;
		else
			return Math.pow(gammaVal, (this.param.getGamma()));
	}
}
