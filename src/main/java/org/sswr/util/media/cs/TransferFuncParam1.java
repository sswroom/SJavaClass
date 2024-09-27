package org.sswr.util.media.cs;

import jakarta.annotation.Nonnull;

public class TransferFuncParam1 extends TransferFunc
{
	private double param2;

	public TransferFuncParam1(@Nonnull double params[])
	{
		super(TransferType.PARAM1, 2.2);
		this.param.set(TransferType.PARAM1, params);
		this.param2 = this.param.params[4] * this.param.params[3] + this.param.params[6];
	}

	public double forwardTransfer(double linearVal)
	{
		if (linearVal < this.param2)
		{
			return (linearVal - this.param.params[6]) / this.param.params[3];
		}
		else
		{
			return (Math.pow(linearVal - this.param.params[5], 1 / this.param.params[0]) - this.param.params[2]) / this.param.params[1];
		}
	}

	public double inverseTransfer(double gammaVal)
	{
		if (gammaVal < this.param.params[4])
		{
			return gammaVal * this.param.params[3] + this.param.params[6];
		}
		else
		{
			return Math.pow(gammaVal * this.param.params[1] + this.param.params[2], this.param.params[0]) + this.param.params[5];
		}
	}
}
