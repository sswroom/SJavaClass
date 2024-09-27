package org.sswr.util.net;

import org.sswr.util.crypto.MyX509File;
import org.sswr.util.crypto.MyX509Key;
import org.sswr.util.crypto.MyX509PrivKey;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class IAMSmartClient {
	private IAMSmartAPI api;
	private IAMSmartAPI.CEKInfo cek;
	private MyX509PrivKey key;

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
		return currTime < cek.expiresAt;
	}

	public IAMSmartClient(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nonnull String domain, @Nonnull String clientID, @Nonnull String clientSecret, @Nonnull String keyFile)
	{
		this.api = new IAMSmartAPI(sockf, ssl, domain, clientID, clientSecret);
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

	public boolean isError()
	{
		return this.key == null;
	}

	public boolean getToken(@Nonnull String code, boolean directLogin, @Nonnull IAMSmartAPI.TokenInfo token)
	{
		if (!this.prepareCEK())
			return false;
		return this.api.getToken(code, directLogin, this.cek, token);
	}
	
	public boolean getProfiles(@Nonnull IAMSmartAPI.TokenInfo token, @Nonnull String eMEFields, @Nonnull String profileFields, @Nonnull IAMSmartAPI.ProfileInfo profiles)
	{
		if (!this.prepareCEK())
			return false;
		return this.api.getProfiles(token, eMEFields, profileFields, this.cek, profiles);
	}
}
