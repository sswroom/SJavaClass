package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONNumber;
import org.sswr.util.data.JSONObject;
import org.sswr.util.data.JSONString;
import org.sswr.util.data.textbinenc.Base64Enc;

public class JWToken
{
	public enum VerifyType
	{
		Unknown,
		Azure,
		Password,
		Key
	};

	private JWSignature.Algorithm alg;
	private String header;
	private String payload;
	private byte[] sign;

	private JWToken(JWSignature.Algorithm alg)
	{
		this.alg = alg;
		this.header = null;
		this.payload = null;
		this.sign = null;
	}

	private void setHeader(String header)
	{
		this.header = header;
	}

	private void setPayload(String payload)
	{
		this.payload = payload;
	}

	private void setSignature(byte[] sign)
	{
		this.sign = sign;
	}

	public JWSignature.Algorithm getAlgorithm()
	{
		return this.alg;
	}

	public String getHeader()
	{
		return this.header;
	}

	public String getPayload()
	{
		return this.payload;
	}

	public VerifyType getVerifyType(JWTParam param)
	{
		String s = param.getIssuer();
		if (s != null && s.startsWith("https://login.microsoftonline.com/"))
		{
			return VerifyType.Azure;
		}
		else
		{
			switch (this.alg)
			{
			case HS256:
			case HS384:
			case HS512:
				return VerifyType.Password;
			case RS256:
			case RS384:
			case RS512:
				return VerifyType.Key;
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
				return VerifyType.Unknown;
			}
		}
	}

	public boolean signatureValid(byte[] key, int keyOfst, int keyLeng, MyX509Key.KeyType keyType)
	{
		Base64Enc b64 = new Base64Enc(Base64Enc.B64Charset.URL, true);
		StringBuilder sb = new StringBuilder();
		b64.encodeBin(sb, this.header.getBytes(StandardCharsets.UTF_8));
		sb.append('.');
		b64.encodeBin(sb, this.payload.getBytes(StandardCharsets.UTF_8));
		JWSignature sign = new JWSignature(this.alg, key, keyOfst, keyLeng, keyType);
		byte[] buff = sb.toString().getBytes(StandardCharsets.UTF_8);
		return sign.verifyHash(buff, 0, buff.length, this.sign, 0, this.sign.length);
	}

	public Map<String, String> parsePayload(JWTParam param, boolean keepDefault, StringBuilder sbErr)
	{
		param.clear();
		JSONBase payloadJson = JSONBase.parseJSONStr(this.payload);
		if (payloadJson == null)
		{
			if (sbErr != null) sbErr.append("Payload cannot be parsed with JSON");
			return null;
		}
		if (!(payloadJson instanceof JSONObject))
		{
			if (sbErr != null) sbErr.append("Payload is not JSON object");
			return null;
		}
		StringBuilder sb = new StringBuilder();
		Map<String, String> retMap = new HashMap<String, String>();
		JSONObject payloadObj = (JSONObject)payloadJson;
		JSONBase json;
		Set<String> objNames = payloadObj.getObjectNames();
		String name;
		boolean isDefault;
		Iterator<String> it = objNames.iterator();
		while (it.hasNext())
		{
			name = it.next();
			json = payloadObj.getObjectValue(name);

			isDefault = true;
			if (name.equals("iss"))
			{
				if (json != null && json instanceof JSONString)
				{
					param.setIssuer(((JSONString)json).getValue());
				}
			}
			else if (name.equals("sub"))
			{
				if (json != null && json instanceof JSONString)
				{
					param.setSubject(((JSONString)json).getValue());
				}
			}
			else if (name.equals("aud"))
			{
				if (json != null && json instanceof JSONString)
				{
					param.setAudience(((JSONString)json).getValue());
				}
			}
			else if (name.equals("exp"))
			{
				if (json != null && json instanceof JSONNumber)
				{
					param.setExpirationTime(((JSONNumber)json).getAsInt64());
				}
			}
			else if (name.equals("nbf"))
			{
				if (json != null && json instanceof JSONNumber)
				{
					param.setNotBefore(((JSONNumber)json).getAsInt64());
				}
			}
			else if (name.equals("iat"))
			{
				if (json != null && json instanceof JSONNumber)
				{
					param.setIssuedAt(((JSONNumber)json).getAsInt64());
				}
			}
			else if (name.equals("jti"))
			{
				if (json != null && json instanceof JSONString)
				{
					param.setJWTId(((JSONString)json).getValue());
				}
			}
			else
			{
				isDefault = false;
			}
			if (keepDefault || !isDefault)
			{
				if (json == null)
				{
					retMap.put(name, null);
				}
				else if (json instanceof JSONString)
				{
					retMap.put(name, ((JSONString)json).getValue());
				}
				else
				{
					sb.setLength(0);
					retMap.put(name, json.toJSONString());
				}
			}
		}
		return retMap;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		Base64Enc b64 = new Base64Enc(Base64Enc.B64Charset.URL, true);
		b64.encodeBin(sb, this.header.getBytes(StandardCharsets.UTF_8));
		sb.append('.');
		b64.encodeBin(sb, this.payload.getBytes(StandardCharsets.UTF_8));
		sb.append('.');
		b64.encodeBin(sb, this.sign);
		return sb.toString();
	}

	public static JWToken generate(JWSignature.Algorithm alg, String payload, byte[] key, int keyOfst, int keyLeng, MyX509Key.KeyType keyType)
	{
		String header = "{\"alg\":\"" + alg.toString() + "\",\"typ\":\"JWT\"}";
		StringBuilder sb = new StringBuilder();
		Base64Enc b64 = new Base64Enc(Base64Enc.B64Charset.URL, true);
		b64.encodeBin(sb, header.getBytes(StandardCharsets.UTF_8));
		sb.append('.');

		b64.encodeBin(sb, payload.getBytes(StandardCharsets.UTF_8));
		JWSignature sign = new JWSignature(alg, key, keyOfst, keyLeng, keyType);
		if (!sign.calcHash(sb.toString().getBytes(StandardCharsets.UTF_8)))
		{
			return null;
		}
		JWToken token = new JWToken(alg);
		token.setHeader(header);
		token.setPayload(payload);
		token.setSignature(sign.getSignature());
		return token;
	}

	public static JWToken parse(String token, StringBuilder sbErr)
	{
		int i1 = token.indexOf('.');;
		if (i1 == -1)
		{
			if (sbErr != null) sbErr.append("Token format error: no . found");
			return null;
		}
		int i2 = token.indexOf('.', i1 + 1);
		if (i2 == -1)
		{
			if (sbErr != null) sbErr.append("Token format error: Only 1 . found");
			return null;
		}
		if (token.indexOf('.', i2 + 1) != -1)
		{
			if (sbErr != null) sbErr.append("Token format error: More than 2 . found");
			return null;
		}
		Base64Enc b64url = new Base64Enc(Base64Enc.B64Charset.URL, true);
		byte[] header = b64url.decodeBin(token.substring(0, i1));
		byte[] payload = b64url.decodeBin(token.substring(i1 + 1, i2));
		byte[] sign = b64url.decodeBin(token.substring(i2 + 1));
		JWSignature.Algorithm alg;
		JSONBase json = JSONBase.parseJSONStr(new String(header, StandardCharsets.UTF_8));
		if (json == null)
		{
			if (sbErr != null) sbErr.append("Token format error: header is not JSON");
			return null;
		}
		else if (!(json instanceof JSONObject))
		{
			if (sbErr != null) sbErr.append("Token format error: header JSON is not object");
			return null;
		}
		String sAlg = json.getValueString("alg");
		if (sAlg == null)
		{
			if (sbErr != null) sbErr.append("Token format error: alg is not found");
			return null;
		}
		alg = JWSignature.algorithmGetByName(sAlg);
		if (alg == JWSignature.Algorithm.Unknown)
		{
			if (sbErr != null) sbErr.append("Token format error: alg is not supported");
			return null;
		}
		json = JSONBase.parseJSONStr(new String(payload, StandardCharsets.UTF_8));
		if (json == null)
		{
			if (sbErr != null) sbErr.append("Token format error: payload is not JSON");
			return null;
		}
		else if (!(json instanceof JSONObject))
		{
			if (sbErr != null) sbErr.append("Token format error: payload JSON is not object");
			return null;
		}
		JWToken jwt = new JWToken(alg);
		jwt.setHeader(new String(header, StandardCharsets.UTF_8));
		jwt.setPayload(new String(payload, StandardCharsets.UTF_8));
		jwt.setSignature(sign);
		return jwt;
	}
}
