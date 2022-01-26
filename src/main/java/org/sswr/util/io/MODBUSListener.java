package org.sswr.util.io;

public interface MODBUSListener
{
	public void onReadResult(int funcCode, byte[] result, int resultOfst, int resultSize);
	public void onSetResult(int funcCode, int startAddr, int count);
}
