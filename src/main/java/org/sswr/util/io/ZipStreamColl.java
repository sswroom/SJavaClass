package org.sswr.util.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ZipStreamColl implements StreamColl
{
	private boolean ignoreCase;
	private File tmpFile;
	private ZipFile zip;
	private HashMap<String, ZipEntry> zipMap;
	private HashMap<String, ZipEntry> lcaseZipMap;

	public ZipStreamColl(@Nonnull InputStream zipFile, boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
		FileOutputStream fos = null;
		try
		{
			this.tmpFile = File.createTempFile("temp", ".zip");
			fos = new FileOutputStream(tmpFile);
			zipFile.transferTo(fos);
			fos.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException ex2)
				{
				}
				this.tmpFile.delete();
			}
			return;
		}

		try
		{
			this.zip = new ZipFile(this.tmpFile);
			this.zipMap = new HashMap<String, ZipEntry>();
			this.lcaseZipMap = new HashMap<String, ZipEntry>();
			Enumeration<? extends ZipEntry> eEnts = this.zip.entries();
			ZipEntry ent;
			while (eEnts.hasMoreElements())
			{
				ent = eEnts.nextElement();
				this.zipMap.put(ent.getName(), ent);
				this.lcaseZipMap.put(ent.getName().toLowerCase(), ent);
			}
		}
		catch (IOException ex)
		{

		}
	}

	public boolean zipError()
	{
		return this.zip == null || this.zipMap == null;
	}

	@Override
	public void close()
	{
		if (this.zip != null)
		{
			try
			{
				this.zip.close();
			}
			catch (IOException ex)
			{

			}
			this.zip = null;
		}

		if (this.tmpFile != null)
		{
			this.tmpFile.delete();
		}
	}

	@Override
	@Nonnull
	public Iterator<String> listFiles()
	{
		if (this.zipMap != null)
			return this.zipMap.keySet().iterator();
		return new ArrayList<String>().iterator();
	}

	@Override
	public boolean hasFile(@Nonnull String fileName)
	{
		if (this.ignoreCase)
			return this.lcaseZipMap.containsKey(fileName.toLowerCase());
		else
			return this.zipMap.containsKey(fileName);
	}

	@Override
	public long getStmSize(@Nonnull String fileName)
	{
		ZipEntry ent;
		if (this.ignoreCase)
			ent = this.lcaseZipMap.get(fileName.toLowerCase());
		else
			ent = this.zipMap.get(fileName);
		if (ent == null)
			return 0;
		return ent.getSize();
	}

	@Override
	@Nullable
	public InputStream openStream(@Nonnull String fileName)
	{
		ZipEntry ent;
		if (this.ignoreCase)
			ent = this.lcaseZipMap.get(fileName.toLowerCase());
		else
			ent = this.zipMap.get(fileName);
		if (ent == null)
			return null;
		try
		{
			return this.zip.getInputStream(ent);
		}
		catch (IOException ex)
		{
			return null;
		}
	}
}
