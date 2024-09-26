package org.sswr.util.crypto;

public class EncryptionException extends Exception {
	public EncryptionException(Throwable ex)
	{
		super(ex);
	}

	public EncryptionException(String message)
	{
		super(message);
	}
}
