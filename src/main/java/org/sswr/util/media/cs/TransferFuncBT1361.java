package org.sswr.util.media.cs;

public class TransferFuncBT1361 extends TransferFunc
{
	public final double csBT1361K1 = 0.018;
	public final double csBT1361K2 = 4.5;
	public final double csBT1361K3 = 0.099;
	public final double csBT1361K4 = 0.45;
	public final double csBT1361K5 = -0.0045;
	
	public final double csBT1361C1 = (csBT1361K1 * csBT1361K2);
	public final double csBT1361C2 = (1.0 / csBT1361K4);
	public final double csBT1361C3 = (csBT1361K1 * csBT1361K5);
	
	public TransferFuncBT1361()
	{
		super(TransferType.BT1361, 2.2);
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal < csBT1361K5)
			return ((1 + csBT1361K3) * Math.pow(-4 * linearVal, csBT1361K4) - csBT1361K3) / -4.0;
		else if (linearVal < csBT1361K1)
			return csBT1361K2 * linearVal;
		else
			return (1 + csBT1361K3) * Math.pow(linearVal, csBT1361K4) - csBT1361K3;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal < csBT1361C3)
			return Math.pow(((gammaVal * -4.0) + csBT1361K3) / (1 + csBT1361K3), csBT1361C2) / -4.0;
		else if (gammaVal < csBT1361C1)
			return gammaVal / csBT1361K2;
		else
			return Math.pow((gammaVal + csBT1361K3) / (1 + csBT1361K3), csBT1361C2);
	}
}
