package org.sswr.util.io;

public interface MODBUSMaster
{
	public boolean readCoils(byte devAddr, int coilAddr, int coilCnt); //Output
	public boolean readInputs(byte devAddr, int inputAddr, int inputCnt);
	public boolean readHoldingRegisters(byte devAddr, int regAddr, int regCnt); //Output
	public boolean readInputRegisters(byte devAddr, int regAddr, int regCnt);
	public boolean writeCoil(byte devAddr, int coilAddr, boolean isHigh);
	public boolean writeHoldingRegister(byte devAddr, int regAddr, int val);
	public boolean writeHoldingRegisters(byte devAddr, int regAddr, int cnt, byte []val, int valOfst);

	public void handleReadResult(byte addr, MODBUSListener listener);

}
