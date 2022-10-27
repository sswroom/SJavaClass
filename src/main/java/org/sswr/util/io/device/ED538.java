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

	public Boolean isDIHighByCoil(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return null;
		return this.readCoil(diNum + 0x20);
	}

	public Boolean isDIHighByInput(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return null;
		return this.readDInput(diNum);
	}

	public Boolean isDIHighByReg(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return null;
		SharedInt val = new SharedInt();
		if (!this.readInputI16(diNum + 0x20, val))
			return null;
		return val.value != 0;
	}

	public Integer getDICountByReg(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return null;
		SharedInt val = new SharedInt();
		if (!this.readInputI16(diNum, val))
			return null;
		return val.value;
	}

	public boolean getDICountByReg(int diNum, SharedInt val)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.readInputI16(diNum, val);
	}

	public Integer getDICountByHolding(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return null;
		SharedInt val = new SharedInt();
		if (!this.readHoldingI16(diNum, val))
			return null;
		return val.value;
	}

	public boolean clearDICount(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return false;
		return this.writeDOutput(0x200 + diNum, true);
	}

	public Boolean isRelayHigh(int index)
	{
		if (index >= 4 || index < 0)
			return null;
		return this.readCoil(index);
	}

	public boolean setRelayState(int index, boolean isHigh)
	{
		if (index >= 4 || index < 0)
			return false;
		return this.writeDOutput(index, isHigh);
	}

	public Boolean getOutputOverloadFlag(int diNum)
	{
		if (diNum >= 8 || diNum < 0)
			return null;
		return this.readDInput(0x400 + diNum);
	}
}
