package org.sswr.util.crypto;

import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1PDUBuilder;
import org.sswr.util.net.ASN1Util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyX509PubKey extends MyX509File
{
	public MyX509PubKey(@Nonnull String sourceName, @Nonnull byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.PublicKey;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		return new MyX509PubKey(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isPublicKeyInfo(this.buff, 0, this.buff.length, "1"))
		{
			appendPublicKeyInfo(this.buff, 0, this.buff.length, "1", sb);
		}
		return sb.toString();
	}

	@Nullable
	public MyX509Key createKey()
	{
		ASN1Item keyTypeOID = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
		ASN1Item keyData = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2");
		if (keyTypeOID != null && keyData != null)
		{
			if (keyData.itemType == ASN1Util.IT_BIT_STRING)
			{
				return new MyX509Key(this.sourceName, this.buff, keyData.ofst + 1, keyData.len - 1, keyTypeFromOID(this.buff, keyTypeOID.ofst, keyTypeOID.len, true));
			}
			else
			{
				return new MyX509Key(this.sourceName, this.buff, keyData.ofst, keyData.len, keyTypeFromOID(this.buff, keyTypeOID.ofst, keyTypeOID.len, true));
			}
		}
		return null;
	}

	@Nullable
	public byte[] getKeyId()
	{
		MyX509Key key = this.createKey();
		if (key != null)
		{
			return key.getKeyId();
		}
		return null;
	}

	@Nonnull
	public static MyX509PubKey createFromKeyBuff(@Nonnull KeyType keyType, @Nonnull byte[] buff, int ofst, int buffSize, @Nonnull String sourceName)
	{
		ASN1PDUBuilder keyPDU = new ASN1PDUBuilder();
		keyPDU.beginSequence();
		keyPDU.beginSequence();
		String oidStr = keyTypeGetOID(keyType);
		keyPDU.appendOIDString(oidStr);
		keyPDU.appendNull();
		keyPDU.endLevel();
		if (keyType == KeyType.RSAPublic)
		{
			keyPDU.appendBitString((byte)0, buff, ofst, buffSize);
		}
		else
		{
			keyPDU.appendBitString((byte)0, buff, ofst, buffSize);
		}
		keyPDU.endLevel();
		return new MyX509PubKey(sourceName, keyPDU.getBuff(), 0, keyPDU.getBuffSize());
	}
	
	@Nonnull
	public static MyX509PubKey createFromKey(@Nonnull MyX509Key key)
	{
		return createFromKeyBuff(key.getKeyType(), key.getASN1Buff(), 0, key.getASN1BuffSize(), key.getSourceNameObj());
	}
}
