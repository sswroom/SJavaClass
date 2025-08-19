package org.sswr.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.Base64Enc.B64Charset;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class HTTPProxyTCPClient extends TCPClient
{
	private static LogTool log = null;
	private static int tunnelConnectTO = 4000;
	public enum PasswordType
	{
		None,
		Basic
	}

	public static void setLog(LogTool log)
	{
		HTTPProxyTCPClient.log = log;
	}

	public static void setTunnelConnectTimeout(int ms)
	{
		tunnelConnectTO = ms;
	}

	public HTTPProxyTCPClient(@Nonnull SocketFactory sockf, @Nonnull String proxyHost, int proxyPort, @Nonnull PasswordType pt, @Nullable String userName, @Nullable String pwd, @Nonnull String destHost, int destPort)
	{
		super(null, null);
		super.setSourceName(destHost);

		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(proxyHost);
		}
		catch (UnknownHostException ex)
		{
			if (log != null)
			{
				log.logMessage("HTTPProxyTCP: Error in resolving host: "+proxyHost, LogLevel.ERROR);
				log.logException(ex);
			}
			this.flags |= 12;
			return;
		}
		Socket s;
		s = new Socket();
		try
		{
			s.connect(new InetSocketAddress(addr, proxyPort), 15000);
		}
		catch (Exception ex)
		{
			if (log != null)
			{
				log.logMessage("HTTPProxyTCP: Error in connecting to proxy server", LogLevel.ERROR);
				log.logException(ex);
			}
			try
			{
				s.close();
			}
			catch (IOException ex2)
			{
				log.logException(ex2);
			}
			this.s = null;
			this.flags |= 12;
			return;
		}
		this.s = s;
		this.flags = 0;
		this.cliId = SocketUtil.genSocketId(s);
		
		StringBuilder sbReq = new StringBuilder();
		sbReq.append("CONNECT "+destHost+":"+destPort+" HTTP/1.1\r\n");
		sbReq.append("Host: "+destHost+":"+destPort+"\r\n");
		String userPwd;
		if (pt == PasswordType.Basic && userName != null && pwd != null)
		{
			userPwd = userName + ":"+pwd;
			Base64Enc b64 = new Base64Enc(B64Charset.NORMAL, false);
			byte[] b = userPwd.getBytes(StandardCharsets.UTF_8);
			sbReq.append("Proxy-Authorization: Basic "+b64.encodeBin(b, 0, b.length)+"\r\n");
		}
		sbReq.append("\r\n");
		if (log != null)
		{
			log.logMessage("HTTPProxyTCP: Sending:\r\n"+sbReq.toString(), LogLevel.RAW);
		}
		this.write(sbReq.toString().getBytes(StandardCharsets.UTF_8));
		this.setTimeout(tunnelConnectTO);
		byte[] reqBuff = new byte[512];
		int respSize = this.read(reqBuff, 0, 512);
		this.setTimeout(60000);
	
		String reqStr = new String(reqBuff, 0, respSize, StandardCharsets.UTF_8);
		if (reqStr.startsWith("HTTP/1.1 200"))
		{
			if (log != null) log.logMessage("HTTPProxyTCP: Recv:\r\n"+reqStr, LogLevel.RAW);
		}
		else if (reqStr.startsWith("HTTP/1.0 200"))
		{
			if (log != null) log.logMessage("HTTPProxyTCP: Recv:\r\n"+reqStr, LogLevel.RAW);
		}
		else
		{
			if (log != null)
			{
				log.logMessage("HTTPProxyTCP: Recv: \r\n"+reqStr, LogLevel.ACTION);
				log.logMessage("HTTPProxyTCP: Unknown response", LogLevel.ERROR);
			}
			try
			{
				s.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			this.s = null;
			this.flags |= 12;
			return;
		}
		this.cliId = SocketUtil.genSocketId(s);
	}
}
