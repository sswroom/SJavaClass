package org.sswr.util.media.cs;

public class TransferFuncLogSqrt10 extends TransferFunc
{
	public TransferFuncLogSqrt10()
	{
		super(TransferType.LOGSQRT10, 2.2);
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal >= 0.0031622776601683793319988935444327)
			return 1.0 + Math.log10(linearVal) * 0.4;
		else
			return 0;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal > 0)
			return Math.pow(10, (gammaVal - 1.0) * 2.5);
		else
			return 0;
	}
}
