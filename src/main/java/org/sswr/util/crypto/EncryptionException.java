package org.sswr.util.crypto;

public class EncryptionException extends Exception {
	private static final long serialVersionUID = -4213269857L;

	public EncryptionException(Throwable ex)
	{
		super(ex);
	}

	public EncryptionException(String message)
	{
		super(message);
	}
}
