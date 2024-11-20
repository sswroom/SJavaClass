package org.sswr.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyStream extends IOStream {

	@Nullable private InputStream istm;
	@Nullable private OutputStream ostm;
	@Nullable private LogTool log;

	public MyStream(@Nullable InputStream istm, @Nullable OutputStream ostm)
	{
		super("MyStream");
		this.istm = istm;
		this.ostm = ostm;
		this.log = null;
	}

	public void setLog(@Nullable LogTool log)
	{
		this.log = log;
	}

	@Override
	public boolean isDown() {
		return this.istm == null && this.ostm == null;
	}

	@Override
	public int read(@Nonnull byte[] buff, int ofst, int size) {
		try
		{
			if (this.istm != null)
			{
				return this.istm.read(buff, ofst, size);
			}
		}
		catch (IOException ex)
		{
			if (this.log != null)
				this.log.logException(ex);
		}
		return 0;
	}

	@Override
	public int write(@Nonnull byte[] buff, int ofst, int size) {
		try
		{
			if (this.ostm != null)
			{
				this.ostm.write(buff, ofst, size);
				return size;
			}
		}
		catch (IOException ex)
		{
			if (this.log != null)
				this.log.logException(ex);
		}
		return 0;
	}

	@Override
	public int flush() {
		try
		{
			if (this.ostm != null)
				this.ostm.flush();
		}
		catch (IOException ex)
		{
			if (this.log != null)
				this.log.logException(ex);
		}
		return 0;
	}

	@Override
	public void close() {
		try
		{
			if (this.istm != null)
				this.istm.close();
		}
		catch (IOException ex)
		{
			if (this.log != null)
				this.log.logException(ex);
		}
		this.istm = null;
		try
		{
			if (this.ostm != null)
				this.ostm.close();
		}
		catch (IOException ex)
		{
			if (this.log != null)
				this.log.logException(ex);
		}
		this.ostm = null;
	}

	@Override
	public boolean recover() {
		return false;
	}
	
}
