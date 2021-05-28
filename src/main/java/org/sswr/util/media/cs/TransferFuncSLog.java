package org.sswr.util.media.cs;

public class TransferFuncSLog extends TransferFunc
{
	public TransferFuncSLog()
	{
		super(TransferType.SLOG, 2.2);
	}

	public double forwardTransfer(double linearVal)
	{
		return (0.432699 * Math.log10(linearVal + 0.037584) + 0.616596) + 0.03;
	}
	
	public double inverseTransfer(double gammaVal)
	{
		return Math.pow(10, ((gammaVal - 0.616596 - 0.03) / 0.432699)) - 0.037584;
	}
}
