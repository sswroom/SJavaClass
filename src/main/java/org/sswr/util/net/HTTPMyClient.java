package org.sswr.util.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sswr.util.basic.ArrayListStr;
import org.sswr.util.crypto.cert.CertUtil;
import org.sswr.util.crypto.cert.MyX509Cert;
import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.MemoryStream;
import org.sswr.util.io.Path;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class HTTPMyClient extends HTTPClient{
	private static boolean LOGREPLY = false;
	private static boolean SHOWDEBUG = false;
	private static boolean DEBUGSPEED = false;
	private static final int BUFFSIZE = 8192;

	public static void setLogReply(boolean logReply)
	{
		LOGREPLY = logReply;
	}

	public static void setShowDebug(boolean showDebug)
	{
		SHOWDEBUG = showDebug;
	}

	public static void setDebugSpeed(boolean debugSpeed)
	{
		DEBUGSPEED = debugSpeed;
	}

	@Nullable private FileStream clsDataFS;
	@Nullable protected SSLEngine ssl;
	@Nullable protected TCPClient cli;
	@Nullable protected	String cliHost;
	@Nonnull protected MemoryStream reqMstm;
	@Nonnull protected String userAgent;
	@Nonnull protected ArrayListStr reqHeaders;
	protected boolean writing;

	protected long contRead;
	protected int contEnc;
	protected int chunkSizeLeft;
	protected List<String> headers;

	@Nonnull protected byte[] dataBuff;
	protected int buffSize;
	protected int buffOfst;
	protected Duration timeout;
		
	protected int readRAWInternal(@Nonnull byte[] buff, int ofst, int size)
	{
		FileStream clsDataFS;
		TCPClient cli;
		if (size > BUFFSIZE)
		{
			size = BUFFSIZE;
		}

		if (size > (this.contLeng - this.contRead))
		{
			if (this.contLeng <= this.contRead)
			{
				return 0;
			}
			size = (int)(this.contLeng - this.contRead);
		}
		if (SHOWDEBUG)
		{
			System.out.println("Read size = "+size);
		}

		if (this.contEnc == 1)
		{
			int i;
			int j;
			int sizeOut = 0;
			if (SHOWDEBUG)
			{
				System.out.println("chunkSizeLeft = "+chunkSizeLeft+", buffSize = "+buffSize);
			}
			if (chunkSizeLeft > 0)
			{
				if (chunkSizeLeft > this.buffSize && BUFFSIZE - 1 - this.buffSize > 0)
				{
					if ((cli = this.cli) == null)
					{
						return 0;
					}
					i = cli.read(this.dataBuff, this.buffSize, BUFFSIZE - 1 - this.buffSize);
					if (i == 0 && this.buffSize <= 0)
					{
						if (SHOWDEBUG)
						{
							System.out.println("Return Read size(1) = "+0);
						}
						if (cli.isClosed())
						{
							cli.dispose();
							this.cli = null;
						}
						return 0;
					}
					if (SHOWDEBUG)
					{
						System.out.println("Read from remote(1) = "+i);
					}
					if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
					{
						if (i > 0)
						{
							clsDataFS.write(this.dataBuff, this.buffSize, i);
						}
					}
					this.totalDownload += i;
					this.buffSize += i;
				}
				if (this.chunkSizeLeft <= 2 && (cli = this.cli) != null)
				{
					while (this.chunkSizeLeft > this.buffSize)
					{
						i = cli.read(this.dataBuff, this.buffSize, BUFFSIZE - 1 - this.buffSize);
						if (i == 0)
						{
							if (SHOWDEBUG)
							{
								System.out.println("Return Read size(1.2) = "+0);
							}
							if (cli.isClosed())
							{
								cli.dispose();
								this.cli = null;
							}
							return 0;
						}
						if (SHOWDEBUG)
						{
							System.out.println("Read from remote(1.2) = "+i);
						}
						if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
						{
							if (i > 0)
							{
								clsDataFS.write(this.dataBuff, this.buffSize, i);
							}
						}
						this.totalDownload += i;
						this.buffSize += i;
					}
					ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, this.chunkSizeLeft, this.buffSize - this.chunkSizeLeft);
					this.buffSize -= this.chunkSizeLeft;
					this.chunkSizeLeft = 0;
				}
				else if (size >= this.buffSize)
				{
					sizeOut = this.buffSize;
					if (sizeOut > this.chunkSizeLeft)
					{
						sizeOut = this.chunkSizeLeft;
					}
					ByteTool.copyArray(buff, ofst, this.dataBuff, 0, sizeOut);
					ofst += sizeOut;
					size -= sizeOut;
					this.chunkSizeLeft -= sizeOut;
					this.buffSize -= sizeOut;
					if (this.buffSize > 0)
					{
						ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, sizeOut, this.buffSize);
					}
					if (this.chunkSizeLeft < 2)
					{
						sizeOut -= (2 - this.chunkSizeLeft);
					}
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(2) = "+sizeOut);
					}
					return sizeOut;
				}
				else
				{
					sizeOut = size;
					if (sizeOut > this.chunkSizeLeft)
					{
						sizeOut = this.chunkSizeLeft;
					}
					ByteTool.copyArray(buff, ofst, this.dataBuff, 0, sizeOut);
					ofst += sizeOut;
					size -= sizeOut;
					this.chunkSizeLeft -= sizeOut;
					this.buffSize -= sizeOut;
					if (this.buffSize > 0)
					{
						ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, sizeOut, this.buffSize);
					}
					if (this.chunkSizeLeft < 2)
					{
						sizeOut -= (2 - this.chunkSizeLeft);
					}
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(3) = "+sizeOut);
					}
					return sizeOut;
				}
			}

			this.dataBuff[this.buffSize] = 0;
			if (this.dataBuff[0] == '\r' && this.dataBuff[1] == '\n')
			{
				ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, 2, this.buffSize - 2);
				buffSize -= 2;
			}
			if (buffSize <= 0 && this.cli != null)
			{
				i = this.cli.read(this.dataBuff, this.buffSize, BUFFSIZE - 1 - this.buffSize);
				if (i == 0 && this.buffSize <= 0)
				{
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(4) = "+0);
					}
					return 0;
				}
				if (SHOWDEBUG)
				{
					System.out.println("Read from remote(2) = "+i);
				}
				if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
				{
					if (i > 0)
					{
						clsDataFS.write(this.dataBuff, this.buffSize, i);
					}
				}
				this.totalDownload += i;
				this.buffSize += i;
			}
			while (-1 == (i = StringUtil.indexOfUTF8(this.dataBuff, 0, this.buffSize, "\r\n")))
			{
				if ((cli = this.cli) == null)
				{
					return 0;
				}
				if ((BUFFSIZE - 1 - this.buffSize) == 0)
				{
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(5) = "+0);
					}
					return 0;
				}
				i = cli.read(this.dataBuff, this.buffSize, BUFFSIZE - 1 - this.buffSize);
				if (i == 0)
				{
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(6) = "+0);
					}
					if (cli.isClosed())
					{
						cli.dispose();
						this.cli = null;
					}
					return 0;
				}
				if (SHOWDEBUG)
				{
					System.out.println("Read from remote(3) = "+i);
				}
				if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
				{
					clsDataFS.write(this.dataBuff, this.buffSize, i);
				}
				this.totalDownload += i;
				this.buffSize += i;
				this.dataBuff[this.buffSize] = 0;
				if (this.dataBuff[0] == '\r' && this.dataBuff[1] == '\n')
				{
					ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, 2, this.buffSize - 2);
					buffSize -= 2;
				}
			}
			this.dataBuff[i] = 0;
			if (SHOWDEBUG)
			{
				System.out.println("Chunk size "+new String(this.dataBuff, 0, i, StandardCharsets.UTF_8));
			}
			String s = new String(this.dataBuff, 0, i, StandardCharsets.UTF_8);
			j = ByteTool.intOr(StringUtil.hex2Int(s), 0);
			if (j == 0 && i == 1 && this.dataBuff[0] == '0')
			{
				i = 3;
				if (this.buffSize >= 5 && this.dataBuff[3] == 13 && this.dataBuff[4] == 10)
				{
					i = 5;
				}
				this.buffSize -= i;
				if (this.buffSize > 0)
				{
					ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, i, this.buffSize);
				}
				this.contLeng = 0;
				if (SHOWDEBUG)
				{
					System.out.println("Return read size(7) = "+0);
				}
				return 0;
			}
			if (j == 0)
			{
				this.dataBuff[i] = 13;
				if (SHOWDEBUG)
				{
					System.out.println("Return read size(8) = "+0+", i = "+i+", ("+StringUtil.toHex(this.dataBuff, 0, i, ' ')+")");
				}
				return 0;
			}
			this.chunkSizeLeft = j + 2;
			if (SHOWDEBUG)
			{
				System.out.println("set chunkSizeLeft = "+this.chunkSizeLeft);
			}
			i += 2;
			if (this.buffSize == i && (cli = this.cli) != null)
			{
				this.buffSize = 0;
				i = cli.read(this.dataBuff, 0, BUFFSIZE - 1);
				if (i == 0)
				{
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(4.2) = "+0);
					}
					return 0;
				}
				if (SHOWDEBUG)
				{
					System.out.println("Read from remote(2.2) = "+i);
				}
				if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
				{
					if (i > 0)
					{
						clsDataFS.write(this.dataBuff, this.buffSize, i);
					}
				}
				this.totalDownload += i;
				this.buffSize += i;
				i = 0;
			}
			if (this.buffSize > i)
			{
				if (size >= this.buffSize - i)
				{
					sizeOut = this.buffSize - i;
					if (sizeOut > this.chunkSizeLeft)
					{
						sizeOut = this.chunkSizeLeft;
					}
					ByteTool.copyArray(buff, ofst, this.dataBuff, i, sizeOut);
					ofst += sizeOut;
					size -= sizeOut;
					this.chunkSizeLeft -= sizeOut;
					this.buffSize -= sizeOut + i;
					if (this.buffSize > 0)
					{
						ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, sizeOut + i, this.buffSize);
					}
					if (this.chunkSizeLeft < 2)
					{
						sizeOut -= (2 - this.chunkSizeLeft);
					}
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(9) = "+sizeOut);
					}
					return sizeOut;
				}
				else
				{
					sizeOut = size;
					if (sizeOut > this.chunkSizeLeft)
					{
						sizeOut = this.chunkSizeLeft;
					}
					ByteTool.copyArray(buff, ofst, this.dataBuff, i, sizeOut);
					ofst += sizeOut;
					size -= sizeOut;
					this.chunkSizeLeft -= sizeOut;
					this.buffSize -= sizeOut + i;
					if (this.buffSize > 0)
					{
						ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, sizeOut + i, this.buffSize);
					}
					if (this.chunkSizeLeft < 2)
					{
						sizeOut -= (2 - this.chunkSizeLeft);
					}
					if (SHOWDEBUG)
					{
						System.out.println("Return read size(10) = "+sizeOut);
					}
					return sizeOut;
				}
			}
			else
			{
				this.buffSize = 0;
				if (SHOWDEBUG)
				{
					System.out.println("Return read size(11) = "+0);
				}
				return 0;
			}
		}
		else
		{
			if (this.buffSize == 0)
			{
				if ((cli = this.cli) == null)
				{
					return 0;
				}
				this.buffSize = cli.read(this.dataBuff, 0, size);
				this.totalDownload += this.buffSize;
				if (SHOWDEBUG)
				{
					System.out.println("Read from remote(4) = "+this.buffSize);
		/*			if (this.buffSize == 0)
					{
						System.out.println("WSA Error code=0x%X\r\n", WSAGetLastError());
					}*/
				}
				if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
				{
					if (this.buffSize > 0)
					{
						clsDataFS.write(this.dataBuff, 0, this.buffSize);
					}
				}
				if (cli.isClosed())
				{
					cli.dispose();
					this.cli = null;
				}
			}
			if (this.buffSize >= size)
			{
				ByteTool.copyArray(buff, ofst, this.dataBuff, 0, size);
				if (this.buffSize > size)
				{
					ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, size, this.buffSize - size);
				}
				this.buffSize -= size;
				this.contRead += size;
				if (SHOWDEBUG)
				{
					System.out.println("Return read size(12) = "+buff.length);
				}
				return size;
			}
			else
			{
				ByteTool.copyArray(buff, ofst, this.dataBuff, 0, this.buffSize);
				size = this.buffSize;
				this.contRead += this.buffSize;
				this.buffSize = 0;
				if (SHOWDEBUG)
				{
					System.out.println("Return read size(13) = "+size);
				}
				return size;
			}
		}
	}

	public HTTPMyClient(@Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, @Nullable String userAgent, boolean kaConn)
	{
		super(clif, kaConn);
		this.headers = new ArrayList<String>();
		this.reqMstm = new MemoryStream();
		this.reqHeaders = new ArrayListStr();
		if (userAgent == null)
		{
			userAgent = "sswr/1.0";
		}
		if (LOGREPLY)
		{
			String fileName = Path.appendPath(Path.getProcessFileName(), "HTTPClient_"+System.currentTimeMillis()+".dat");
			this.clsDataFS = new FileStream(fileName, FileMode.Create, FileShare.DenyNone, BufferType.Normal);
		}
		this.ssl = ssl;
		this.cli = null;
		this.cliHost = null;
		this.writing = false;
		this.buffSize = 0;
		this.buffOfst = 0;
		this.contEnc = 0;
		this.timeout = Duration.ofSeconds(120, 0);
		this.userAgent = userAgent;
		this.dataBuff = new byte[BUFFSIZE];
	}

	public void dispose()
	{
		FileStream clsDataFS;
		TCPClient cli;
		if ((cli = this.cli) != null)
		{
			Socket soc;
			if ((soc = cli.getSocket()) != null)
			{
				try
				{
					soc.setSoLinger(false, 0);
				}
				catch (SocketException ex)
				{
					ex.printStackTrace();
				}
			}
			cli.shutdownSend();
			cli.dispose();
			this.cli = null;
		}
		if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
		{
			clsDataFS.dispose();
		}
	}

	public boolean isError()
	{
		return this.cli == null;
	}

	public int readRAW(@Nonnull byte[] buff, int ofst, int size)
	{
		this.endRequest(null, null);
		if (this.respCode == 0)
			return 0;
		return this.readRAWInternal(buff, ofst, size);
	}

	public int read(@Nonnull byte[] buff, int ofst, int size)
	{
		this.endRequest(null, null);
		if (this.respCode == 0)
			return 0;
		return this.readRAWInternal(buff, ofst, size);
	}

	public int write(@Nonnull byte[] buff, int ofst, int size)
	{
		if (this.canWrite && this.sbForm == null)
		{
			if (!writing)
			{
				//cli.Write((UInt8*)"\r\n", 2);
				this.reqMstm.write("\r\n".getBytes(StandardCharsets.UTF_8), 0, 2);
			}
			writing = true;
			//return cli.Write(buff, size);
			return this.reqMstm.write(buff, ofst, size);
		}
		return 0;
	}

	public int flush()
	{
		return 0;
	}

	public void close()
	{
		TCPClient cli;
		if ((cli = this.cli) != null)
		{
			cli.shutdownSend();
			cli.close();
		}
	}

	public boolean recover()
	{
		//////////////////////////////////////////
		return false;
	}

	public boolean connect(@Nonnull String url, @Nonnull RequestMethod method, @Nullable SharedDouble timeDNS, @Nullable SharedDouble timeConn, boolean defHeaders)
	{
		FileStream clsDataFS;
		String urltmp;
		String svrname;
		String host;
		TCPClient cli;

		int i;
		String ptr1;
		String optptr2;
		String[] ptrs;
		//UnsafeArray<UTF8Char> cptr;
		int port;
		boolean secure = false;
		this.hdrLen = 0;

		this.url = url;
		if (url.startsWith("http://"))
		{
			ptr1 = url.substring(7);
			secure = false;
		}
		else if (url.startsWith("https://"))
		{
			ptr1 = url.substring(8);
			secure = true;
		}
		else
		{
			if (timeDNS != null) timeDNS.value = -1;
			if (timeConn != null) timeConn.value = -1;
			return false;
		}

		if (secure && this.ssl == null)
		{
			if (timeDNS != null) timeDNS.value = -1;
			if (timeConn != null) timeConn.value = -1;
			return false;
		}

		this.setSourceName(url);
		if (SHOWDEBUG)
		{
			System.out.println("Request URL: "+method.toString()+" "+url);
		}
		i = ptr1.indexOf('/');
		if (i != -1)
		{
			urltmp = ptr1.substring(0, i);
			optptr2 = ptr1.substring(i);
		}
		else
		{
			optptr2 = null;
			urltmp = ptr1;
		}
		String hostName;
		if ((hostName = this.forceHost) != null)
		{
			urltmp = hostName;
		}
		else
		{
			urltmp = URIEncoding.uriDecode(urltmp);
		}
		host = "Host: "+urltmp + "\r\n";
		if (urltmp.charAt(0) == '[')
		{
			i = urltmp.indexOf(']');
			if (i == -1)
			{
				this.cli = null;

				this.writing = true;
				this.canWrite = false;
				return false;
			}
			svrname = urltmp.substring(1, i);
			if (urltmp.length() > i + 1 && urltmp.charAt(i + 1) == ':')
			{
				port = StringUtil.toIntegerS(urltmp.substring(i + 2), 0);
				urltmp = urltmp.substring(0, i + 1);
			}
			else
			{
				if (secure)
				{
					port = 443;
				}
				else
				{
					port = 80;
				}
			}
		}
		else
		{
			ptrs = StringUtil.split(urltmp, ":");
			if (ptrs.length == 2)
			{
				port = StringUtil.toIntegerS(ptrs[1], 0);
				svrname = ptrs[0];
				urltmp = ptrs[0];
			}
			else
			{
				if (secure)
				{
					port = 443;
				}
				else
				{
					port = 80;
				}
				svrname = ptrs[0];
			}
		}

		this.clk.start();
		String cliHost;
		if ((cliHost = this.cliHost) == null)
		{
			this.cliHost = cliHost = urltmp;

			double t1;
			InetAddress addr;
			if (svrname.equals("localhost"))
			{
				addr = Inet4Address.getLoopbackAddress();
			}
			else
			{
				try
				{
					addr = InetAddress.getByName(svrname);
				}
				catch (UnknownHostException ex)
				{
					this.cli = null;

					this.writing = true;
					this.canWrite = false;
					return false;
				}
			}
			if (timeDNS != null) timeDNS.value = this.clk.getTimeDiff();
			this.svrAddr = addr;
			if (SHOWDEBUG)
			{
				System.out.println("Server IP: "+addr.getHostAddress()+":"+port+", t = "+addr.getClass().getName());
			}
			SSLEngine ssl;
			if (secure && (ssl = this.ssl) != null)
			{
				//Net.SSLEngine.ErrorType err;
				this.cli = cli = ssl.clientConnect(clif, cliHost, port, this.timeout);
				if (cli == null || cli.isConnectError())
				{
					if (SHOWDEBUG)
					{
						System.out.println("Connect error: ");
					}
					if (cli != null) cli.dispose();
					this.cli = null;
				}
			}
			else
			{
				cli = this.clif.create(this.svrAddr, port, this.timeout);
				this.cli = cli;
			}
			t1 = this.clk.getTimeDiff();
			if (timeConn != null) timeConn.value = t1;
			if (DEBUGSPEED)
			{
				if (t1 > 0.01)
				{
					System.out.println("Time in connect: "+t1);
				}
			}

			Socket soc;
			if ((cli = this.cli) == null)
			{
				this.writing = true;
				this.canWrite = false;
				return false;
			}
			else if (cli.isConnectError() || (soc = cli.getSocket()) == null)
			{
				if (SHOWDEBUG)
				{
					System.out.println("Error in connect to server");
				}
				cli.dispose();
				this.cli = null;

				this.writing = true;
				this.canWrite = false;
				return false;
			}
			try
			{
				soc.setSoLinger(false, 0);
				soc.setTcpNoDelay(true);
			}
			catch (SocketException ex)
			{

			}
		}
		else if (cliHost.equals(urltmp) && (cli = this.cli) != null)
		{
			if (this.buffSize > 0)
			{
				this.contRead += this.buffSize;
				this.buffSize = 0;
			}
			while (this.contRead != this.contLeng)
			{
				int size = BUFFSIZE;
				if (size > (this.contLeng - this.contRead))
				{
					size = (int)(this.contLeng - this.contRead);
				}
				size = cli.read(this.dataBuff, 0, size);
				this.totalDownload += size;
				if (SHOWDEBUG)
				{
					System.out.println("Read from remote(5), size = "+size);
				}
				if (size == 0)
				{
					if (timeDNS != null) timeDNS.value = -1;
					if (timeConn != null) timeConn.value = -1;
					return false;
				}
				if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
				{
					clsDataFS.write(this.dataBuff, 0, size);
				}
				this.contRead += size;
			}
			if (timeDNS != null) timeDNS.value = 0;
			if (timeConn != null) timeConn.value = 0;
			this.contRead = 0;
			this.headers.clear();
			this.reqHeaders.clear();
		}
		else
		{
			if (timeDNS != null) timeDNS.value = -1;
			if (timeConn != null) timeConn.value = -1;
			return false;
		}

		String ptr2;
		if ((ptr2 = optptr2) == null)
		{
			ptr2 = "/";
		}
		String reqStr;
		switch (method)
		{
		case HTTP_POST:
			this.canWrite = true;
			this.writing = false;
			reqStr = "POST "+ptr2+" HTTP/1.1\r\n";
			break;
		case HTTP_PUT:
			this.canWrite = true;
			this.writing = false;
			reqStr = "PUT "+ptr2+" HTTP/1.1\r\n";
			break;
		case HTTP_PATCH:
			this.canWrite = true;
			this.writing = false;
			reqStr = "PATCH "+ptr2+" HTTP/1.1\r\n";
			break;
		case HTTP_DELETE:
			this.canWrite = true;
			this.writing = false;
			reqStr = "DELETE "+ptr2+" HTTP/1.1\r\n";
			break;
		case HTTP_HEAD:
			this.canWrite = true;
			this.writing = false;
			reqStr = "HEAD "+ptr2+" HTTP/1.1\r\n";
			break;
		case HTTP_CONNECT:
		case Unknown:
		case HTTP_GET:
		default:
			this.canWrite = false;
			this.writing = false;
			reqStr = "GET "+ptr2+" HTTP/1.1\r\n";
			break;
		case HTTP_TRACE:
			this.canWrite = false;
			this.writing = false;
			reqStr = "TRACE "+ptr2+" HTTP/1.1\r\n";
			break;

		case HTTP_OPTIONS:
		case RTSP_DESCRIBE:
		case RTSP_ANNOUNCE:
		case RTSP_GET_PARAMETER:
		case RTSP_PAUSE:
		case RTSP_PLAY:
		case RTSP_RECORD:
		case RTSP_REDIRECT:
		case RTSP_SETUP:
		case RTSP_SET_PARAMETER:
		case RTSP_TEARDOWN:
			this.canWrite = false;
			this.writing = false;
			reqStr = "TEARDOWN "+ptr2+" RTSP/1.0\r\n";
			break;
		}
		byte[] tmpbuff = reqStr.getBytes(StandardCharsets.UTF_8);
		this.reqMstm.write(tmpbuff, 0, tmpbuff.length);
		tmpbuff = host.getBytes(StandardCharsets.UTF_8);
		this.reqMstm.write(tmpbuff, 0, tmpbuff.length);
		if (SHOWDEBUG)
		{
			System.out.print("Request Data: "+reqStr);
			System.out.print("Add Header: "+host);
		}

		if (defHeaders)
		{
			this.addHeader("User-Agent", this.userAgent);
			this.addHeader("Accept", "*/*");
			this.addHeader("Accept-Language", "*");
			if (this.kaConn)
			{
				this.addHeader("Connection", "keep-alive");
			}
			else
			{
				this.addHeader("Connection", "close");
			}
		}
		return true;
	}

	public void addHeader(@Nonnull String name, @Nonnull String value)
	{
		if (this.reqHeaders.sortedIndexOf(name) >= 0)
		{
			if (SHOWDEBUG)
			{
				System.out.println("Add Header Failed(duplicated): "+name+": "+value);
			}
			return;
		}

		if (this.cli != null && !this.writing)
		{
			this.reqMstm.write((name + ": "+value+"\r\n").getBytes(StandardCharsets.UTF_8));
			if (SHOWDEBUG)
			{
				System.out.println("Add Header: "+name+": "+value);
			}
			this.reqHeaders.sortedInsert(name);
		}
		else
		{
			if (SHOWDEBUG)
			{
				System.out.println("Add Header Failed(Non-writing state): "+name+": "+value);
			}
		}
	}

	public void endRequest(@Nullable SharedDouble timeReq, @Nullable SharedDouble timeResp)
	{
		FileStream clsDataFS;
		TCPClient cli;
		Socket soc;
		if ((this.writing && !this.canWrite) || (cli = this.cli) == null || (soc = cli.getSocket()) == null)
		{
			if (timeReq != null) timeReq.value = -1;
			if (timeResp != null) timeResp.value = -1;
			return;
		}
		else
		{
			double t1;
			if (SHOWDEBUG)
			{
				System.out.println("End Request begin");
			}
			StringBuilder sbForm;
			if ((sbForm = this.sbForm) != null)
			{
				byte[] formData = sbForm.toString().getBytes(StandardCharsets.UTF_8);
				this.addContentLength(formData.length);
				this.sbForm = null;
				this.write(formData, 0, formData.length);
			}
			this.canWrite = false;
			this.writing = true;

			this.reqMstm.write("\r\n".getBytes(StandardCharsets.UTF_8), 0, 2);
			int writeSize = 0;
			int currSize = 0;
			byte[] reqBuff = this.reqMstm.getBuff();
			int reqSize = reqBuff.length;
			while (writeSize < reqSize)
			{
				currSize = cli.write(reqBuff, writeSize, reqSize - writeSize);
				if (SHOWDEBUG)
				{
					System.out.println("Writing "+(reqSize - writeSize)+" bytes, sent "+currSize+" bytes");
				}
				if (currSize <= 0)
					break;
				this.totalUpload += currSize;
				writeSize += currSize;
			}
			this.reqMstm.clear();

			try
			{
				soc.setSoLinger(false, 0);
			}
			catch (SocketException ex)
			{
			}
			if (!this.kaConn && !cli.isSSL())
				cli.shutdownSend();
			cli.setTimeout((int)this.timeout.toMillis());
			t1 = this.clk.getTimeDiff();
			if (timeReq != null) timeReq.value = t1;

			this.buffSize = 0;
			while (this.buffSize < 32)
			{
				int recvSize = cli.read(this.dataBuff, this.buffSize, BUFFSIZE - 1 - this.buffSize);
				if (SHOWDEBUG)
				{
					System.out.println("Read from remote(6) = "+recvSize);
				}
				if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
				{
					if (recvSize > 0)
					{
						clsDataFS.write(this.dataBuff, this.buffSize, recvSize);
					}
				}
				this.totalDownload += recvSize;
				this.buffSize += recvSize;
				if (recvSize <= 0)
					break;
			}
			t1 = this.clk.getTimeDiff();
			if (timeResp != null) timeResp.value = t1;
			if (DEBUGSPEED)
			{
				if (t1 > 0.01)
				{
					System.out.println("Request time = "+t1);
				}
			}
			if (SHOWDEBUG)
			{
				System.out.println("Read buffSize = "+this.buffSize);
			}
			this.dataBuff[this.buffSize] = 0;
			if (StringUtil.startsWithC(this.dataBuff, 0, this.buffSize, "HTTP/"))
			{
				String lineBuff;
				String[] ptrs;
				int ptr;
				int ptrEnd;
				String s;
				int i;
				i = StringUtil.indexOfUTF8(this.dataBuff, 0, this.buffSize, "\r\n");
				lineBuff = new String(this.dataBuff, 0, i, StandardCharsets.UTF_8);
				if (SHOWDEBUG)
				{
					System.out.println("Read HTTP response: "+lineBuff);
				}
				ptrs = StringUtil.split(lineBuff, " ");
				this.respCode = (ptrs.length >= 2)?StringUtil.toIntegerS(ptrs[1], 0):0;

				if (this.respCode == StatusCode.UNKNOWN)
				{
					if (SHOWDEBUG)
					{
						System.out.println("Unhandled HTTP response: "+lineBuff);
					}
					this.respCode = StatusCode.UNKNOWN;
				}

				ptr = i + 2;
				ptrEnd = this.buffSize;
				this.contLeng = 0x7fffffff;
				this.contRead = 0;
				boolean header = true;
				boolean eventStream = false;
				int keepAliveTO = 0;
				while (header)
				{
					while ((i = StringUtil.indexOfUTF8(this.dataBuff, ptr, ptrEnd - ptr, "\n")) != -1 && i > 0)
					{
						if (i == 1 && this.dataBuff[ptr] == '\r')
						{
							ptr++;
							i = 0;
							break;
						}
						if (this.dataBuff[ptr + i - 1] == '\r')
						{
							s = new String(this.dataBuff, ptr, i - 1, StandardCharsets.UTF_8);
						}
						else
						{
							s = new String(this.dataBuff, ptr, i, StandardCharsets.UTF_8);
						}
						if (SHOWDEBUG)
						{
							System.out.println("Read Header: "+s);
						}
						this.headers.add(s);

						String ls = s.toLowerCase();
						if (ls.startsWith("transfer-encoding: "))
						{
							if (s.startsWith("chunked", 19))
							{
								this.contEnc = 1;
								this.chunkSizeLeft = 0;
							}
						}
						else if (ls.startsWith("content-"))
						{
							if (ls.startsWith("length: ", 8))
							{
								this.contLeng = StringUtil.toLongS(s.substring(16).trim(), 0);
							}
							else if (ls.startsWith("type: text/event-stream", 8))
							{
								eventStream = true;
							}
							else if (ls.startsWith("disposition: ", 8))
							{
								int si = s.indexOf("filename=", 21);
								if (si >= 0)
								{
									if (s.charAt(si + 9) == '"')
									{
										int sj = s.indexOf('"', si + 10);
										if (sj > 0)
										{
											String tmpS = s.substring(si + 10, sj);
											this.setSourceName(tmpS);
										}
									}
									else
									{
										this.setSourceName(s.substring(si + 9));
									}
								}
							}
						}
						else if (ls.startsWith("keep-alive: timeout="))
						{
							keepAliveTO = StringUtil.toIntegerS(s.substring(20), 0);
						}

						ptr += i + 1;
					}
					if (i == 0)
					{
						ptr += 1;
						this.hdrLen += ptr;
						this.buffSize -= ptr;
						ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, ptr, this.buffSize);

						header = false;
						break;
					}
					this.hdrLen += ptr;
					this.buffSize = ptrEnd - ptr;
					ByteTool.copyArray(this.dataBuff, 0, this.dataBuff, ptr, this.buffSize);
					i = cli.read(this.dataBuff, this.buffSize, BUFFSIZE - 1 - this.buffSize);
					if (i <= 0)
					{
						header = false;
						break;
					}
					else
					{
						if (SHOWDEBUG)
						{
							System.out.println("Read from remote(7) = "+i);
						}
						if (LOGREPLY && (clsDataFS = this.clsDataFS) != null)
						{
							clsDataFS.write(this.dataBuff, this.buffSize, i);
						}
						this.totalDownload += i;
						this.buffSize += i;
						this.dataBuff[this.buffSize] = 0;
						ptr = 0;
						ptrEnd = this.buffSize;
					}
				}
				if (eventStream && keepAliveTO != 0)
				{
					cli.setTimeout((int)keepAliveTO * 1000);
				}
			}
			else
			{
				if (SHOWDEBUG)
				{
					System.out.println("No reply HTTP header");
					System.out.println("Reply: "+new String(this.dataBuff, 0, this.buffSize));
				}
				this.respCode = StatusCode.UNKNOWN;
			}
			if (SHOWDEBUG)
			{
				System.out.println("End Request end status = "+this.respCode);
			}
		}
	}

	public int getRespHeaderCnt()
	{
		return this.headers.size();
	}

	@Nullable
	public String getRespHeader(int index)
	{
		if (index < 0 || index >= this.headers.size())
			return null;
		return this.headers.get(index);
	}

	@Nullable
	public String getRespHeader(@Nonnull String name)
	{
		String s2 = (name+": ").toLowerCase();
		String s;
		String ls;
		Iterator<String> it = this.headers.iterator();
		while (it.hasNext())
		{
			s = it.next();
			ls = s.toLowerCase();
			if (ls.startsWith(s2))
			{
				return s.substring(s2.length());
			}
		}
		return null;
	}

	@Nullable
	public String getContentEncoding()
	{
		return this.getRespHeader("Content-Encoding");
	}

	@Nullable
	public ZonedDateTime getLastModified()
	{
		this.endRequest(null, null);
		String t;
		if ((t = this.getRespHeader("Last-Modified")) != null)
		{
			return HTTPClient.parseDateStr(t);
		}
		return null;
	}

	public void setTimeout(@Nonnull Duration timeout)
	{
		TCPClient cli;
		this.timeout = timeout;
		if ((cli = this.cli) != null)
			cli.setTimeout((int)timeout.toMillis());
	}

	public void setReadTimeout(int timeoutMS)
	{
		TCPClient cli;
		this.timeout = Duration.ofMillis(timeoutMS);
		if ((cli = this.cli) != null)
			cli.setTimeout(timeoutMS);
	}

	public boolean isSecureConn()
	{
		TCPClient cli;
		if ((cli = this.cli) == null)
		{
			return false;
		}
		return cli.isSSL();
	}

	public boolean setClientCert(@Nonnull MyX509Cert cert, @Nonnull MyX509File key)
	{
		SSLEngine ssl;
		if ((ssl = this.ssl) == null)
			return false;
		return ssl.clientSetCertASN1(cert, key);
	}

	@Nullable
	public List<MyX509Cert> getServerCerts()
	{
		TCPClient cli;
		if ((cli = this.cli) != null && cli.isSSL())
		{
			List<Certificate> certList = cli.getRemoteCerts();
			if (certList == null)
			{
				return null;
			}
			List<MyX509Cert> certs = new ArrayList<MyX509Cert>();
			Iterator<Certificate> it = certList.iterator();
			while (it.hasNext())
			{
				certs.add(CertUtil.toMyCert(it.next()));
			}
			return certs;
		}
		return null;
	}
}
