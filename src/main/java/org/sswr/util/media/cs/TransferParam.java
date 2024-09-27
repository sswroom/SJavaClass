package org.sswr.util.media.cs;

import org.sswr.util.media.LUT;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TransferParam
{
	private TransferType tranType;
	private double gamma;

	private LUT lut;

	public double params[];

	public TransferParam()
	{
		this.lut = null;
		this.tranType = TransferType.SRGB;
		this.gamma = 2.2;
		this.params = null;
	}

	public TransferParam(@Nonnull TransferParam tran)
	{
		if (tran.lut != null)
		{
			this.lut = tran.lut.clone();
		}
		else
		{
			this.lut = null;
		}
		this.tranType = tran.tranType;
		this.gamma = tran.gamma;
		this.params = null;
		if (tran.params != null)
		{
			this.params = new double[tran.params.length];
			int i = this.params.length;
			while (i-- > 0)
			{
				this.params[i] = tran.params[i];
			}
		}
	}

	public TransferParam(@Nonnull TransferType tranType, double gamma)
	{
		this.lut = null;
		this.tranType = tranType;
		this.gamma = gamma;
		this.params = null;
	}

	public TransferParam(@Nonnull LUT lut)
	{
		this.lut = lut.clone();
		this.tranType = TransferType.LUT;
		this.params = null;
		this.gamma = 2.2;
	}

	public void set(@Nonnull TransferType tranType, double gamma)
	{
		this.tranType = tranType;
		this.gamma = gamma;
		this.lut = null;
		this.params = null;
	}

	public void set(@Nonnull TransferType tranType, double params[])
	{
		this.tranType = tranType;
		this.gamma = 2.2;
		this.lut = null;
		this.params = new double[params.length];
		int i = this.params.length;
		while (i-- > 0)
		{
			this.params[i] = params[i];
		}
	}

	public void set(@Nonnull LUT lut)
	{
		this.params = null;
		this.lut = lut.clone();
		this.tranType = TransferType.LUT;
	}

	public void set(@Nonnull TransferParam tran)
	{
		if (tran.lut != null)
		{
			this.lut = tran.lut.clone();
		}
		else
		{
			this.lut = null;
		}
		this.tranType = tran.tranType;
		this.gamma = tran.gamma;
		this.params = null;
		if (tran.params != null)
		{
			this.params = new double[tran.params.length];
			int i = this.params.length;
			while (i-- > 0)
			{
				this.params[i] = tran.params[i];
			}
		}
	}

	@Nonnull
	public TransferType getTranType()
	{
		return this.tranType;
	}

	public double getGamma()
	{
		return this.gamma;
	}

	@Nullable
	public LUT getLUT()
	{
		return this.lut;
	}

	public boolean Equals(@Nonnull TransferParam tran)
	{
		if (this.tranType != tran.tranType)
			return false;
		if (this.tranType == TransferType.GAMMA)
		{
			return this.gamma == tran.gamma;
		}
		else if (this.tranType == TransferType.LUT)
		{
			if ((this.lut == null) || (tran.lut == null))
				return false;
			return this.lut.equals(tran.lut);
		}
		else if (this.tranType == TransferType.PARAM1)
		{
			if (this.params.length != tran.params.length)
				return false;
			int i = this.params.length;
			while (i-- > 0)
			{
				if (this.params[i] != tran.params[i])
					return false;
			}
			return true;
		}
		else
		{
			return true;
		}
	}
}
