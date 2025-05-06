package org.sswr.util.net;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.sswr.util.crypto.cert.CipherPadding;
import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.crypto.cert.MyX509Key;
import org.sswr.util.crypto.cert.MyX509PrivKey;
import org.sswr.util.crypto.cert.MyX509PubKey;
import org.sswr.util.crypto.encrypt.AES256GCM;
import org.sswr.util.crypto.encrypt.EncryptionException;
import org.sswr.util.crypto.hash.HMAC;
import org.sswr.util.crypto.hash.SHA256;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.JSONBase;
import org.sswr.util.data.JSONBuilder;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.RandomBytesGenerator;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.JSONBuilder.ObjectType;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class IAMSmartAPI {
	private static LogTool logger = null;
	public static void setLog(LogTool log)
	{
		logger = log;
	}

	public static class CEKInfo
	{
		public byte[] key;
		public long issueAt;
		public long expiresAt;
	};

	public static class TokenInfo
	{
		public String accessToken;
		public String openID;
		public long issueAt;
		public long expiresAt;
		public long lastModifiedDate;
	};

	public static class ProfileInfo
	{
		public String hkid;
		public char hkidChk;
		public String prefix;
		public String enName;
		public String chName;
		public String chNameVerfied;
		public int birthDate;
		public char gender;
		public char maritalStatus;
		public String homeTelNumberICC;
		public String homeTelNumber;
		public String officeTelNumberICC;
		public String officeTelNumber;
		public String mobileTelNumberICC;
		public String mobileTelNumber;
		public String emailAddress;
		public String residentialAddress;
		public String postalAddress;
		public char educationLevel;
	};

	private TCPClientFactory clif;
	private SSLEngine ssl;
	private String domain;
	private String clientID;
	private String clientSecret;
	private RandomBytesGenerator rand;

	private void initHTTPClient(@Nonnull HTTPClient cli, @Nonnull String content)
	{
		byte[] buff;
		String signatureMethod = "HmacSHA256";
		long timestamp = System.currentTimeMillis();
		SHA256 sha256 = new SHA256();
		byte[] cliSecBuff = this.clientSecret.getBytes(StandardCharsets.UTF_8);
		HMAC hmac = new HMAC(sha256, cliSecBuff, 0, cliSecBuff.length);
		hmac.calc(this.clientID.getBytes(StandardCharsets.UTF_8));
		hmac.calc(signatureMethod.getBytes(StandardCharsets.UTF_8));
		cli.addHeader("clientID", this.clientID);
		cli.addHeader("signatureMethod", signatureMethod);
		buff = String.valueOf(timestamp).getBytes(StandardCharsets.UTF_8);
		hmac.calc(buff, 0, buff.length);
		cli.addHeader("timestamp", String.valueOf(timestamp));
		buff = this.rand.nextBytes(18);
		hmac.calc(StringUtil.toHex(buff).getBytes(StandardCharsets.UTF_8));
		if (content.length() > 0)
		{
			hmac.calc(content.getBytes(StandardCharsets.UTF_8));
			cli.addContentType("application/json");
		}
		cli.addHeader("nonce", StringUtil.toHex(buff));
		buff = hmac.getValue();
		Base64Enc b64 = new Base64Enc();
		String s = b64.encodeBin(buff, 0, buff.length);
		cli.addHeader("signature", FormEncoding.formEncode(s));
		cli.addContentLength(content.length());
		if (content.length() > 0)
		{
			cli.write(content.getBytes(StandardCharsets.UTF_8));
		}		
	}

	private JSONBase postEncReq(@Nonnull String url, @Nonnull CEKInfo cek, @Nonnull String jsonMsg)
	{
		RandomBytesGenerator byteGen = new RandomBytesGenerator();
		byte[] jsonMsgBuff = jsonMsg.getBytes(StandardCharsets.UTF_8);
		byte[] msgBuff = new byte[jsonMsgBuff.length + 32];
		ByteTool.writeMInt32(msgBuff, 0, 12);
		ByteTool.copyArray(msgBuff, 4, byteGen.nextBytes(12), 0, 12);
		AES256GCM aes = new AES256GCM(cek.key, 0, cek.key.length, msgBuff, 4);
		byte[] encBuff;
		try
		{
			encBuff = aes.encrypt(jsonMsgBuff);
		}
		catch (EncryptionException ex)
		{
			if (logger != null) logger.logException(ex);
			return null;
		}
		if (encBuff.length != jsonMsg.length() + 16)
		{
			return null;
		}
		ByteTool.copyArray(msgBuff, 16, encBuff, 0, encBuff.length);
		StringBuilder sb = new StringBuilder();
		Base64Enc b64 = new Base64Enc();
		sb.append("{\"content\":\"");
		b64.encodeBin(sb, msgBuff, 0, jsonMsgBuff.length + 32);
		sb.append("\"}");
		if (logger != null)
		{
			logger.logMessage("PostEncReq.Url: "+url, LogLevel.RAW);
			logger.logMessage("PostEncReq.Req: "+sb.toString(), LogLevel.RAW);
		}
		HTTPClient cli = HTTPClient.createConnect(this.clif, this.ssl, url, RequestMethod.HTTP_POST, false);
		this.initHTTPClient(cli, sb.toString());
		byte[] dataBuff = cli.readToEnd();
		int code = cli.getRespStatus();
		cli.close();
		if (logger != null)
		{
			logger.logMessage("PostEncReq.Status: "+code, LogLevel.RAW);
			logger.logMessage("PostEncReq.Content: "+new String(dataBuff, StandardCharsets.UTF_8), LogLevel.RAW);
		}
		if (code != 200)
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Status is not OK: "+code, LogLevel.ACTION);
			}
			return null;
		}
		JSONBase json;
		if ((json = JSONBase.parseJSONStr(new String(dataBuff, StandardCharsets.UTF_8))) == null)
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Response is not JSON", LogLevel.ACTION);
			}
			return null;
		}
		String s;
		if ((s = json.getValueString("message")) == null || !s.equals("SUCCESS"))
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Response is not success", LogLevel.ACTION);
			}
			return null;
		}
		if ((s = json.getValueString("content")) == null)
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Response content not found", LogLevel.ACTION);
			}
			return null;
		}
	
		msgBuff = b64.decodeBin(s);
		if (msgBuff == null || msgBuff.length < 32)
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Response content too short", LogLevel.ACTION);
			}
			return null;
		}
		if (ByteTool.readMInt32(msgBuff, 0) != 12)
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Response content IV not found", LogLevel.ACTION);
			}
			return null;
		}
		aes.setIV(msgBuff, 4);
		byte[] decBuff;
		try
		{
			decBuff = aes.decrypt(msgBuff, 16, msgBuff.length - 16);
		}
		catch (EncryptionException ex)
		{
			if (logger != null) logger.logException(ex);
			return null;
		}
		if (decBuff.length != msgBuff.length - 32)
		{
			if (logger != null)
			{
				logger.logMessage("PostEncReq: Decrypted length not valid", LogLevel.ACTION);
			}
			return null;
		}
		if (logger != null)
		{
			logger.logMessage("PostEncReq.Dec Content = "+new String(decBuff, StandardCharsets.UTF_8), LogLevel.RAW);
		}
		JSONBase decJSON = JSONBase.parseJSONStr(new String(decBuff, StandardCharsets.UTF_8));
		if (logger != null)
		{
			if (decJSON == null)
			{
				logger.logMessage("PostEncReq: Decrypted content is not JSON", LogLevel.ACTION);
			}
		}
		return decJSON;
	}

	@Nullable
	private String parseAddress(@Nonnull JSONBase json, @Nonnull String path)
	{
		JSONBase addr;
		if ((addr = json.getValue(path)) == null)
			return null;
		StringBuilder sb = new StringBuilder();
		String s;
		JSONBase obj;
		if ((obj = addr.getValue("ChiPremisesAddress")) != null)
		{
			sb.append(StringUtil.orEmpty(obj.getValueString("Region")));
			if ((s = obj.getValueString("ChiDistrict.Sub-district")) != null)
			{
				sb.append(" ");
				sb.append(s);
			}
			if ((s = obj.getValueString("ChiStreet.StreetName")) != null)
			{
				sb.append(" ");
				sb.append(s);
			}
			if ((s = obj.getValueString("ChiStreet.BuildingNoFrom")) != null)
			{
				sb.append(" ");
				sb.append(s);
				sb.append("號");
			}
			if ((s = obj.getValueString("ChiEstate.EstateName")) != null)
			{
				sb.append(" ");
				sb.append(s);
			}
			if ((s = obj.getValueString("ChiEstate.ChiPhase.PhaseNo")) != null)
			{
				sb.append(" 第");
				sb.append(s);
				sb.append("期");
			}
			if ((s = obj.getValueString("ChiBlock.BlockNo")) != null)
			{
				sb.append(" ");
				sb.append(s);
				sb.append(StringUtil.orEmpty(obj.getValueString("ChiBlock.BlockDescriptor")));
			}
			if ((s = obj.getValueString("BuildingName")) != null)
			{
				sb.append(" ");
				sb.append(s);
			}
			if ((s = obj.getValueString("Chi3dAddress.ChiFloor.FloorNum")) != null)
			{
				sb.append(" ");
				sb.append(s);
				sb.append("樓");
			}
			if ((s = obj.getValueString("Chi3dAddress.ChiUnit.UnitNo")) != null)
			{
				sb.append(" ");
				sb.append(s);
				sb.append(StringUtil.orEmpty(obj.getValueString("Chi3dAddress.ChiUnit.UnitDescriptor")));
			}
			return sb.toString();
		}
		else if ((obj = addr.getValue("EngPremisesAddress")) != null)
		{
			if (obj.getValue("Eng3dAddress.EngUnit") != null)
			{
				sb.append(StringUtil.orEmpty(obj.getValueString("Eng3dAddress.EngUnit.UnitDescriptor")));
				sb.append(' ');
				sb.append(StringUtil.orEmpty(obj.getValueString("Eng3dAddress.EngUnit.UnitNo")));
				sb.append(", ");
			}
			if ((s = obj.getValueString("Eng3dAddress.EngFloor.FloorNum")) != null)
			{
				sb.append(s);
				sb.append("/F ");
			}
			if (obj.getValue("EngBlock") != null)
			{
				sb.append(StringUtil.orEmpty(obj.getValueString("EngBlock.BlockDescriptor")));
				sb.append(' ');
				sb.append(StringUtil.orEmpty(obj.getValueString("EngBlock.BlockNo")));
				sb.append(", ");
			}
			sb.append(StringUtil.orEmpty(obj.getValueString("BuildingName")));
			if (obj.getValue("EngEstate") != null)
			{
				if (sb.length() > 0)
					sb.append(", ");
				boolean found = false;
				if ((s = obj.getValueString("EngEstate.EstateName")) != null)
				{
					if (found) sb.append(' ');
					sb.append(s);
					found = true;
				}
				if ((s = obj.getValueString("EngEstate.EngPhase.PhaseNo")) != null)
				{
					if (found) sb.append(' ');
					sb.append(s);
					found = true;
				}
			}
			if (obj.getValue("EngStreet") != null)
			{
				if (sb.length() > 0)
					sb.append(", ");
				boolean found = false;
				if ((s = obj.getValueString("EngStreet.BuildingNoFrom")) != null)
				{
					if (found) sb.append(' ');
					sb.append(s);
					found = true;
				}
				if ((s = obj.getValueString("EngStreet.StreetName")) != null)
				{
					if (found) sb.append(' ');
					sb.append(s);
					found = true;
				}
			}
			if ((s = obj.getValueString("EngDistrict.Sub-district")) != null)
			{
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(s);
			}
			if ((s = obj.getValueString("Region")) != null)
			{
				if (s.equals("KLN"))
					sb.append(", KOWLOON");
				else if (s.equals("HK"))
					sb.append(", HONG KONG");
				else if (s.equals("NT"))
					sb.append(", NEW TERRITORIES");
			}
			return sb.toString();
		}
		else if ((obj = addr.getValue("FreeFormatAddress")) != null)
		{
			sb.append(StringUtil.orEmpty(obj.getValueString("AddressLine1")));
			sb.append(", ");
			sb.append(StringUtil.orEmpty(obj.getValueString("AddressLine2")));
			sb.append(", ");
			sb.append(StringUtil.orEmpty(obj.getValueString("AddressLine3")));
			return sb.toString();
		}
		else if ((obj = addr.getValue("PostBoxAddress")) != null)
		{
			if ((addr = obj.getValue("EngPostBox")) != null)
			{
				sb.append(StringUtil.orEmpty(addr.getValueString("PostOffice")));
				sb.append(" Box ");
				sb.append(addr.getValueAsInt32("PoBoxNo"));
				sb.append(", ");
				sb.append(StringUtil.orEmpty(addr.getValueString("PostOfficeRegion")));
				return sb.toString();
			}
			else if ((addr = obj.getValue("ChiPostBox")) != null)
			{
				sb.append(StringUtil.orEmpty(addr.getValueString("PostOfficeRegion")));
				sb.append(StringUtil.orEmpty(addr.getValueString("PostOffice")));
				sb.append("信箱");
				sb.append(addr.getValueAsInt32("PoBoxNo"));
				sb.append("號");
				return sb.toString();
			}
		}
		return null;		
	}

	public IAMSmartAPI(@Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, @Nonnull String domain, @Nonnull String clientID, @Nonnull String clientSecret)
	{
		this.clif = clif;
		this.ssl = ssl;
		this.domain = domain;
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.rand = new RandomBytesGenerator();
	}

	public boolean getKey(@Nonnull MyX509PrivKey privKey, @Nonnull CEKInfo cek)
	{
		StringBuilder sbURL = new StringBuilder();
		sbURL.append("https://");
		sbURL.append(this.domain);
		sbURL.append("/api/v1/security/getKey");
		HTTPClient cli = HTTPClient.createConnect(this.clif, this.ssl, sbURL.toString(), RequestMethod.HTTP_POST, false);
		this.initHTTPClient(cli, "");
		byte[] dataBuff = cli.readToEnd();
		int code = cli.getRespStatus();
		cli.close();
		if (logger != null)
		{
			logger.logMessage("GetKey.Status: "+code, LogLevel.RAW);
			logger.logMessage("GetKey.Content: "+new String(dataBuff, StandardCharsets.UTF_8), LogLevel.RAW);
		}
		if (code != 200)
		{
			if (logger != null)
			{
				logger.logMessage("getKey: Status is not OK: "+code, LogLevel.ACTION);
			}
			return false;
		}
		JSONBase json;
		if ((json = JSONBase.parseJSONStr(new String(dataBuff, StandardCharsets.UTF_8))) == null)
		{
			if (logger != null)
			{
				logger.logMessage("getKey: Response is not JSON", LogLevel.ACTION);
			}
			return false;
		}
		String secretKey;
		String pubKey;
		long issueAt;
		long expiresIn;
		if (((secretKey = json.getValueString("content.secretKey")) == null) ||
			((pubKey = json.getValueString("content.pubKey")) == null) ||
			((issueAt = json.getValueAsInt64("content.issueAt")) == 0) ||
			((expiresIn = json.getValueAsInt64("content.expiresIn")) == 0))
		{
			if (logger != null)
			{
				logger.logMessage("getKey: Response content is not valid", LogLevel.ACTION);
			}
			return false;
		}
	
		byte[] pubKeyBuff;
		Base64Enc b64 = new Base64Enc();
		pubKeyBuff = b64.decodeBin(pubKey);
		if (!MyX509File.isPublicKeyInfo(pubKeyBuff, 0, pubKeyBuff.length, "1"))
		{
			if (logger != null)
			{
				logger.logMessage("getKey: PubKey is not valid", LogLevel.ACTION);
			}
			return false;
		}
		MyX509PubKey pk = new MyX509PubKey("PublicKey.key", pubKeyBuff, 0, pubKeyBuff.length);
	
		byte[] privKeyId = privKey.getKeyId();
		byte[] pubKeyId = pk.getKeyId();
		if (pubKeyId == null || privKeyId == null)
		{
			if (logger != null)
			{
				logger.logMessage("getKey: Error in getting key id", LogLevel.ACTION);
			}
			return false;
		}
		if (!ByteTool.byteEquals(pubKeyId, 0, privKeyId, 0, 20))
		{
			if (logger != null)
			{
				logger.logMessage("getKey: PubKey and PrivKey is not the same", LogLevel.ACTION);
			}
			return false;
		}
		byte[] cekBuff1;
		byte[] cekBuff2 = new byte[256];
		cekBuff1 = b64.decodeBin(secretKey);
		if (cekBuff1 == null || cekBuff1.length != 256)
		{
			if (logger != null)
			{
				logger.logMessage("getKey: secretKey length is not valid", LogLevel.ACTION);
			}
			return false;
		}
		MyX509Key prkey = privKey.createKey();
		if (prkey == null)
		{
			if (logger != null)
			{
				logger.logMessage("getKey: Error in converting PrivKey to Key", LogLevel.ACTION);
			}
			return false;
		}
		cekBuff2 = prkey.decrypt(cekBuff1, 0, cekBuff1.length, CipherPadding.PKCS1);
		if (cekBuff2 == null || cekBuff2.length != 32)
		{
			if (logger != null)
			{
				logger.logMessage("getKey: Decrypted key length is not valid", LogLevel.ACTION);
			}
			return false;
		}
		if (logger != null)
		{
			sbURL.setLength(0);
			StringUtil.appendHex(sbURL, cekBuff2, 0, cekBuff2.length, ' ', LineBreakType.CRLF);
			logger.logMessage("getKey: CEK = "+ sbURL.toString(), LogLevel.RAW);
			logger.logMessage("getKey: IssueAt = "+ZonedDateTime.ofInstant(Instant.ofEpochMilli(issueAt), ZoneId.systemDefault()), LogLevel.RAW);
			logger.logMessage("getKey: ExpiresAt = "+ZonedDateTime.ofInstant(Instant.ofEpochMilli(issueAt + expiresIn), ZoneId.systemDefault()), LogLevel.RAW);
		}
		cek.key = cekBuff2;
		cek.issueAt = issueAt;
		cek.expiresAt = issueAt + expiresIn;
		return true;		
	}

	public boolean revokeKey()
	{
		StringBuilder sbURL = new StringBuilder();
		sbURL.append("https://");
		sbURL.append(this.domain);
		sbURL.append("/api/v1/security/revokeKey");
		HTTPClient cli = HTTPClient.createConnect(this.clif, this.ssl, sbURL.toString(), RequestMethod.HTTP_POST, false);
		this.initHTTPClient(cli, "");
		byte[] dataBuff = cli.readToEnd();
		int code = cli.getRespStatus();
		cli.close();
		if (logger != null)
		{
			logger.logMessage("RevokeKey.Status: "+code, LogLevel.RAW);
			logger.logMessage("RevokeKey.Content: "+new String(dataBuff, StandardCharsets.UTF_8), LogLevel.RAW);
		}
		if (code != 200)
		{
			if (logger != null)
			{
				logger.logMessage("revokeKey: Status is not OK", LogLevel.ACTION);
			}
			return false;
		}
		JSONBase json;
		if ((json = JSONBase.parseJSONStr(new String(dataBuff, StandardCharsets.UTF_8))) == null)
		{
			if (logger != null)
			{
				logger.logMessage("revokeKey: Response is not JSON", LogLevel.ACTION);
			}
			return false;
		}
		String s;
		boolean succ = (s = json.getValueString("message")) != null && s.equals("SUCCESS");
		return succ;
	}

	public boolean getToken(@Nonnull String code, boolean directLogin, @Nonnull CEKInfo cek, @Nonnull TokenInfo token)
	{
		JSONBuilder jsonMsg = new JSONBuilder(ObjectType.OT_OBJECT);
		jsonMsg.objectAddStr("code", code);
		if (directLogin)
			jsonMsg.objectAddStr("isDirectLoginV2", "true");
		jsonMsg.objectAddStr("grantType", "authorization_code");
		StringBuilder sbURL = new StringBuilder();
		sbURL.append("https://");
		sbURL.append(this.domain);
		sbURL.append("/api/v1/auth/getToken");
		JSONBase json;
		if ((json = this.postEncReq(sbURL.toString(), cek, jsonMsg.toString())) == null)
			return false;
		long expiresIn;
		String accessToken;
		String openID;
		if ((token.issueAt = json.getValueAsInt64("issueAt")) != 0 && (expiresIn = json.getValueAsInt64("expiresIn")) != 0 && (token.lastModifiedDate = json.getValueAsInt64("lastModifiedDate")) != 0 && (accessToken = json.getValueString("accessToken")) != null && (openID = json.getValueString("openID")) != null)
		{
			token.accessToken = accessToken;
			token.openID = openID;
			token.expiresAt = token.issueAt + expiresIn;
			return true;
		}
		return false;
	}

	public boolean getProfiles(@Nonnull TokenInfo token, @Nonnull String eMEFields, @Nonnull String profileFields, @Nonnull CEKInfo cek, @Nonnull ProfileInfo profiles)
	{
		JSONBuilder jsonMsg = new JSONBuilder(ObjectType.OT_OBJECT);
		jsonMsg.objectAddStr("accessToken", token.accessToken);
		jsonMsg.objectAddStr("openID", token.openID);
		jsonMsg.objectAddArrayStr("profileFields", profileFields, ",");
		jsonMsg.objectAddArrayStr("eMEFields", eMEFields, ",");
		StringBuilder sbURL = new StringBuilder();
		sbURL.append("https://");
		sbURL.append(this.domain);
		sbURL.append("/api/v1/profiles");
		JSONBase json;
		if ((json = this.postEncReq(sbURL.toString(), cek, jsonMsg.toString())) == null)
			return false;
		profiles.hkid = null;
		profiles.hkidChk = 0;
		profiles.prefix = null;
		profiles.enName = null;
		profiles.chName = null;
		profiles.chNameVerfied = null;
		profiles.birthDate = 0;
		profiles.gender = 0;
		profiles.maritalStatus = 0;
		profiles.homeTelNumber = null;
		profiles.homeTelNumberICC = null;
		profiles.officeTelNumber = null;
		profiles.officeTelNumberICC = null;
		profiles.mobileTelNumber = null;
		profiles.mobileTelNumberICC = null;
		profiles.emailAddress = null;
		profiles.residentialAddress = null;
		profiles.postalAddress = null;
		profiles.educationLevel = 0;
		String s;
		profiles.hkid = json.getValueString("idNo.Identification");
		if ((s = json.getValueString("idNo.CheckDigit")) != null && s.length() > 0)
			profiles.hkidChk = s.charAt(0);
		profiles.prefix = json.getValueString("prefix");
		profiles.enName = json.getValueString("enName.UnstructuredName");
		profiles.chName = json.getValueString("chName.ChineseName");
		profiles.chNameVerfied = json.getValueString("chNameVerified");
		if ((s = json.getValueString("birthDate")) != null)
			profiles.birthDate = StringUtil.toIntegerS(s, 0);
		if ((s = json.getValueString("gender")) != null && s.length() > 0)
			profiles.gender = s.charAt(0);
		if ((s = json.getValueString("maritalStatus")) != null && s.length() > 0)
			profiles.maritalStatus = s.charAt(0);
		profiles.homeTelNumber = json.getValueString("homeTelNumber.SubscriberNumber");
		profiles.homeTelNumberICC = json.getValueString("homeTelNumber.CountryCode");
		profiles.officeTelNumber = json.getValueString("officeTelNumber.SubscriberNumber");
		profiles.officeTelNumberICC = json.getValueString("officeTelNumber.CountryCode");
		profiles.mobileTelNumber = json.getValueString("mobileNumber.SubscriberNumber");
		profiles.mobileTelNumberICC = json.getValueString("mobileNumber.CountryCode");
		profiles.emailAddress = json.getValueString("emailAddress");
		profiles.residentialAddress = parseAddress(json, "residentialAddress");
		profiles.postalAddress = parseAddress(json, "postalAddress");
		if ((s = json.getValueString("educationLevel")) != null && s.length() > 0)
			profiles.educationLevel = s.charAt(0);
		return true;
	}
}
