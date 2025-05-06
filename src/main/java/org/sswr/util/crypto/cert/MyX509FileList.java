package org.sswr.util.crypto.cert;

import java.util.ArrayList;
import java.util.List;

import org.sswr.util.net.ASN1Data;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyX509FileList extends MyX509File
{
	private List<MyX509File> fileList;

	public MyX509FileList(@Nonnull String sourceName, @Nonnull MyX509Cert cert)
	{
		super(sourceName, cert.getASN1Buff(), 0, cert.getASN1BuffSize());
		this.fileList = new ArrayList<MyX509File>();
		this.fileList.add(cert);
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.FileList;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		MyX509FileList fileList = new MyX509FileList(this.getSourceNameObj(), (MyX509Cert)this.fileList.get(0).clone());
		int i = 1;
		int j = this.fileList.size();
		while (i < j)
		{
			fileList.addFile((MyX509File)this.fileList.get(i).clone());
			i++;
		}
		return fileList;
	}

	@Override
	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j = this.fileList.size();
		while (i < j)
		{
			sb.append(this.fileList.get(i).toString());
			i++;
		}
		return sb.toString();
	}

	public void addFile(@Nonnull MyX509File file)
	{
		this.fileList.add(file);
	}
	
	public int getFileCount()
	{
		return this.fileList.size();
	}
	
	@Nullable
	public MyX509File getFile(int index)
	{
		return this.fileList.get(index);
	}	
}
