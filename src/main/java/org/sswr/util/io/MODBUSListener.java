package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public interface MODBUSListener
{
	public void onReadResult(int funcCode, @Nonnull byte[] result, int resultOfst, int resultSize);
	public void onSetResult(int funcCode, int startAddr, int count);
}
