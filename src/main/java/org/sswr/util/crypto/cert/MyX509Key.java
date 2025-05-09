package org.sswr.util.crypto.cert;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.sswr.util.crypto.hash.HashType;
import org.sswr.util.crypto.hash.SHA1;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1PDUBuilder;
import org.sswr.util.net.ASN1Util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyX509Key extends MyX509File
{
	private KeyType keyType;

	public MyX509Key(@Nonnull String sourceName, @Nonnull byte[] buff, int ofst, int size, @Nonnull KeyType keyType)
	{
		super(sourceName, buff, ofst, size);
		this.keyType = keyType;
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.Key;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		return new MyX509Key(this.sourceName, this.buff, 0, this.buff.length, this.keyType);
	}

	@Override
	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		byte[] buff;
		if (this.keyType == KeyType.RSA)
		{
			buff = this.getRSAModulus();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Modulus = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAPublicExponent();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Public Exponent = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAPrivateExponent();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Private Exponent = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAPrime1();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Prime1 = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAPrime2();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Prime2 = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAExponent1();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Exponent1 = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAExponent2();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Exponent2 = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSACoefficient();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Coefficient = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
		}
		else if (this.keyType == KeyType.RSAPublic)
		{
			buff = this.getRSAModulus();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Modulus = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
			buff = this.getRSAPublicExponent();
			if (buff != null)
			{
				if (found) sb.append("\r\n");
				found = true;
				sb.append(this.sourceName);
				sb.append('.');
				sb.append("RSA.Public Exponent = ");
				StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
			}
		}
	
		buff = this.getKeyId();
		if (buff != null)
		{
			if (found) sb.append("\r\n");
			found = true;
			sb.append(this.sourceName);
			sb.append('.');
			sb.append("KeyId = ");
			StringUtil.appendHex(sb, buff, 0, buff.length, ' ', LineBreakType.NONE);
		}
		return sb.toString();
	}

	@Nonnull
	public KeyType getKeyType()
	{
		return this.keyType;
	}

	public int getKeySizeBits()
	{
		return keyGetLeng(this.buff, 0, this.buff.length, this.keyType);
	}

	public boolean isPrivateKey()
	{
		{
			switch (this.keyType)
			{
			case DSA:
			case ECDSA:
			case ED25519:
			case RSA:
				return true;
			case RSAPublic:
			case ECPublic:
			case Unknown:
			default:
				return false;
			}
		}
	}

	@Nullable
	public MyX509Key createPublicKey()
	{
		if (this.keyType == KeyType.RSAPublic)
		{
			return (MyX509Key)this.clone();
		}
		else if (this.keyType == KeyType.RSA)
		{
			ASN1PDUBuilder builder = new ASN1PDUBuilder();
			byte[] buff;
			builder.beginSequence();
			if ((buff = this.getRSAModulus()) == null) return null;
			builder.appendOther((byte)ASN1Util.IT_INTEGER, buff, 0, buff.length);
			if ((buff = this.getRSAPublicExponent()) == null) return null;
			builder.appendOther((byte)ASN1Util.IT_INTEGER, buff, 0, buff.length);
			builder.endLevel();
			SharedInt buffSize = new SharedInt();
			buff = builder.getBuff(buffSize);
			return new MyX509Key(this.getSourceNameObj(), buff, 0, buffSize.value, KeyType.RSAPublic);
		}
		else
		{
			return null;
		}
	}

	@Nullable
	public byte[] getKeyId()
	{
		MyX509Key pubKey = this.createPublicKey();
		if (pubKey != null)
		{
			SHA1 sha1 = new SHA1();
			byte[] buff = pubKey.getASN1Buff();
			sha1.calc(buff, 0, buff.length);
			return sha1.getValue();
		}
		return null;
	}
	
	@Nullable
	private byte[] toBuff(@Nullable ASN1Item item)
	{
		if (item == null)
			return null;
		return Arrays.copyOfRange(this.buff, item.ofst, item.ofst + item.len);
	}


	public int getDataBlockSize()
	{
		return getKeySizeBits() / 8;
	}

	@Nullable
	public byte[] getRSAModulus()
	{
		if (this.keyType == KeyType.RSA)
		{
			return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2"));
		}
		else if (this.keyType == KeyType.RSAPublic)
		{
			return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1"));
		}
		else
		{
			return null;
		}
	}
	
	@Nullable
	public byte[] getRSAPublicExponent()
	{
		if (this.keyType == KeyType.RSA)
		{
			return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.3"));
		}
		else if (this.keyType == KeyType.RSAPublic)
		{
			return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2"));
		}
		else
		{
			return null;
		}
	}
	
	@Nullable
	public byte[] getRSAPrivateExponent()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.4"));
	}
	
	@Nullable
	public byte[] getRSAPrime1()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.5"));
	}
	
	@Nullable
	public byte[] getRSAPrime2()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.6"));
	}
	
	@Nullable
	public byte[] getRSAExponent1()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.7"));
	}
	
	@Nullable
	public byte[] getRSAExponent2()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.8"));
	}
	
	@Nullable
	public byte[] getRSACoefficient()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.9"));
	}

	@Nullable
	public byte[] getECPrivate()
	{
		if (this.keyType == KeyType.ECDSA)
		{
			return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2"));
		}
		else
		{
			return null;
		}		
	}

	@Nullable
	public byte[] getECPublic()
	{
		ASN1Item itemPDU;
		if (this.keyType == KeyType.ECPublic)
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2");
			if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_BIT_STRING)
			{
				return Arrays.copyOfRange(this.buff, itemPDU.ofst + 1, itemPDU.ofst + itemPDU.len);
			}
			return null;
		}
		else if (this.keyType == KeyType.ECDSA)
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.3");
			if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_1)
			{
				itemPDU = ASN1Util.pduGetItem(this.buff, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
				if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_BIT_STRING)
				{
					return Arrays.copyOfRange(this.buff, itemPDU.ofst + 1, itemPDU.ofst + itemPDU.len);
				}
				return null;
			}
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.4");
			if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_1)
			{
				itemPDU = ASN1Util.pduGetItem(this.buff, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
				if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_BIT_STRING)
				{
					return Arrays.copyOfRange(this.buff, itemPDU.ofst + 1, itemPDU.ofst + itemPDU.len);
				}
				return null;
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	@Nonnull
	public ECName getECName()
	{
		if (this.keyType == KeyType.ECPublic)
		{
			ASN1Item itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.2");
			if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_OID)
			{
				return ecNameFromOID(this.buff, itemPDU.ofst, itemPDU.len);
			}
		}
		else if (this.keyType == KeyType.ECDSA)
		{
			ASN1Item itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.3");
			if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				itemPDU = ASN1Util.pduGetItem(this.buff, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
				if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_OID)
				{
					return ecNameFromOID(this.buff, itemPDU.ofst, itemPDU.len);
				}
			}
		}
		return ECName.Unknown;
	}

	@Nullable
	public Cipher createCipher(@Nonnull CipherPadding padding)
	{
		try
		{
			if (keyType == KeyType.RSA)
			{
				if (padding != CipherPadding.OAEP)
				{
					return Cipher.getInstance("RSA");
				}
				else
				{
					return Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
				}
			}
			else if (keyType == KeyType.DSA)
			{
				return Cipher.getInstance("DSA");
			}
			else if (keyType == KeyType.ECDSA)
			{
				return Cipher.getInstance("ECDSA");
			}
			else if (keyType == KeyType.ED25519)
			{
				return Cipher.getInstance("ED25519");
			}
			else
			{
				return null;
			}
		}
		catch (NoSuchAlgorithmException|NoSuchPaddingException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public PrivateKey createJPrivateKey()
	{
		MyX509PrivKey privKey = MyX509PrivKey.createFromKey(this);
		if (privKey == null)
			return null;
		try
		{
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privKey.getASN1Buff());
			KeyFactory kf;
			if (keyType == KeyType.RSA)
			{
				kf = KeyFactory.getInstance("RSA");
			}
			else if (keyType == KeyType.DSA)
			{
				kf = KeyFactory.getInstance("DSA");
			}
			else if (keyType == KeyType.ECDSA)
			{
				kf = KeyFactory.getInstance("ECDSA");
			}
			else if (keyType == KeyType.ED25519)
			{
				kf = KeyFactory.getInstance("ED25519");
			}
			else
			{
				return null;
			}
			return kf.generatePrivate(spec);
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvalidKeySpecException ex)
		{
			ex.printStackTrace();
			return null;
		}		
	}

	@Nullable
	public PublicKey createJPublicKey()
	{
		MyX509Key pKey = createPublicKey();
		if (pKey == null)
			return null;
		MyX509PubKey pubKey = MyX509PubKey.createFromKey(pKey);
		try
		{
			X509EncodedKeySpec spec = new X509EncodedKeySpec(pubKey.getASN1Buff());
			KeyFactory kf;
			if (keyType == KeyType.RSAPublic || keyType == KeyType.RSA)
			{
				kf = KeyFactory.getInstance("RSA");
			}
			else if (keyType == KeyType.DSA)
			{
				kf = KeyFactory.getInstance("DSA");
			}
			else if (keyType == KeyType.ECDSA)
			{
				kf = KeyFactory.getInstance("ECDSA");
			}
			else if (keyType == KeyType.ED25519)
			{
				kf = KeyFactory.getInstance("ED25519");
			}
			else
			{
				return null;
			}
			return kf.generatePublic(spec);
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvalidKeySpecException ex)
		{
			ex.printStackTrace();
			return null;
		}	
	}

	@Nullable
	public byte[] signature(@Nonnull HashType hashType, @Nonnull byte[] buff, int ofst, int len)
	{
		PrivateKey key = createJPrivateKey();
		if (key == null)
			return null;
		return CertUtil.signature(buff, ofst, len, hashType, key);
	}

	public boolean signatureVerify(@Nonnull HashType hashType, @Nonnull byte[] buff, int ofst, int len, @Nonnull byte[] sign, int signOfst, int signSize)
	{
		PublicKey key = createJPublicKey();
		if (key == null)
			return false;
		return CertUtil.verifySign(buff, ofst, len, sign, signOfst, signSize, key, hashType, null, "temp");
	}

	@Nullable
	public byte[] decrypt(@Nonnull byte[] buff, int ofst, int len, @Nonnull CipherPadding padding)
	{
		try
		{
			PrivateKey key = createJPrivateKey();
			if (key == null)
				return null;
			Cipher cipher = createCipher(padding);
			if (cipher == null)
				return null;
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(buff, ofst, len);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nonnull
	public static MyX509Key fromECPublicKey(@Nonnull byte[] buff, int buffOfst, int buffSize, @Nonnull byte[] paramOID, int oidOfst, int oidLen)
	{
		ASN1PDUBuilder pdu = new ASN1PDUBuilder();
		pdu.beginSequence();
		pdu.beginSequence();
		pdu.appendOIDString("1.2.840.10045.2.1");
		pdu.appendOID(paramOID, oidOfst, oidLen);
		pdu.endLevel();
		pdu.appendBitString((byte)0, buff, buffOfst, buffSize);
		pdu.endLevel();
		return new MyX509Key("ECPublic.key", pdu.getBuff(), 0, pdu.getBuffSize(), KeyType.ECPublic);
	}

	@Nonnull 
	public static MyX509Key createRSAPublicKey(@Nonnull String name, @Nonnull byte[] modulus, int modulusOfst, int modulusSize, @Nonnull byte[] publicExponent, int publicExponentOfst, int publicExponentSize)
	{
		ASN1PDUBuilder builder = new ASN1PDUBuilder();
		builder.beginSequence();
		builder.appendOther((byte)ASN1Util.IT_INTEGER, modulus, modulusOfst, modulusSize);
		builder.appendOther((byte)ASN1Util.IT_INTEGER, publicExponent, publicExponentOfst, publicExponentSize);
		builder.endLevel();
		return new MyX509Key(name, builder.getBuff(), 0, builder.getBuffSize(), KeyType.RSAPublic);
	}
}
