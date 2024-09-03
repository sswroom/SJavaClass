package org.sswr.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class StreamDataInputStream extends InputStream {
	private StreamData fd;
	private long ofst;

	public StreamDataInputStream(StreamData fd)
	{
		this.fd = fd.getPartialData(0, fd.getDataSize());
		this.ofst = 0;
	}

	@Override
	public int read() throws IOException {
		if (this.fd == null)
			throw new IOException("Stream already closed");
		long leng = fd.getDataSize();
		if (ofst >= leng)
		{
			return -1;
		}
		byte[] buff = new byte[1];
		if (fd.getRealData(ofst, 1, buff, 0) != 1)
			return -1;
		ofst += 1;
		return buff[0] & 0xff;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (this.fd == null)
			throw new IOException("Stream already closed");
		Objects.checkFromIndexSize(off, len, b.length);
		long leng = fd.getDataSize();
		if (len <= 0) {
			return 0;
		} else {
			if (ofst + len > leng)
				len = (int)(leng - ofst);
			if (len <= 0)
				return -1;
			int readSize = fd.getRealData(ofst, len, b, off);
			ofst += readSize;
            return readSize;
		}
	}

	public void close() throws IOException {
		if (this.fd == null)
			throw new IOException("Stream already closed");
		this.fd.close();
		this.fd = null;
		//System.out.println("StreamDataInputStream.close()");
	}
 }
