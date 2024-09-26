package org.sswr.util.crypto;

import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1PDUBuilder;
import org.sswr.util.net.ASN1Util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyX509PrivKey extends MyX509File
{
	public MyX509PrivKey(@Nonnull String sourceName, @Nonnull byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.PrivateKey;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		return new MyX509PrivKey(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isPrivateKeyInfo(this.buff, 0, this.buff.length, "1"))
		{
			appendPrivateKeyInfo(this.buff, 0, this.buff.length, "1", sb);
		}
		return sb.toString();
	}

	@Nonnull
	public KeyType getKeyType()
	{
		ASN1Item item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2.1");
		if (item != null)
		{
			return keyTypeFromOID(this.buff, item.ofst, item.len, false);
		}
		return KeyType.Unknown;
	}

	@Nullable
	public MyX509Key createKey()
	{
		KeyType keyType = this.getKeyType();
		if (keyType == KeyType.Unknown)
		{
			return null;
		}
		ASN1Item item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.3");
		if (item != null)
		{
			return new MyX509Key(this.sourceName, this.buff, item.ofst, item.len, keyType);
		}
		return null;
	}

	@Nullable
	public byte[] getKeyId()
	{
		MyX509Key key = createKey();
		if (key != null)
		{
			return key.getKeyId();
		}
		return null;
	}

	@Nonnull
	public static MyX509PrivKey createFromKeyBuff(@Nonnull KeyType keyType, @Nonnull byte[] buff, int ofst, int buffSize, @Nullable String sourceName)
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

	@Nullable
	public static MyX509PrivKey createFromKey(@Nonnull MyX509Key key)
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
			if (keyBuff == null)
				return null;
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
