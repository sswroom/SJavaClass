package org.sswr.util.crypto;

import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1PDUBuilder;

public class MyX509PrivKey extends MyX509File
{
	public MyX509PrivKey(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.PrivateKey;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509PrivKey(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isPrivateKeyInfo(this.buff, 0, this.buff.length, "1"))
		{
			appendPrivateKeyInfo(this.buff, 0, this.buff.length, "1", sb);
		}
		return sb.toString();
	}

	public static MyX509PrivKey createFromKeyBuff(KeyType keyType, byte[] buff, int ofst, int buffSize, String sourceName)
	{
		if (sourceName == null)
		{
			sourceName = "";
		}
		ASN1PDUBuilder keyPDU = new ASN1PDUBuilder();
		keyPDU.beginSequence();
		keyPDU.appendInt32(0);
		keyPDU.beginSequence();
		String oidStr = keyTypeGetOID(keyType);
		keyPDU.appendOIDString(oidStr);
		keyPDU.appendNull();
		keyPDU.endLevel();
		keyPDU.appendOctetString(buff, ofst, buffSize);
		keyPDU.endLevel();
		return new MyX509PrivKey(sourceName, keyPDU.getBuff(null), 0, keyPDU.getBuffSize());
	}

	public static MyX509PrivKey createFromKey(MyX509Key key)
	{
		KeyType keyType = key.getKeyType();
		if (keyType == KeyType.ECDSA)
		{
			ECName ecName = key.getECName();
			byte[] keyBuff;
			ASN1PDUBuilder keyPDU = new ASN1PDUBuilder();
			keyPDU.beginSequence();
			keyPDU.appendInt32(0);
			keyPDU.beginSequence();
			String oidStr = keyTypeGetOID(keyType);
			keyPDU.appendOIDString(oidStr);
			oidStr = ecNameGetOID(ecName);
			keyPDU.appendOIDString(oidStr);
			keyPDU.endLevel();
			keyPDU.beginOther((byte)4);
			keyPDU.beginSequence();
			keyPDU.appendInt32(1);
			keyBuff = key.getECPrivate();
			keyPDU.appendOctetString(keyBuff, 0, keyBuff.length);
			keyBuff = key.getECPublic();
			if (keyBuff != null)
			{
				keyPDU.beginContentSpecific(1);
				keyPDU.appendBitString((byte)0, keyBuff, 0, keyBuff.length);
				keyPDU.endLevel();
			}
			keyPDU.endLevel();
			keyPDU.endLevel();
			keyPDU.endLevel();
			MyX509PrivKey pkey;
			pkey = new MyX509PrivKey(key.getSourceNameObj(), keyPDU.getBuff(null), 0, keyPDU.getBuffSize());
			return pkey;
		}
		else if (keyType == KeyType.RSA)
		{
			return createFromKeyBuff(keyType, key.getASN1Buff(), 0, key.getASN1BuffSize(), key.getSourceNameObj());
		}
		else
		{
			return null;
		}
	}
}
