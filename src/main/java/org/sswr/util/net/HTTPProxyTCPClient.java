package org.sswr.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.Base64Enc.B64Charset;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class HTTPProxyTCPClient extends TCPClient
{
	private static boolean debug = false;
	public enum PasswordType
	{
		None,
		Basic
	}

	public HTTPProxyTCPClient(@Nonnull SocketFactory sockf, @Nonnull String proxyHost, int proxyPort, @Nonnull PasswordType pt, @Nullable String userName, @Nullable String pwd, @Nonnull String destHost, int destPort)
	{
		super(null, null);
		this.setSourceName(destHost);

		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(destHost);
		}
		catch (UnknownHostException ex)
		{
			if (debug)
			{
				System.out.println("HTTPProxyTCP: Error in resolving host: "+proxyHost);
				ex.printStackTrace();
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
			if (debug)
			{
				System.out.println("HTTPProxyTCP: Error in connecting to proxy server");
				ex.printStackTrace();
			}
			try
			{
				s.close();
			}
			catch (IOException ex2)
			{
				ex2.printStackTrace();
			}
			this.s = null;
			this.flags |= 12;
			return;
		}
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
		if (debug)
		{
			System.out.println("HTTPProxyTCP: Sending:\r\n"+sbReq.toString());
		}
		this.write(sbReq.toString().getBytes(StandardCharsets.UTF_8));
		this.setTimeout(4000);
		byte[] reqBuff = new byte[512];
		int respSize = this.read(reqBuff, 0, 512);
		this.setTimeout(-1);
	
		String reqStr = new String(reqBuff, 0, respSize, StandardCharsets.UTF_8);
		if (debug)
		{
			reqBuff[respSize] = 0;
			System.out.println("HTTPProxyTCP: Recv:\r\n"+reqStr);
		}
	
		if (reqStr.startsWith("HTTP/1.1 200"))
		{
		}
		else if (reqStr.startsWith("HTTP/1.0 200"))
		{
		}
		else
		{
			if (debug)
			{
				System.out.println("HTTPProxyTCP: Unknown response\r\n");
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
