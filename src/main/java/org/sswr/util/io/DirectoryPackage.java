package org.sswr.util.io;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.sswr.util.io.stmdata.FileData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class DirectoryPackage extends PackageFile
{
	private Set<String> fileNames;
	private Set<String> dirNames;

	public DirectoryPackage(@Nonnull String pathName)
	{
		super(pathName);

		this.fileNames = new HashSet<String>();
		this.dirNames = new HashSet<String>();

		File file = new File(pathName);
		if (file.isDirectory())
		{
			File[]files = file.listFiles();
			int i = 0;
			int j = files.length;
			while (i < j)
			{
				if (files[i].isDirectory())
				{
					this.dirNames.add(files[i].getName());
				}
				else if (files[i].isFile())
				{
					this.fileNames.add(files[i].getName());
				}
				i++;
			}
		}
	}

	@Nullable
	public StreamData getItemStmData(@Nonnull String name)
	{
		if (this.fileNames.contains(name))
		{
			String path = this.sourceName;
			if (path.endsWith(File.separator))
			{
				return new FileData(path+name, false);
			}
			else
			{
				return new FileData(path+File.separator+name, false);
			}
		}
		return null;
	}
}
