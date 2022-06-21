package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONBuilder;
import org.sswr.util.data.JSONParser;
import org.sswr.util.data.JSONBuilder.ObjectType;

public class JWTHandler
{
	public enum Algorithm
	{
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
	private byte privateKey[];

	private JWTHandler(Algorithm alg, byte privateKey[])
	{
		this.alg = alg;
		this.privateKey = privateKey;
	}

	public String generate(Map<String, String> payload, JWTParam param)
	{
		Encoder b64url = Base64.getUrlEncoder().withoutPadding();
		StringBuilder sb = new StringBuilder();
		JSONBuilder json = new JSONBuilder(ObjectType.OT_OBJECT);
		sb.append(b64url.encodeToString(("{\"alg\":\""+this.alg.toString()+"\",\"typ\":\"JWT\"}").getBytes(StandardCharsets.UTF_8)));
		sb.append('.');
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
		sb.append(b64url.encodeToString(json.toString().getBytes(StandardCharsets.UTF_8)));
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
		default:
			return null;
		}
		byte buff[] = sb.toString().getBytes(StandardCharsets.UTF_8);
		hash.calc(buff, 0, buff.length);
		sb.append('.');
		sb.append(b64url.encodeToString(hash.getValue()));
		return sb.toString();
	}

	public Map<String, String> parse(String token, JWTParam param)
	{
		int i1 = token.indexOf(".");
		int i2 = token.indexOf(".", i1 + 1);
		int i3 = token.indexOf(".", i2 + 1);
		if (i2 < 0 || i3 >= 0)
		{
			System.out.println("token format invalid");
			return null;
		}
		Decoder b64urldec = Base64.getUrlDecoder();
		String header = new String(b64urldec.decode(token.substring(0, i1)), StandardCharsets.UTF_8);
		String payload = new String(b64urldec.decode(token.substring(i1 + 1, i2)), StandardCharsets.UTF_8);
		Object hdrJson = JSONParser.parse(header);
		Algorithm tokenAlg = null;
		if (hdrJson != null && hdrJson instanceof Map)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> hdrMap = (Map<String, Object>)hdrJson;
			Object algJson = hdrMap.get("alg");
			if (algJson != null)
			{
				tokenAlg = DataTools.getEnum(Algorithm.class, algJson.toString());
			}
		}
//		System.out.println("Token Alg = "+tokenAlg);
//		System.out.println("Handler Alg = "+this.alg);
//		System.out.println("Header: "+header);
//		System.out.println("payload: "+payload);
		if (this.alg != tokenAlg)
		{
			System.out.println("Token algorithm mismatch");
			return null;
		}
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
		default:
			System.out.println("Token algorithm not supported");
			return null;
		}
		Encoder b64url = Base64.getUrlEncoder().withoutPadding();
		byte buff[] = token.substring(0, i2).getBytes(StandardCharsets.UTF_8);
		hash.calc(buff, 0, buff.length);
		if (!b64url.encodeToString(hash.getValue()).equals(token.substring(i2 + 1)))
		{
			System.out.println("Token hash mismatch");
			return null;
		}

		param.clear();
		Object payloadJson = JSONParser.parse(payload);
		if (payloadJson == null || !(payloadJson instanceof Map))
		{
			System.out.println("Token payload missing");
			return null;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> payloadMap = (Map<String, Object>)payloadJson;
		Map<String, String> retMap = new HashMap<String, String>();
		Object json;
		Iterator<String> itNames = payloadMap.keySet().iterator();
		String name;
		while (itNames.hasNext())
		{
			name = itNames.next();
			json = payloadMap.get(name);
			switch (name)
			{
			case "iss":
				if (json != null && json.getClass().equals(String.class))
				{
					param.setIssuer((String)json);
				}
				break;
			case "sub":
				if (json != null && json.getClass().equals(String.class))
				{
					param.setSubject((String)json);
				}
				break;
			case "aud":
				if (json != null && json.getClass().equals(String.class))
				{
					param.setAudience((String)json);
				}
				break;
			case "exp":
				if (json != null)
				{
					if (json instanceof Integer)
					{
						param.setExpirationTime(((Integer)json).intValue());
					}
					else if (json instanceof Long)
					{
						param.setExpirationTime(((Long)json).longValue());
					}
				}
				break;
			case "nbf":
				if (json != null)
				{
					if (json instanceof Integer)
					{
						param.setNotBefore(((Integer)json).intValue());
					}
					else if (json instanceof Long)
					{
						param.setNotBefore(((Long)json).longValue());
					}
				}
				break;
			case "iat":
				if (json != null)
				{
					if (json instanceof Integer)
					{
						param.setIssuedAt(((Integer)json).intValue());
					}
					else if (json instanceof Long)
					{
						param.setIssuedAt(((Long)json).longValue());
					}
				}
				break;
			case "jti":
				if (json != null && json.getClass().equals(String.class))
				{
					param.setJWTId((String)json);
				}
				break;
			default:
				if (json == null)
				{
					retMap.put(name, null);
				}
				else if (json.getClass().equals(String.class))
				{
					retMap.put(name, (String)json);
				}
				else
				{
					retMap.put(name, json.toString());
				}
				break;
			}
		}
		return retMap;
	}

	public static JWTHandler createHMAC(Algorithm alg, byte key[])
	{
		switch (alg)
		{
		case HS256:
		case HS384:
		case HS512:
			return new JWTHandler(alg, key);
		default:
			return null;
		}
	}
}
