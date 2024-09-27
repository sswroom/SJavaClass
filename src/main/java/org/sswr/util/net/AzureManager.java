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
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class AzureManager
{
	private boolean debug = true;
	private LogTool errLog;
	private Map<String, String> keyMap;
	private SocketFactory sockf;
	private SSLEngine ssl;

	public AzureManager()
	{
		this.errLog = null;
		if (debug) this.errLog = new LogTool().addPrintLog(System.out, LogLevel.RAW);
		this.sockf = null;
		this.ssl = null;
		this.keyMap = null;
	}

	public AzureManager(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl)
	{
		this.errLog = null;
		if (debug) this.errLog = new LogTool().addPrintLog(System.out, LogLevel.RAW);
		this.sockf = sockf;
		this.ssl = ssl;
		this.keyMap = null;
	}

	public void setErrorLog(@Nullable LogTool log)
	{
		this.errLog = log;
	}

	@Nullable
	public MyX509Key createKey(@Nonnull String kid)
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
							if (key != null)
							{
								String skid = key.getValueString("kid");
								String cert = key.getValueString("x5c[0]");
								if (skid != null && cert != null)
								{
									this.keyMap.put(skid, cert);
								}
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

	@Nullable
	public AzureToken parseToken(@Nonnull String token, boolean ignoreSignCheck, boolean ignoreTimeCheck)
	{
		JWToken jwt = JWToken.parse(token, null);
		if (jwt == null)
		{
			if (errLog != null) errLog.logMessage("Error in parsing token", LogLevel.ERROR);
			return null;
		}
		else
		{
			if (!ignoreSignCheck)
			{
				String header = jwt.getHeader();
				if (header == null)
				{
					if (errLog != null) errLog.logMessage("Header not found", LogLevel.ERROR);
					return null;
				}
				JSONBase json = JSONBase.parseJSONStr(header);
				if (json == null)
				{
					if (errLog != null) errLog.logMessage("Error in parsing header", LogLevel.ERROR);
					return null;
				}
				String kid = json.getValueString("kid");
				if (kid == null)
				{
					if (errLog != null) errLog.logMessage("kid not found in header", LogLevel.ERROR);
					return null;
				}
				MyX509Key key = createKey(kid);
				if (key == null)
				{
					if (errLog != null) errLog.logMessage("key not found in header", LogLevel.ERROR);
					return null;
				}
				if (!jwt.signatureValid(key.getASN1Buff(), 0, key.getASN1BuffSize(), key.getKeyType()))
				{
					if (errLog != null) errLog.logMessage("Signature not valid", LogLevel.ERROR);
					return null;
				}
			}
			JWTParam param = new JWTParam();
			Map<String, String> result = jwt.parsePayload(param, false, null);
			if (result == null)
			{
				if (errLog != null) errLog.logMessage("Error in parsing payload", LogLevel.ERROR);
				return null;
			}
			if (!ignoreTimeCheck)
			{
				if (param.isExpired(System.currentTimeMillis()))
				{
					if (errLog != null) errLog.logMessage("Expired", LogLevel.ERROR);
					return null;
				}
			}
			return new AzureToken(result);
		}
	}
}
