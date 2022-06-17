package org.sswr.util.crypto;

import java.util.Arrays;

import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.ASN1PDUBuilder;

public class MyX509Key extends MyX509File
{
	private KeyType keyType;

	public MyX509Key(String sourceName, byte[] buff, int ofst, int size, KeyType keyType)
	{
		super(sourceName, buff, ofst, size);
		this.keyType = keyType;
	}

	@Override
	public FileType getFileType()
	{
		return FileType.Key;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509Key(this.sourceName, this.buff, 0, this.buff.length, this.keyType);
	}

	@Override
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

	MyX509Key createPublicKey()
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
	
	private byte[] toBuff(ASN1Item item)
	{
		if (item == null)
			return null;
		return Arrays.copyOfRange(this.buff, item.ofst, item.ofst + item.len);
	}

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
	
	public byte[] getRSAPrivateExponent()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.4"));
	}
	
	public byte[] getRSAPrime1()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.5"));
	}
	
	public byte[] getRSAPrime2()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.6"));
	}
	
	public byte[] getRSAExponent1()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.7"));
	}
	
	public byte[] getRSAExponent2()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.8"));
	}
	
	public byte[] getRSACoefficient()
	{
		if (this.keyType != KeyType.RSA) return null;
		return toBuff(ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.9"));
	}
}
