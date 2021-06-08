package org.sswr.util.io;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.sswr.util.crypto.JasyptEncryptor;
import org.sswr.util.crypto.JasyptEncryptor.CipherAlgorithm;
import org.sswr.util.crypto.JasyptEncryptor.KeyAlgorithm;

public class JasyptConfigFile extends ConfigFile
{
	private ConfigFile cfg;
	private JasyptEncryptor enc;

	public JasyptConfigFile(ConfigFile cfg, KeyAlgorithm keyAlg, CipherAlgorithm cipherAlg, String password)
	{
		this.enc = new JasyptEncryptor(keyAlg, cipherAlg, password);
		this.cfg = cfg;
	}
	
	public String getValue(String category, String name)
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
			return enc.decryptToString(ret.substring(4, ret.length() - 1));
		}
		else
		{
			return ret;
		}
	}

	public boolean setValue(String category, String name, String value)
	{
		return this.cfg.setValue(category, name, value);
	}

	public boolean setEncValue(String category, String name, String value)
	{
		byte buff[] = value.getBytes(StandardCharsets.UTF_8);
		return this.cfg.setValue(category, name, "ENC("+this.enc.encryptAsB64(buff, 0, buff.length)+")");
	}

	public int getCateCount()
	{
		return this.cfg.getCateCount();
	}

	public Set<String> getCateList()
	{
		return this.cfg.getCateList();
	}

	public Set<String> getKeys(String category)
	{
		return this.cfg.getKeys(category);
	}

	public boolean hasCategory(String category)
	{
		return this.cfg.hasCategory(category);
	}
}
