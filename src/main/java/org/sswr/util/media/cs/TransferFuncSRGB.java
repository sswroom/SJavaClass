package org.sswr.util.media.cs;

public class TransferFuncSRGB extends TransferFunc
{
	public final double cssRGBK1 = 0.0031308;
	public final double cssRGBK2 = 12.92;
	public final double cssRGBK3 = 0.055;
	public final double cssRGBK4 = (1.0 / 2.4);
	
	public final double cssRGBC1 = (cssRGBK1 * cssRGBK2);
	public final double cssRGBC2 = (1.0 / cssRGBK4);

	public TransferFuncSRGB()
	{
		super(TransferType.SRGB, 2.2);
	}


	public double forwardTransfer(double linearVal)
	{
		if (linearVal < -cssRGBK1)
			return (-1 - cssRGBK3) * Math.pow(-linearVal, cssRGBK4) + cssRGBK3;
		else if (linearVal <= cssRGBK1)
			return cssRGBK2 * linearVal;
		else
			return (1 + cssRGBK3) * Math.pow(linearVal, cssRGBK4) - cssRGBK3;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal <= -cssRGBC1)
			return -Math.pow((-gammaVal + cssRGBK3) / (1 + cssRGBK3), cssRGBC2);
		else if (gammaVal < cssRGBC1)
			return gammaVal / cssRGBK2;
		else
			return Math.pow((gammaVal + cssRGBK3) / (1 + cssRGBK3), cssRGBC2);
	}
}
