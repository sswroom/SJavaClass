package org.sswr.util.io;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.sswr.util.crypto.EncryptionException;
import org.sswr.util.crypto.JasyptEncryptor;
import org.sswr.util.crypto.JasyptEncryptor.CipherAlgorithm;
import org.sswr.util.crypto.JasyptEncryptor.KeyAlgorithm;
import org.sswr.util.data.textbinenc.EncodingException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JasyptConfigFile extends ConfigFile
{
	private ConfigFile cfg;
	private JasyptEncryptor enc;

	public JasyptConfigFile(@Nonnull ConfigFile cfg, @Nonnull KeyAlgorithm keyAlg, @Nonnull CipherAlgorithm cipherAlg, @Nonnull String password)
	{
		this.enc = new JasyptEncryptor(keyAlg, cipherAlg, password);
		this.cfg = cfg;
	}
	
	@Nullable
	public String getValue(@Nullable String category, @Nonnull String name)
	{
		if (category == null)
		{
			category = "";
		}
		String ret = this.cfg.getValue(category, name);
		if (ret == null)
		{
			return null;
		}
		if (ret.startsWith("ENC(") && ret.endsWith(")"))
		{
			try
			{
				return enc.decryptToString(ret.substring(4, ret.length() - 1));
			}
			catch (EncryptionException ex)
			{
				return ret;
			}
		}
		else
		{
			return ret;
		}
	}

	public boolean setValue(@Nullable String category, @Nonnull String name, @Nullable String value)
	{
		return this.cfg.setValue(category, name, value);
	}

	public boolean setEncValue(@Nullable String category, @Nonnull String name, @Nonnull String value) throws EncryptionException, EncodingException
	{
		byte buff[] = value.getBytes(StandardCharsets.UTF_8);
		return this.cfg.setValue(category, name, "ENC("+this.enc.encryptAsB64(buff, 0, buff.length)+")");
	}

	public int getCateCount()
	{
		return this.cfg.getCateCount();
	}

	@Nonnull
	public Set<String> getCateList()
	{
		return this.cfg.getCateList();
	}

	@Nullable
	public Set<String> getKeys(@Nullable String category)
	{
		return this.cfg.getKeys(category);
	}

	public boolean hasCategory(@Nullable String category)
	{
		return this.cfg.hasCategory(category);
	}
}
