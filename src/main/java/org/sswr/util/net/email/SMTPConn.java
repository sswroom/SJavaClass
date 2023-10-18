package org.sswr.util.net.email;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.io.IOWriter;
import org.sswr.util.io.UTF8Reader;
import org.sswr.util.io.UTF8Writer;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.TCPClient;
import org.sswr.util.net.TCPClientType;

public class SMTPConn implements Runnable
{
	private static boolean VERBOSE = false;

	private TCPClient cli;
	private UTF8Writer writer;
	private boolean threadToStop;
	private boolean threadRunning;
	private boolean threadStarted;
	private boolean statusChg;
	private int lastStatus;
	private byte[] msgRet;
	private int msgRetOfst;
	private ThreadEvent evt;
	private IOWriter logWriter;
	private int initCode;
	private int maxSize;
	private boolean authPlain;
	private boolean authLogin;

	@Override
	public void run()
	{
		UTF8Reader reader;
		StringBuilder sb = new StringBuilder();
		int msgCode;
	
		this.threadStarted = true;
		this.threadRunning = true;
		reader = new UTF8Reader(this.cli.createInputStream());
		while (!this.threadToStop)
		{
			sb.setLength(0);
			if (!reader.readLine(sb, 2048))
			{
				if (this.logWriter != null)
				{
					this.logWriter.writeLine("Connection Closed");
				}
				break;
			}
	
			if (this.logWriter != null)
			{
				this.logWriter.writeLine(sb.toString());
			}
			if (VERBOSE)
			{
				System.out.println("SMTP Read: "+sb.toString());
			}
			if (sb.charAt(0) == ' ')
			{
				if (this.msgRet != null)
				{
					this.msgRetOfst = StringUtil.concat(this.msgRet, this.msgRetOfst, sb.toString());
					this.msgRetOfst = reader.getLastLineBreak(this.msgRet, this.msgRetOfst);
				}
			}
			else
			{
				msgCode = StringUtil.toIntegerS(sb.substring(0, 3), 0);
				if (msgCode == 235)
				{
//					this.logged = true;
				}
				if (sb.charAt(3) == ' ')
				{
					if (this.msgRet != null)
					{
						this.msgRetOfst = StringUtil.concat(this.msgRet, this.msgRetOfst, sb.substring(4));
						this.msgRet[this.msgRetOfst] = 0;
					}
					this.lastStatus = msgCode;
					this.statusChg = true;
					this.evt.set();
				}
				else if (sb.charAt(3) == '-')
				{
					if (this.msgRet != null)
					{
						this.msgRetOfst = StringUtil.concat(this.msgRet, this.msgRetOfst, sb.substring(4));
						this.msgRetOfst = reader.getLastLineBreak(this.msgRet, this.msgRetOfst);
					}
				}
			}
		}
		this.lastStatus = 0;
		this.statusChg = true;
		this.evt.set();
		try
		{
			reader.close();
		}
		catch (IOException ex)
		{
		}
		this.threadRunning = false;
	}

	private int waitForResult(SharedInt msgRetEnd)
	{
		long startTime = System.currentTimeMillis();
		while (this.threadRunning && !this.statusChg && System.currentTimeMillis() - startTime < 30000)
		{
			this.evt.waitEvent(1000);
		}
		if (msgRetEnd != null)
		{
			msgRetEnd.value = this.msgRetOfst;
		}
		this.msgRet = null;
		if (this.statusChg)
		{
			return this.lastStatus;
		}
		else
			return 0;
	}
	
	public SMTPConn(String host, int port, SSLEngine ssl, SMTPConnType connType, IOWriter logWriter)
	{
		this.threadStarted = false;
		this.threadRunning = false;
		this.threadToStop = false;
		this.msgRet = null;
		this.msgRetOfst = 0;
		this.statusChg = false;
		this.maxSize = 0;
		this.authLogin = false;
		this.authPlain = false;
		this.logWriter = logWriter;
		this.evt = new ThreadEvent(true);
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(host);
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return;
		}
		if (connType == SMTPConnType.SSL)
		{
			this.cli = new TCPClient(host, port, ssl, TCPClientType.SSL);
		}
		else if (connType == SMTPConnType.STARTTLS)
		{
			byte[] buff = new byte[1024];
			int buffSize;
			String buffStr;
			this.cli = new TCPClient(addr, port, ssl, TCPClientType.PLAIN);
			this.cli.setTimeout(2000);
			buffSize = this.cli.read(buff, 0, 1024);
			if (this.logWriter != null)
			{
				this.logWriter.writeStr(new String(buff, 0, buffSize, StandardCharsets.UTF_8));
			}
			
			if (buffSize > 2 && (buffStr = new String(buff, 0, buffSize, StandardCharsets.UTF_8)).startsWith("220 ") && buffStr.endsWith("\r\n"))
			{
				if (this.logWriter != null)
				{
					this.logWriter.writeLine("STARTTLS");
				}
				this.cli.write("STARTTLS\r\n".getBytes(StandardCharsets.UTF_8), 0, 10);
				buffSize = this.cli.read(buff, 0, 1024);
				if (this.logWriter != null)
				{
					this.logWriter.writeStr(new String(buff, 0, buffSize, StandardCharsets.UTF_8));
				}
				if (buffSize > 0 && (buffStr = new String(buff, 0, buffSize, StandardCharsets.UTF_8)).startsWith("220 ") && buffStr.endsWith("\r\n"))
				{
					if (this.logWriter != null)
					{
						this.logWriter.writeLine("SSL Handshake begin");
					}
					
					if (this.cli.sslHandshake())
					{
						if (this.logWriter != null)
						{
							this.logWriter.writeLine("SSL Handshake success");
						}
					}
					else
					{
						if (this.logWriter != null)
						{
							this.logWriter.writeLine("SSL Handshake failed");
						}
					}
				}
				else
				{
					this.cli.close();
				}
			}
			else
			{
				this.cli.close();
			}
		}
		else
		{
			this.cli = new TCPClient(addr, port, ssl, TCPClientType.PLAIN);
		}
		this.cli.setNoDelay(false);
		this.writer = new UTF8Writer(this.cli);
		if (this.logWriter != null)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Connect to ");
			sb.append(host);
			sb.append("(");
			sb.append(addr.toString());
			sb.append("):");
			sb.append(port);
			this.logWriter.writeLine(sb.toString());
		}
		new Thread(this).start();
		while (!this.threadStarted)
		{
			MyThread.sleep(10);
		}
		if (connType == SMTPConnType.STARTTLS)
		{
			this.initCode = 220;
			this.logWriter.writeLine("Connected");
		}
		else
		{
			this.initCode = this.waitForResult(null);
		}
	}

	public void close()
	{
		this.threadToStop = true;
		this.cli.close();
		while (this.threadRunning)
		{
			MyThread.sleep(10);
		}
		this.writer.close();
	}

	public boolean isError()
	{
		return this.initCode != 220 || this.cli.isConnectError();
	}

	public int getMaxSize()
	{
		return this.maxSize;
	}

	public boolean sendHelo(String cliName)
	{
		String sbuff = "HELO" + cliName;
		this.statusChg = false;
		if (this.logWriter != null)
		{
			this.logWriter.writeLine(sbuff);
		}
		this.writer.writeLine(sbuff);
		int code = this.waitForResult(null);
		return code == 250;
	}

	public boolean sendEHlo(String cliName)
	{
		byte[] returnMsg = new byte[2048];
		SharedInt returnMsgEnd = new SharedInt();
		String sbuff = "EHLO " + cliName + "\r\n";
		this.statusChg = false;
		returnMsg[0] = 0;
		this.msgRet = returnMsg;
		this.msgRetOfst = 0;
		if (this.logWriter != null)
		{
			this.logWriter.writeStr(sbuff);
		}
		this.writer.writeStr(sbuff);
		int code = this.waitForResult(returnMsgEnd);
		if (code == 0)
		{
			if (VERBOSE)
			{
				System.out.println("Retry sending EHlo");
			}
			this.statusChg = false;
			this.msgRet = returnMsg;
			this.msgRetOfst = 0;
			this.writer.writeStr(sbuff);
			code = this.waitForResult(returnMsgEnd);
		}
		if (VERBOSE)
		{
			System.out.println("EHlo reply = "+code);
		}
		if (code == 250)
		{
			String s = new String(returnMsg, 0, returnMsgEnd.value, StandardCharsets.UTF_8);
			String[] lines = StringUtil.splitLine(s);
			int i = 0;
			int j = lines.length;
			while (i < j)
			{
				if (lines[i].startsWith("SIZE "))
				{
					this.maxSize = StringUtil.toIntegerS(lines[i].substring(5), 0);
				}
				else if (lines[i].startsWith("AUTH "))
				{
					String[] cols = StringUtil.split(lines[i].substring(5), " ");
					int k = cols.length;
					while (k-- > 0)
					{
						if (cols[k].equals("LOGIN"))
						{
							this.authLogin = true;
						}
						else if (cols[k].equals("PLAIN"))
						{
							this.authPlain = true;
						}
					}
				}
				i++;
			}
		}
		return code == 250;
	}

	public boolean sendAuth(String userName, String password)
	{
		if (this.authPlain)
		{
			byte[] userBuff = userName.getBytes(StandardCharsets.UTF_8);
			byte[] pwdBuff = password.getBytes(StandardCharsets.UTF_8);
			byte[] authBuff = new byte[userBuff.length + pwdBuff.length + 2];
			authBuff[0] = 0;
			ByteTool.copyArray(authBuff, 1, userBuff, 0, userBuff.length);
			authBuff[userBuff.length + 1] = 0;
			ByteTool.copyArray(authBuff, userBuff.length + 2, pwdBuff, 0, pwdBuff.length);
			Base64Enc b64 = new Base64Enc();
			StringBuilder sbCmd = new StringBuilder();
			sbCmd.append("AUTH PLAIN ");
			sbCmd.append(b64.encodeBin(authBuff, 0, authBuff.length));
			
			this.statusChg = false;
			if (this.logWriter != null)
			{
				this.logWriter.writeLine(sbCmd.toString());
			}
			this.writer.writeLine(sbCmd.toString());
			int code = this.waitForResult(null);
			return code == 235;
		}
		else if (this.authLogin)
		{
			byte[] retBuff = new byte[256];
			SharedInt retBuffEnd = new SharedInt();
			Base64Enc b64 = new Base64Enc();
			StringBuilder sbCmd = new StringBuilder();
			int code;
			retBuff[0] = 0;
			this.statusChg = false;
			this.msgRet = retBuff;
			this.msgRetOfst = 0;
			if (this.logWriter != null)
			{
				this.logWriter.writeLine("AUTH LOGIN");
			}
			this.writer.writeLine("AUTH LOGIN");
			code = this.waitForResult(retBuffEnd);
			if (code != 334 || !"VXNlcm5hbWU6".equals(new String(retBuff, 0, retBuffEnd.value, StandardCharsets.UTF_8)))
			{
				if (VERBOSE)
				{
					System.out.println("Error in login1: code = "+code+", msgLen = "+retBuffEnd.value+", msg = "+new String(retBuff, 0, retBuffEnd.value, StandardCharsets.UTF_8));
				}
				return false;
			}
			sbCmd.setLength(0);
			sbCmd.append(b64.encodeBin(userName.getBytes(StandardCharsets.UTF_8)));
			this.statusChg = false;
			this.msgRet = retBuff;
			this.msgRetOfst = 0;
			if (this.logWriter != null)
			{
				this.logWriter.writeLine(sbCmd.toString());
			}
			this.writer.writeLine(sbCmd.toString());
			code = this.waitForResult(retBuffEnd);
			if (code != 334 || !"UGFzc3dvcmQ6".equals(new String(retBuff, 0, retBuffEnd.value, StandardCharsets.UTF_8)))
			{
				if (VERBOSE)
				{
					System.out.println("Error in login2: code = "+code+", msgLen = "+retBuffEnd.value+", msg = "+new String(retBuff, 0, retBuffEnd.value, StandardCharsets.UTF_8));
				}
				return false;
			}
			sbCmd.setLength(0);
			sbCmd.append(b64.encodeBin(password.getBytes(StandardCharsets.UTF_8)));
			this.statusChg = false;
			this.msgRet = retBuff;
			if (this.logWriter != null)
			{
				this.logWriter.writeLine(sbCmd.toString());
			}
			this.writer.writeLine(sbCmd.toString());
			code = this.waitForResult(null);
			return code == 235;
		}
		else
		{
			return false;
		}
	}
	
	public boolean sendMailFrom(String fromEmail)
	{
		String sbuff = "MAIL FROM: <" + fromEmail + ">";
		this.statusChg = false;
		if (this.logWriter != null)
		{
			this.logWriter.writeLine(sbuff);
		}
		this.writer.writeLine(sbuff);
		int code = this.waitForResult(null);
		return code == 250;
	}

	public boolean sendRcptTo(String toEmail)
	{
		String sbuff = "RCPT TO: <" + toEmail + ">";
		this.statusChg = false;
		if (this.logWriter != null)
		{
			this.logWriter.writeLine(sbuff);
		}
		this.writer.writeLine(sbuff);
		int code = this.waitForResult(null);
		return code == 250;
	}

	public boolean sendQuit()
	{
		this.statusChg = false;
		if (this.logWriter != null)
		{
			this.logWriter.writeLine("QUIT");
		}
		this.writer.writeLine("QUIT");
		int code = this.waitForResult(null);
		return code == 221;
	}

	public boolean sendData(byte[] buff, int buffOfst, int buffSize)
	{
		this.statusChg = false;
		if (this.logWriter != null)
		{
			this.logWriter.writeLine("DATA");
		}
		this.writer.writeLine("DATA");
		int code = this.waitForResult(null);
		if (code != 354)
		{
			return false;
		}
		this.statusChg = false;
		this.cli.write(buff, buffOfst, buffSize);
		byte[] endMsg = "\r\n.\r\n".getBytes(StandardCharsets.UTF_8);
		this.cli.write(endMsg, 0, endMsg.length);
		code = this.waitForResult(null);
		return code == 250;
	}
}
