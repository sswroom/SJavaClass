package org.sswr.util.net;

import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.crypto.cert.MyX509Key;
import org.sswr.util.crypto.cert.MyX509PrivKey;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class IAMSmartClient {
	public static interface CEKUpdateListener
	{
		public void cekUpdated(IAMSmartAPI.CEKInfo cek);
	}
	private IAMSmartAPI api;
	private IAMSmartAPI.CEKInfo cek;
	private MyX509PrivKey key;
	private LogTool log;
	private CEKUpdateListener cekListener;

	private boolean prepareCEK()
	{
		long currTime = System.currentTimeMillis();
		if (currTime < cek.expiresAt)
		{
			return true;
		}
		if (this.key == null)
			return false;
		if (cek.issueAt != 0)
		{
			this.cek.issueAt = 0;
			this.cek.key = null;
		}
		if (!this.api.getKey(key, cek))
			return false;
		if (this.log != null) this.log.logMessage("CEK updated: exp="+cek.issueAt+", byte="+StringUtil.toHex(cek.key), LogLevel.ACTION);
		if (this.cekListener != null) this.cekListener.cekUpdated(this.cek);
		return currTime < cek.expiresAt;
	}

	public IAMSmartClient(@Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, @Nonnull String domain, @Nonnull String clientID, @Nonnull String clientSecret, @Nonnull String keyFile)
	{
		this.api = new IAMSmartAPI(clif, ssl, domain, clientID, clientSecret);
		this.cek = new IAMSmartAPI.CEKInfo();
		this.cek.issueAt = 0;
		this.cek.expiresAt = 0;
		this.key = null;
		X509Parser parser = new X509Parser();
		MyX509File file;
		if ((file = (MyX509File)parser.parseFilePath(keyFile)) != null)
		{
			if (file.getFileType() == MyX509File.FileType.PrivateKey)
			{
				this.key = (MyX509PrivKey)file;
			}
			else if (file.getFileType() == MyX509File.FileType.Key)
			{
				this.key = MyX509PrivKey.createFromKey((MyX509Key)file);
			}
		}
	}

	public void setLog(@Nullable LogTool log)
	{
		this.log = log;
	}

	public void setCEKUpdateListener(CEKUpdateListener cekListener)
	{
		this.cekListener = cekListener;
	}

	public boolean isError()
	{
		return this.key == null;
	}

	public boolean getToken(@Nonnull String code, boolean directLogin, @Nonnull IAMSmartAPI.TokenInfo token)
	{
		if (!this.prepareCEK())
			return false;
		boolean succ = this.api.getToken(code, directLogin, this.cek, token);
		if (!succ && this.log != null) this.log.logMessage("Failed to getToken", LogLevel.ERROR);
		return succ;
	}
	
	public boolean getProfiles(@Nonnull IAMSmartAPI.TokenInfo token, @Nonnull String eMEFields, @Nonnull String profileFields, @Nonnull IAMSmartAPI.ProfileInfo profiles)
	{
		if (!this.prepareCEK())
			return false;
		boolean succ = this.api.getProfiles(token, eMEFields, profileFields, this.cek, profiles);
		if (!succ && this.log != null) this.log.logMessage("Failed to getProfiles", LogLevel.ERROR);
		return succ;
	}

	public void updateCEK(@Nonnull byte[] key, long issueAt, long expiresAt)
	{
		this.cek.key = key;
		this.cek.issueAt = issueAt;
		this.cek.expiresAt = expiresAt;
	}
}
