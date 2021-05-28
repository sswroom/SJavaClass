package org.sswr.util.media.cs;

import org.sswr.util.media.LUT;

public abstract class TransferFunc
{
	protected TransferParam param;
	public TransferFunc(TransferType tranType, Double gamma)
	{
		this.param = new TransferParam(tranType, gamma);
	}

	public TransferFunc(LUT lut)
	{
		this.param = new TransferParam(lut);
	}

	public abstract double forwardTransfer(double linearVal);
	public abstract double inverseTransfer(double gammaVal);
	public TransferType getTransferType()
	{
		return this.param.getTranType();
	}

	public double getTransferGamma()
	{
		return this.param.getGamma();
	}

	public TransferParam getTransferParam()
	{
		return this.param;
	}

	public static TransferFunc createFunc(TransferParam param)
	{
		switch (param.getTranType())
		{
		case SRGB:
			return new TransferFuncSRGB();
		case BT709:
			return new TransferFuncBT709();
		case GAMMA:
			return new TransferFuncCGamma(param.getGamma());
		case BT1361:
			return new TransferFuncBT1361();
		case SMPTE240:
			return new TransferFuncSMPTE240();
		case LINEAR:
			return new TransferFuncLinear();
		case LOG100:
			return new TransferFuncLog100();
		case LOGSQRT10:
			return new TransferFuncLogSqrt10();
		case NTSC:
			return new TransferFuncNTSC();
		case SLOG:
			return new TransferFuncSLog();
		case SLOG1:
			return new TransferFuncSLog1();
		case SLOG2:
			return new TransferFuncSLog2();
		case SLOG3:
			return new TransferFuncSLog3();
		case VLOG:
			return new TransferFuncVLog();
		case PROTUNE:
			return new TransferFuncProtune();
		case LUT:
			return new TransferFuncLUT(param.getLUT());
		case BT2100:
			return new TransferFuncBT2100();
		case HLG:
			return new TransferFuncHLG();
		case NLOG:
			return new TransferFuncNLog();
		case PARAM1:
			return new TransferFuncParam1(param.params);
		default:
			return new TransferFuncSRGB();
		}
	}

	public static double getRefLuminance(TransferParam param)
	{
		if (param.getTranType() == TransferType.BT2100)
		{
			return 1000.0;
		}
		return 0.0;
	}

	public static String getTransferFuncName(TransferType ttype)
	{
		switch (ttype)
		{
		case SRGB:
			return "sRGB";
		case GAMMA:
			return "Constant Gamma";
		case LINEAR:
			return "Linear RGB";
		case BT709:
			return "BT.709";
		case SMPTE240:
			return "SMPTE 240M";
		case BT1361:
			return "BT.1361";
		case BT2100:
			return "BT.2100/SMPTE ST 2084 (HDR10)";
		case HLG:
			return "Hybrid Log Gamma (HLG)";
		case LOG100:
			return "Log Transfer (100:1)";
		case LOGSQRT10:
			return "Log Transfer (100 * Sqrt(10) : 1)";
		case NLOG:
			return "N-Log";
		case NTSC:
			return "NTSC";
		case SLOG:
			return "Sony S-Log";
		case SLOG1:
			return "Sony S-Log1";
		case SLOG2:
			return "Sony S-Log2";
		case SLOG3:
			return "Sony S-Log3";
		case VLOG:
			return "Panasonic V-Log";
		case PROTUNE:
			return "GoPro Protune";
		case LUT:
			return "LUT";
		case PARAM1:
			return "Parameter Function1";
		default:
			return "Unknown function";
		}
	}
}
