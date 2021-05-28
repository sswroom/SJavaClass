package org.sswr.util.media.cs;

public class TransferFuncBT709 extends TransferFunc
{
	private final double csBT709K1 = 0.018;
	private final double csBT709K2 = 4.5;
	private final double csBT709K3 = 0.099;
	private final double csBT709K4 = 0.45;
	
	private final double csBT709C1 = (csBT709K1 * csBT709K2);
	private final double csBT709C2 = (1.0 / csBT709K4);
	
	public TransferFuncBT709()
	{
		super(TransferType.BT709, 2.2);
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal <= -csBT709K1)
			return (-1 - csBT709K3) * Math.pow(-linearVal, csBT709K4) + csBT709K3;
		else if (linearVal < csBT709K1)
			return csBT709K2 * linearVal;
		else
			return (1 + csBT709K3) * Math.pow(linearVal, csBT709K4) - csBT709K3;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal <= -csBT709C1)
			return -Math.pow((-gammaVal + csBT709K3) / (1 + csBT709K3), csBT709C2);
		else if (gammaVal < csBT709C1)
			return gammaVal / csBT709K2;
		else
			return Math.pow((gammaVal + csBT709K3) / (1 + csBT709K3), csBT709C2);
	}
}
