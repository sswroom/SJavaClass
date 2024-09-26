package org.sswr.util.io;

import java.util.HashMap;
import java.util.Map;

import org.sswr.util.basic.HiResClock;
import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MODBUSTCPMaster implements MODBUSMaster, Runnable
{
	public static final int CMDDELAY = 0;

	private IOStream stm;
	private int tranId;
	private boolean threadRunning;
	private boolean threadToStop;
	private HiResClock clk;
	private Map<Byte, MODBUSListener> cbMap;

	public void run()
	{
		byte []buff = new byte[1024];
		int buffSize = 0;
		int readSize;
		int i;
		boolean incomplete;
		MODBUSListener cb;
		this.threadRunning = true;
		while (!this.threadToStop)
		{
			readSize = this.stm.read(buff, buffSize, 1024 - buffSize);
			if (readSize > 0)
			{
				buffSize += readSize;
				incomplete = false;
				i = 0;
				while (i < buffSize - 5)
				{
					if ( buff[i + 2] == 0 && buff[i + 3] == 0)
					{
						int packetSize = ByteTool.readMUInt16(buff, i + 4);
						if (i + 6 + packetSize > buffSize)
						{
							incomplete = true;
						}
						else
						{
							switch (buff[i + 7])
							{
							case 1:
							case 2:
							case 3:
							case 4:
								if (3 + buff[i + 8] == packetSize)
								{
									cb = this.cbMap.get(buff[i + 6]);
									if (cb != null)
									{
										cb.onReadResult(buff[i + 7], buff, i + 9, buff[i + 8]);
									}
									i += 6 + packetSize;
								}
								else
								{
									i++;
								}
								break;
							case 5:
							case 6:
							case 15:
							case 16:
								if (packetSize == 6)
								{
									cb = this.cbMap.get(buff[i + 6]);
									if (cb != null)
									{
										cb.onSetResult(buff[i + 7], ByteTool.readMUInt16(buff, i + 8), ByteTool.readMUInt16(buff, i + 10));
									}
									i += 12;
								}
								else
								{
									i++;
								}
								break;
							default:
								i += packetSize + 6;
								break;
							}
						}
					}
					else
					{
						i++;
					}
	
					if (incomplete)
					{
						break;
					}
				}
	
				if (i >= buffSize)
				{
					buffSize = 0;
				}
				else if (i >= 0)
				{
					ByteTool.copyArray(buff, 0, buff, i, buffSize - i);
					buffSize -= i;
				}
			}
			else
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException ex)
				{
					
				}
			}
			
		}
		this.threadRunning = false;
	}

	public MODBUSTCPMaster(@Nonnull IOStream stm)
	{
		this.stm = stm;
		this.threadRunning = false;
		this.threadToStop = false;
		this.tranId = 0;
		this.cbMap = new HashMap<Byte, MODBUSListener>();
		this.clk = new HiResClock();
		if (this.stm != null)
		{
			Thread t = new Thread(this);
			t.start();
			while (!this.threadRunning)
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	
	}

	public void close()
	{
		if (this.stm != null)
		{
			this.threadToStop = true;
			this.stm.close();
			while (this.threadRunning)
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException ex)
				{

				}
			}
		}
	}

	public boolean readCoils(byte devAddr, int coilAddr, int coilCnt)
	{
		byte []buff = new byte[12];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 1;
		ByteTool.writeMInt16(buff, 8, coilAddr);
		ByteTool.writeMInt16(buff, 10, coilCnt);
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, 12);
				this.clk.start();
			}
		}
		return true;
	}

	public boolean readInputs(byte devAddr, int inputAddr, int inputCnt)
	{
		byte []buff = new byte[12];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 2;
		ByteTool.writeMInt16(buff, 8, inputAddr);
		ByteTool.writeMInt16(buff, 10, inputCnt);
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, 12);
				this.clk.start();
			}
		}
		return true;
	}

	public boolean readHoldingRegisters(byte devAddr, int regAddr, int regCnt)
	{
		byte []buff = new byte[12];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 3;
		ByteTool.writeMInt16(buff, 8, regAddr);
		ByteTool.writeMInt16(buff, 10, regCnt);
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, 12);
				this.clk.start();
			}
		}
		return true;
	}

	public boolean readInputRegisters(byte devAddr, int regAddr, int regCnt)
	{
		byte []buff = new byte[12];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 4;
		ByteTool.writeMInt16(buff, 8, regAddr);
		ByteTool.writeMInt16(buff, 10, regCnt);
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, 12);
				this.clk.start();
			}
		}
		return true;
	}

	public boolean writeCoil(byte devAddr, int coilAddr, boolean isHigh)
	{
		byte []buff = new byte[12];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 5;
		ByteTool.writeMInt16(buff, 8, coilAddr);
		if (isHigh)
		{
			ByteTool.writeMInt16(buff, 10, 0xff00);
		}
		else
		{
			ByteTool.writeMInt16(buff, 10, 0);
		}	
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, 12);
				this.clk.start();
			}
		}
		return true;
	}

	public boolean writeHoldingRegister(byte devAddr, int regAddr, int val)
	{
		byte []buff = new byte[12];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 6;
		ByteTool.writeMInt16(buff, 8, regAddr);
		ByteTool.writeMInt16(buff, 10, val);
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, 12);
				this.clk.start();
			}
		}
		return true;
	}

	public boolean writeHoldingRegisters(byte devAddr, int regAddr, int cnt, @Nonnull byte []val, int valOfst)
	{
		byte []buff = new byte[cnt * 2 + 13];
		ByteTool.writeMInt16(buff, 0, this.tranId);
		this.tranId++;
		buff[2] = 0;
		buff[3] = 0;
		ByteTool.writeMInt16(buff, 4, 7 + cnt * 2);
		buff[4] = 0;
		buff[5] = 6;
		buff[6] = devAddr;
		buff[7] = 16;
		ByteTool.writeMInt16(buff, 8, regAddr);
		ByteTool.writeMInt16(buff, 10, cnt);
		buff[12] = (byte)(cnt << 1);
		ByteTool.copyArray(buff, 13, val, valOfst, cnt * 2);
		if (this.stm != null)
		{
			synchronized(this)
			{
				double t = this.clk.getTimeDiff();
				if (t < CMDDELAY * 0.001)
				{
					try
					{
						Thread.sleep((CMDDELAY - (int)(t * 1000)));
					}
					catch (InterruptedException ex)
					{

					}
				}
				this.stm.write(buff, 0, cnt * 2 + 13);
				this.clk.start();
			}
		}
		return true;
	}

	public void handleReadResult(byte addr, @Nullable MODBUSListener listener)
	{
		this.cbMap.put(addr, listener);
	}
}
