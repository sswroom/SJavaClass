package org.sswr.util.net;

import java.util.HashMap;
import java.util.Map;

import org.sswr.util.crypto.JWTParam;
import org.sswr.util.crypto.JWToken;
import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.crypto.MyX509Key;
import org.sswr.util.data.JSONArray;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONObject;
import org.sswr.util.data.textbinenc.Base64Enc;

public class AzureManager
{
	private boolean debug = true;
	private Map<String, String> keyMap;
	private SocketFactory sockf;
	private SSLEngine ssl;

	public AzureManager()
	{
		this.sockf = null;
		this.ssl = null;
		this.keyMap = null;
	}

	public AzureManager(SocketFactory sockf, SSLEngine ssl)
	{
		this.sockf = sockf;
		this.ssl = ssl;
		this.keyMap = null;
	}

	public MyX509Key createKey(String kid)
	{
		if (this.keyMap == null)
		{
			this.keyMap = new HashMap<String, String>();
			StringBuilder sb = new StringBuilder();
			if (HTTPClient.loadContent(this.sockf, this.ssl, "https://login.microsoftonline.com/common/discovery/v2.0/keys", sb, 1048576))
			{
				JSONBase json = JSONBase.parseJSONStr(sb.toString());
				if (json != null && json instanceof JSONObject)
				{
					JSONArray keys = json.getValueArray("keys");
					if (keys != null)
					{
						int i = 0;
						int j = keys.getArrayLength();
						while (i < j)
						{
							JSONBase key = keys.getArrayValue(i);
							String skid = key.getValueString("kid");
							String cert = key.getValueString("x5c[0]");
							if (skid != null && cert != null)
							{
								this.keyMap.put(skid, cert);
							}
							i++;
						}
					}
				}
			}
		}
		String s = this.keyMap.get(kid);
		if (s == null)
		{
			return null;
		}
		Base64Enc b64 = new Base64Enc();
		byte []keyBuff = b64.decodeBin(s);
		MyX509Cert cert = new MyX509Cert(kid, keyBuff, 0, keyBuff.length);
		return cert.getNewPublicKey();        
	}

	public AzureToken parseToken(String token, boolean ignoreSignCheck, boolean ignoreTimeCheck)
	{
		JWToken jwt = JWToken.parse(token, null);
		if (jwt == null)
		{
			if (debug) System.out.println("Error in parsing token");
			return null;
		}
		else
		{
			if (!ignoreSignCheck)
			{
				JSONBase json = JSONBase.parseJSONStr(jwt.getHeader());
				if (json == null)
				{
					if (debug) System.out.println("Error in parsing header");
					return null;
				}
				String kid = json.getValueString("kid");
				if (kid == null)
				{
					if (debug) System.out.println("kid not found in header");
					return null;
				}
				MyX509Key key = createKey(kid);
				if (key == null)
				{
					if (debug) System.out.println("key not found in header");
					return null;
				}
				if (!jwt.signatureValid(key.getASN1Buff(), 0, key.getASN1BuffSize(), key.getKeyType()))
				{
					if (debug) System.out.println("Signature not valid");
					return null;
				}
			}
			JWTParam param = new JWTParam();
			Map<String, String> result = jwt.parsePayload(param, false, null);
			if (result == null)
			{
				if (debug) System.out.println("Error in parsing payload");
				return null;
			}
			if (!ignoreTimeCheck)
			{
				if (param.isExpired(System.currentTimeMillis()))
				{
					if (debug) System.out.println("Expired");
					return null;
				}
			}
			return new AzureToken(result);
		}
	}
}
