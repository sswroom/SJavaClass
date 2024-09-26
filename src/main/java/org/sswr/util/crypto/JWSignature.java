package org.sswr.util.crypto;

import java.util.Arrays;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.EncodingException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JWSignature
{
	public enum Algorithm
	{
		Unknown,
		HS256,
		HS384,
		HS512,
		PS256,
		PS384,
		PS512,
		RS256,
		RS384,
		RS512,
		ES256,
		ES256K,
		ES384,
		ES512,
		EdDSA
	}

	private Algorithm alg;
	private MyX509Key.KeyType keyType;
	private byte[] privateKey;
	private byte[] hashVal;

	public JWSignature(@Nonnull Algorithm alg, @Nonnull byte[] privateKey, int privateKeyOfst, int privateKeyLen, @Nonnull MyX509Key.KeyType keyType)
	{
		this.alg = alg;
		this.privateKey = Arrays.copyOfRange(privateKey, privateKeyOfst, privateKeyLen);
		this.keyType = keyType;
		this.hashVal = null;
	}

	public boolean calcHash(@Nonnull byte[] buff)
	{
		return calcHash(buff, 0, buff.length);
	}

	public boolean calcHash(@Nonnull byte[] buff, int buffOfst, int buffSize)
	{
		Hash hash;
		switch (this.alg)
		{
		case HS256:
			hash = new HMAC(new SHA256(), this.privateKey, 0, this.privateKey.length);
			break;
		case HS384:
			hash = new HMAC(new SHA384(), this.privateKey, 0, this.privateKey.length);
			break;
		case HS512:
			hash = new HMAC(new SHA512(), this.privateKey, 0, this.privateKey.length);
			break;
		case RS256:
		case RS384:
		case RS512:
			{
				if (this.keyType != MyX509Key.KeyType.RSA)
				{
					return false;
				}
				MyX509Key key;
				key = new MyX509Key("rsakey", this.privateKey, 0, this.privateKey.length, this.keyType);
				if (alg == Algorithm.RS256)
					this.hashVal = key.signature(HashType.SHA256, buff, buffOfst, buffSize);
				else if (alg == Algorithm.RS384)
					this.hashVal = key.signature(HashType.SHA384, buff, buffOfst, buffSize);
				else if (alg == Algorithm.RS512)
					this.hashVal = key.signature(HashType.SHA512, buff, buffOfst, buffSize);
				return this.hashVal != null;
			}
		case PS256:
		case PS384:
		case PS512:
		case ES256:
		case ES256K:
		case ES384:
		case ES512:
		case EdDSA:
		case Unknown:
		default:
			return false;
		}
	
		hash.calc(buff, buffOfst, buffSize);
		this.hashVal = hash.getValue();
		return true;
	}

	public boolean verifyHash(@Nonnull byte[] buff, int buffOfst, int buffSize, @Nonnull byte[] signature, int signOfst, int signatureSize)
	{
		Hash hash;
		switch (this.alg)
		{
		case HS256:
			hash = new HMAC(new SHA256(), this.privateKey, 0, this.privateKey.length);
			break;
		case HS384:
			hash = new HMAC(new SHA384(), this.privateKey, 0, this.privateKey.length);
			break;
		case HS512:
			hash = new HMAC(new SHA512(), this.privateKey, 0, this.privateKey.length);
			break;
		case RS256:
		case RS384:
		case RS512:
			{
				if ((this.keyType != MyX509Key.KeyType.RSA && this.keyType != MyX509Key.KeyType.RSAPublic))
				{
					return false;
				}
				MyX509Key key;
				boolean succ = false;
				key = new MyX509Key("rsakey", this.privateKey, 0, this.privateKey.length, this.keyType);
				if (alg == Algorithm.RS256)
					succ = key.signatureVerify(HashType.SHA256, buff, buffOfst, buffSize, signature, signOfst, signatureSize);
				else if (alg == Algorithm.RS384)
					succ = key.signatureVerify(HashType.SHA384, buff, buffOfst, buffSize, signature, signOfst, signatureSize);
				else if (alg == Algorithm.RS512)
					succ = key.signatureVerify(HashType.SHA512, buff, buffOfst, buffSize, signature, signOfst, signatureSize);
				return succ;
			}
		case PS256:
		case PS384:
		case PS512:
		case ES256:
		case ES256K:
		case ES384:
		case ES512:
		case EdDSA:
		case Unknown:
		default:
			return false;
		}
	
		if (signatureSize != hash.getResultSize())
		{
			return false;
		}
		byte[] hashVal;
		hash.calc(buff, buffOfst, buffSize);
		hashVal = hash.getValue();
		return ByteTool.byteEquals(signature, signOfst, hashVal, 0, signatureSize);
	}

	@Nullable
	public String getHashB64()
	{
		if (this.hashVal == null)
			return null;
		Base64Enc b64 = new Base64Enc(Base64Enc.B64Charset.URL, true);
		try
		{
			return b64.encodeBin(this.hashVal);
		}
		catch (EncodingException ex)
		{
			return null;
		}
	}

	@Nullable
	public byte[] getSignature()
	{
		return this.hashVal;
	}

	@Nonnull
	public static Algorithm algorithmGetByName(@Nonnull String name)
	{
		Algorithm alg = DataTools.getEnum(Algorithm.class, name.toUpperCase());
		if (alg == null)
			return Algorithm.Unknown;
		return alg;
	}
}
