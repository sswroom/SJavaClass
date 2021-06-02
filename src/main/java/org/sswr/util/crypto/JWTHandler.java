package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONBuilder;
import org.sswr.util.data.JSONNumber;
import org.sswr.util.data.JSONObject;
import org.sswr.util.data.JSONString;
import org.sswr.util.data.JSONBase.JSType;
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
			return null;
		}
		Decoder b64urldec = Base64.getUrlDecoder();
		String header = new String(b64urldec.decode(token.substring(0, i1)), StandardCharsets.UTF_8);
		String payload = new String(b64urldec.decode(token.substring(i1 + 1, i2)), StandardCharsets.UTF_8);
		JSONBase hdrJson = JSONBase.parseJSONStr(header);
		Algorithm tokenAlg = null;
		if (hdrJson != null && hdrJson.getJSType() == JSType.OBJECT)
		{
			JSONBase algJson = ((JSONObject)hdrJson).getObjectValue("alg");
			if (algJson != null && algJson.getJSType() == JSType.STRING)
			{
				tokenAlg = DataTools.getEnum(Algorithm.class, ((JSONString)algJson).getValue());
			}
		}
//		System.out.println("Token Alg = "+tokenAlg);
//		System.out.println("Handler Alg = "+this.alg);
//		System.out.println("Header: "+header);
//		System.out.println("payload: "+payload);
		if (this.alg != tokenAlg)
		{
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
			return null;
		}
		Encoder b64url = Base64.getUrlEncoder().withoutPadding();
		byte buff[] = token.substring(0, i2).getBytes(StandardCharsets.UTF_8);
		hash.calc(buff, 0, buff.length);
		if (!b64url.encodeToString(hash.getValue()).equals(token.substring(i2 + 1)))
		{
			return null;
		}

		param.clear();
		JSONBase payloadJson = JSONBase.parseJSONStr(payload);
		if (payloadJson == null || payloadJson.getJSType() != JSType.OBJECT)
		{
			return null;
		}
		Map<String, String> retMap = new HashMap<String, String>();
		JSONObject payloadObj = (JSONObject)payloadJson;
		JSONBase json;
		Iterator<String> itNames = payloadObj.getObjectNames().iterator();
		String name;
		while (itNames.hasNext())
		{
			name = itNames.next();
			json = payloadObj.getObjectValue(name);
			switch (name)
			{
			case "iss":
				if (json != null && json.getJSType() == JSType.STRING)
				{
					param.setIssuer(((JSONString)json).getValue());
				}
				break;
			case "sub":
				if (json != null && json.getJSType() == JSType.STRING)
				{
					param.setSubject(((JSONString)json).getValue());
				}
				break;
			case "aud":
				if (json != null && json.getJSType() == JSType.STRING)
				{
					param.setAudience(((JSONString)json).getValue());
				}
				break;
			case "exp":
				if (json != null && json.getJSType() == JSType.NUMBER)
				{
					param.setExpirationTime((long)((JSONNumber)json).getValue());
				}
				break;
			case "nbf":
				if (json != null && json.getJSType() == JSType.NUMBER)
				{
					param.setNotBefore((long)((JSONNumber)json).getValue());
				}
				break;
			case "iat":
				if (json != null && json.getJSType() == JSType.NUMBER)
				{
					param.setIssuedAt((long)((JSONNumber)json).getValue());
				}
				break;
			case "jti":
				if (json != null && json.getJSType() == JSType.STRING)
				{
					param.setJWTId(((JSONString)json).getValue());
				}
				break;
			default:
				if (json == null)
				{
					retMap.put(name, null);
				}
				else if (json.getJSType() == JSType.STRING)
				{
					retMap.put(name, ((JSONString)json).getValue());
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
