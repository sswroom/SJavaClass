package org.sswr.util.crypto.cert;

import org.sswr.util.crypto.cert.MyX509File.AlgType;

public class SignedInfo
{
	public AlgType algType;
	public byte[] payload;
	public int payloadOfst;
	public int payloadSize;
	public byte[] signature;
	public int signOfst;
	public int signSize;
}
