package org.sswr.util.crypto;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.sswr.util.data.JSONBuilder;
import org.sswr.util.data.JSONBuilder.ObjectType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JWTHandler
{
	private JWSignature.Algorithm alg;
	private byte key[];
	private MyX509Key.KeyType keyType;

	private JWTHandler(@Nonnull JWSignature.Algorithm alg, @Nonnull byte key[], int keyOfst, int keyLen, @Nonnull MyX509Key.KeyType keyType)
	{
		this.alg = alg;
		this.key = Arrays.copyOfRange(key, keyOfst, keyLen);
		this.keyType = keyType;
	}

	@Nullable
	public String generate(@Nonnull Map<String, String> payload, @Nullable JWTParam param)
	{
		JSONBuilder json = new JSONBuilder(ObjectType.OT_OBJECT);
		Iterator<String> itKey = payload.keySet().iterator();
		String name;
		while (itKey.hasNext())
		{
			name = itKey.next();
			json.objectAddStr(name, payload.get(name));
		}
		if (param != null)
		{
			if (param.getIssuer() != null)
			{
				json.objectAddStr("iss", param.getIssuer());
			}
			if (param.getSubject() != null)
			{
				json.objectAddStr("sub", param.getSubject());
			}
			if (param.getAudience() != null)
			{
				json.objectAddStr("aud", param.getAudience());
			}
			if (param.getExpirationTime() != 0)
			{
				json.objectAddInt64("exp", param.getExpirationTime());
			}
			if (param.getNotBefore() != 0)
			{
				json.objectAddInt64("nbf", param.getNotBefore());
			}
			if (param.getIssuedAt() != 0)
			{
				json.objectAddInt64("iat", param.getIssuedAt());
			}
			if (param.getJWTId() != null)
			{
				json.objectAddStr("jti", param.getJWTId());
			}
		}
		JWToken token = JWToken.generate(alg, json.toString(), this.key, 0, this.key.length, this.keyType);
		if (token == null)
		{
			return null;
		}
		return token.toString();
	}

	@Nullable
	public static JWTHandler createHMAC(@Nonnull JWSignature.Algorithm alg, @Nonnull byte key[])
	{
		switch (alg)
		{
		case HS256:
		case HS384:
		case HS512:
			return new JWTHandler(alg, key, 0, key.length, MyX509Key.KeyType.Unknown);
		default:
			return null;
		}
	}
}
