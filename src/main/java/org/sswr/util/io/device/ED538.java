package org.sswr.util.io.device;

import org.sswr.util.data.SharedInt;
import org.sswr.util.io.MODBUSDevice;
import org.sswr.util.io.MODBUSMaster;

public class ED538 extends MODBUSDevice
{
	public ED538(MODBUSMaster modbus, byte addr)
	{
		super(modbus, addr);
	}


	public boolean isDIHighByCoil(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.readCoil(diNum + 0x20);
	}

	public boolean isDIHighByInput(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.readDInput(diNum);
	}

	public boolean isDIHighByReg(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		SharedInt val = new SharedInt();
		if (!this.readInputI16(diNum + 0x20, val))
			return false;
		return val.value != 0;
	}

	public int getDICountByReg(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return 0;
		SharedInt val = new SharedInt();
		if (!this.readInputI16(diNum, val))
			return 0;
		return val.value;
	}

	public boolean getDICountByReg(int diNum, SharedInt val)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.readInputI16(diNum, val);
	}

	public int getDICountByHolding(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return 0;
		SharedInt val = new SharedInt();
		if (!this.readHoldingI16(diNum, val))
			return 0;
		return val.value;
	}

	public boolean clearDICount(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.writeDOutput(0x200 + diNum, true);
	}

	public boolean isRelayHigh(int index)
	{
		if (index >= 4 || index < 0)
			return false;
		return this.readCoil(index);
	}

	public boolean setRelayState(int index, boolean isHigh)
	{
		if (index >= 4 || index < 0)
			return false;
		return this.writeDOutput(index, isHigh);
	}

	public boolean getOutputOverloadFlag(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.readDInput(0x400 + diNum);
	}
}
