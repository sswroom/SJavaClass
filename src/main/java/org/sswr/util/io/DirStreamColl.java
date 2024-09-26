package org.sswr.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class DirStreamColl implements StreamColl
{
	private File file;
	private Map<String, File> fileMap;
	private Map<String, File> lcaseFileMap;
	private boolean ignoreCase;

	public DirStreamColl(@Nonnull String path, boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
		this.file = new File(path);
		this.fileMap = new HashMap<String, File>();
		this.lcaseFileMap = new HashMap<String, File>();
		File[] files = this.file.listFiles();
		int i = 0;
		int j = files.length;
		while (i < j)
		{
			if (files[i].isFile())
			{
				this.fileMap.put(files[i].getName(), files[i]);
				this.lcaseFileMap.put(files[i].getName().toLowerCase(), files[i]);
			}
			i++;
		}
	}

	@Override
	public void close()
	{
	}

	@Override
	@Nonnull
	public Iterator<String> listFiles()
	{
		return this.fileMap.keySet().iterator();
	}

	@Override
	public boolean hasFile(@Nonnull String fileName)
	{
		if (this.ignoreCase)
		{
			return this.lcaseFileMap.containsKey(fileName.toLowerCase());
		}
		else
		{
			return this.fileMap.containsKey(fileName);
		}
	}

	@Override
	public long getStmSize(@Nonnull String fileName)
	{
		File file;
		if (this.ignoreCase)
		{
			file = this.lcaseFileMap.get(fileName.toLowerCase());
		}
		else
		{
			file = this.fileMap.get(fileName);
		}
		if (file == null)
		{
			return 0;
		}
		else
		{
			return file.length();
		}
	}

	@Override
	@Nullable
	public InputStream openStream(@Nonnull String fileName)
	{
		File file;
		if (this.ignoreCase)
		{
			file = this.lcaseFileMap.get(fileName.toLowerCase());
		}
		else
		{
			file = this.fileMap.get(fileName);
		}
		if (file == null)
		{
			return null;
		}
		else
		{
			try
			{
				return new FileInputStream(file);
			}
			catch (FileNotFoundException ex)
			{
				return null;
			}
		}
	}
	
}
