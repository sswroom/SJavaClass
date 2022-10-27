package org.sswr.util.io;

import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.SharedInt;

public class MODBUSDevice implements MODBUSListener
{
	private MODBUSMaster modbus;
	private int timeout;
	private byte addr;
	private ThreadEvent cbEvt;
	private SharedDouble reqDResult;
	private SharedInt reqIResult;
	private byte[] reqBResult;
	private boolean reqHasResult;
	private int reqSetStartAddr;

	public void onReadResult(int funcCode, byte[] result, int resultOfst, int resultSize)
	{
		if (funcCode == 4)
		{
			if (this.reqBResult != null)
			{
				ByteTool.copyArray(this.reqBResult, 0, result, resultOfst, resultSize);
				this.reqHasResult = true;
				this.cbEvt.set();
			}
			else if (resultSize == 4)
			{
				if (this.reqDResult != null)
				{
					this.reqDResult.value = ByteTool.readMSingle(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (this.reqIResult != null)
				{
					this.reqIResult.value = ByteTool.readMInt32(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
			}
			else if (resultSize == 2)
			{
				if (this.reqIResult != null)
				{
					this.reqIResult.value = ByteTool.readMUInt16(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
			}
		}
		else if (funcCode == 3)
		{
			if (this.reqBResult != null)
			{
				ByteTool.copyArray(this.reqBResult, 0, result, resultOfst, resultSize);
				this.reqHasResult = true;
				this.cbEvt.set();
			}
			else if (resultSize == 4)
			{
				if (this.reqDResult != null)
				{
					this.reqDResult.value = ByteTool.readMSingle(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (this.reqIResult != null)
				{
					this.reqIResult.value = ByteTool.readMInt32(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
			}
			else if (resultSize == 2)
			{
				if (this.reqIResult != null)
				{
					this.reqIResult.value = ByteTool.readMUInt16(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
			}
		}
		else if (funcCode == 2)
		{
			if (this.reqIResult != null)
			{
				if (resultSize == 1)
				{
					this.reqIResult.value = result[resultOfst];
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (resultSize == 2)
				{
					this.reqIResult.value = ByteTool.readUInt16(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (resultSize == 3)
				{
					this.reqIResult.value = ByteTool.readUInt24(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (resultSize == 4)
				{
					this.reqIResult.value = ByteTool.readInt32(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
			}
		}
		else if (funcCode == 1)
		{
			if (this.reqIResult != null)
			{
				if (resultSize == 1)
				{
					this.reqIResult.value = result[resultOfst];
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (resultSize == 2)
				{
					this.reqIResult.value = ByteTool.readUInt16(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (resultSize == 3)
				{
					this.reqIResult.value = ByteTool.readUInt24(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
				else if (resultSize == 4)
				{
					this.reqIResult.value = ByteTool.readInt32(result, resultOfst);
					this.reqHasResult = true;
					this.cbEvt.set();
				}
			}
		}
	}

	public void onSetResult(int funcCode, int startAddr, int count)
	{
		if (funcCode == 5 || funcCode == 6 || funcCode == 15 || funcCode == 16)
		{
			if (this.reqSetStartAddr == startAddr)
			{
				this.reqHasResult = true;
				this.cbEvt.set();
			}
		}
	}

	public void setTimeout(int timeoutMS)
	{
		this.timeout = timeoutMS;
	}

	protected boolean readInputI16(int addr, SharedInt outVal)
	{
		boolean succ;
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqIResult = outVal;
			this.cbEvt.clear();
			this.modbus.readInputRegisters(this.addr, addr, 1);
			this.cbEvt.waitEvent(this.timeout);
			this.reqIResult = null;
			succ = this.reqHasResult;
		}
		return succ;
	}

	protected boolean readInputFloat(int addr, SharedDouble outVal)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqDResult = outVal;
			this.cbEvt.clear();
			this.modbus.readInputRegisters(this.addr, addr, 2);
			this.cbEvt.waitEvent(this.timeout);
			this.reqDResult = null;
			return this.reqHasResult;
		}
	}

	protected boolean readInputBuff(int addr, int regCnt, byte[] buff, int buffOfst)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqBResult = buff;
			this.cbEvt.clear();
			this.modbus.readInputRegisters(this.addr, addr, regCnt);
			this.cbEvt.waitEvent(this.timeout);
			this.reqBResult = null;
			return this.reqHasResult;
		}
	}

	protected boolean readHoldingI16(int addr, SharedInt outVal)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqIResult = outVal;
			this.cbEvt.clear();
			this.modbus.readHoldingRegisters(this.addr, addr, 1);
			this.cbEvt.waitEvent(this.timeout);
			this.reqIResult = null;
			return this.reqHasResult;
		}
	}

	protected boolean readHoldingI32(int addr, SharedInt outVal)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqIResult = outVal;
			this.cbEvt.clear();
			this.modbus.readHoldingRegisters(this.addr, addr, 2);
			this.cbEvt.waitEvent(this.timeout);
			this.reqIResult = null;
			return this.reqHasResult;
		}
	}

	protected boolean readHoldingFloat(int addr, SharedDouble outVal)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqDResult = outVal;
			this.cbEvt.clear();
			this.modbus.readHoldingRegisters(this.addr, addr, 2);
			this.cbEvt.waitEvent(this.timeout);
			this.reqDResult = null;
			return this.reqHasResult;
		}
	}

	protected boolean writeHoldingU16(int addr, int val)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqSetStartAddr = addr;
			this.cbEvt.clear();
			this.modbus.writeHoldingRegister(this.addr, addr, val);
			this.cbEvt.waitEvent(this.timeout);
			this.reqSetStartAddr = 0;
			return this.reqHasResult;
		}
	}

	protected boolean writeHoldingsU16(int addr, int cnt, int[] vals, int valOfst)
	{
		byte []buff = new byte[256];
		int i = 0;
		while (i < cnt)
		{
			ByteTool.writeMInt16(buff, i * 2, vals[i + valOfst]);
		}
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqSetStartAddr = addr;
			this.cbEvt.clear();
			this.modbus.writeHoldingRegisters(this.addr, addr, cnt, buff, 0);
			this.cbEvt.waitEvent(this.timeout);
			this.reqSetStartAddr = 0;
			return this.reqHasResult;
		}
	}

	protected boolean writeHoldingI32(int addr, int val)
	{
		byte []buff = new byte[4];
		ByteTool.writeMInt32(buff, 0, val);
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqSetStartAddr = addr;
			this.cbEvt.clear();
			this.modbus.writeHoldingRegisters(this.addr, addr, 2, buff, 0);
			this.cbEvt.waitEvent(this.timeout);
			this.reqSetStartAddr = 0;
			return this.reqHasResult;
		}
	}

	protected boolean writeHoldingF32(int addr, float val)
	{
		byte []buff = new byte[4];
		ByteTool.writeMSingle(buff, 0, val);
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqSetStartAddr = addr;
			this.cbEvt.clear();
			this.modbus.writeHoldingRegisters(this.addr, addr, 2, buff, 0);
			this.cbEvt.waitEvent(this.timeout);
			this.reqSetStartAddr = 0;
			return this.reqHasResult;
		}
	}

	protected Boolean readDInput(int addr)
	{
		SharedInt outVal = new SharedInt(0);
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqIResult = outVal;
			this.cbEvt.clear();
			this.modbus.readInputs(this.addr, addr, 1);
			this.cbEvt.waitEvent(this.timeout);
			this.reqIResult = null;
			if (this.reqHasResult)
			{
				return outVal.value != 0;
			}
			else
			{
				return null;
			}
		}
	}

	protected boolean readDInputs(int addr, int cnt, SharedInt outVal)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqIResult = outVal;
			this.cbEvt.clear();
			this.modbus.readInputs(this.addr, addr, cnt);
			this.cbEvt.waitEvent(this.timeout);
			this.reqIResult = null;
			return this.reqHasResult;
		}
	}

	protected Boolean readCoil(int addr)
	{
		SharedInt outVal = new SharedInt(0);
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqIResult = outVal;
			this.cbEvt.clear();
			this.modbus.readCoils(this.addr, addr, 1);
			this.cbEvt.waitEvent(this.timeout);
			this.reqIResult = null;
			if (this.reqHasResult)
			{
				return outVal.value != 0;
			}
			else
			{
				return null;
			}
		}
	}

	protected boolean writeDOutput(int addr, boolean isHigh)
	{
		synchronized(this)
		{
			this.reqHasResult = false;
			this.reqSetStartAddr = addr;
			this.cbEvt.clear();
			this.modbus.writeCoil(this.addr, addr, isHigh);
			this.cbEvt.waitEvent(this.timeout);
			this.reqSetStartAddr = 0;
			return this.reqHasResult;
		}
	}

	public MODBUSDevice(MODBUSMaster modbus, byte addr)
	{
		this.modbus = modbus;
		this.addr = addr;
		this.timeout = 200;
		this.cbEvt = new ThreadEvent(true);
		this.reqDResult = null;
		this.reqIResult = null;
		this.reqBResult = null;
		this.reqSetStartAddr = 0;
		this.modbus.handleReadResult(this.addr, this);	
	}

	public void close()
	{
		this.modbus.handleReadResult(this.addr, null);
		this.modbus.close();
	}
}
