package org.sswr.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.basic.ThreadPriority;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TCPServer implements Runnable
{
	class SubThread implements Runnable
	{
		private boolean toStop;
		private boolean threadRunning;
		private ThreadEvent threadEvt;

		public SubThread(@Nonnull ThreadEvent threadEvt)
		{
			this.threadEvt = threadEvt;
			this.toStop = false;
			this.threadRunning = false;
		}

		@Override
		public void run()
		{
			this.threadRunning = true;
			this.threadEvt.set();
			while (!this.toStop)
			{
				Socket s;
				try
				{
					s = TCPServer.this.svrSoc.accept();
					synchronized(TCPServer.this.socs)
					{
						TCPServer.this.socs.addLast(s);
						TCPServer.this.socsEvt.set();
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					TCPServer.this.addLogMsg("Client connect error: " + ex.getMessage(), LogLevel.ERR_DETAIL);
				}
			}
			this.threadRunning = false;
			this.threadEvt.set();
		}
		
	}

	private InetAddress bindAddr;
	private int port;
	private LogTool log;
	private TCPServerHandler hdlr;
	private boolean toStop;
	private boolean error;
	private String logPrefix;
	ServerSocket svrSoc;
	int threadRunning;
	LinkedList<Socket> socs;
	ThreadEvent socsEvt;

	private void addLogMsg(@Nonnull String msg, @Nonnull LogLevel logLev)
	{
		if (this.log != null)
		{
			if (this.logPrefix != null)
			{
				this.log.logMessage(this.logPrefix + msg, logLev);
			}
			else
			{
				this.log.logMessage(msg, logLev);
			}
		}
	}

	public void run()
	{
		TCPServer svr = this;
		int sthreadCnt = 1;
		int i;
		boolean found;
		SubThread[] sthreads = null;
		ThreadEvent threadEvt = null;
	
		MyThread.setPriority(ThreadPriority.HIGHEST);
		try
		{
			svr.svrSoc = new ServerSocket();
		}
		catch (Exception ex)
		{
			svr.addLogMsg("Error in creating socket", LogLevel.ERROR);
			svr.error = true;
			return;
		}

		try
		{
			svr.svrSoc.bind(new InetSocketAddress(svr.bindAddr, svr.port));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			svr.addLogMsg("Cannot bind to the port: " + svr.port, LogLevel.ERROR);
			try
			{
				svr.svrSoc.close();
			}
			catch (Exception ex2)
			{

			}
			svr.svrSoc = null;
			svr.error = true;
			return;
		}
		svr.threadRunning |= 1;
		svr.addLogMsg("Start listening to v4 port " + svr.port, LogLevel.ACTION);
	
		if (sthreadCnt > 0)
		{
			sthreads = new SubThread[sthreadCnt];
			threadEvt = new ThreadEvent(true);
			i = sthreadCnt;
			while (i-- > 0)
			{
				sthreads[i] = new SubThread(threadEvt);
				new Thread(sthreads[i]).start();
			}
			found = true;
			while (found)
			{
				found = false;
				i = sthreadCnt;
				while (i-- > 0)
				{
					if (!sthreads[i].threadRunning)
					{
						found = true;
						break;
					}
				}
				if (!found)
					break;
				threadEvt.waitEvent(100);
			}
		}
		while (!svr.toStop)
		{
			Socket s;
			try
			{
				s = svr.svrSoc.accept();
				synchronized(svr.socs)
				{
					svr.socs.addLast(s);
					svr.socsEvt.set();
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				svr.addLogMsg("Client connect error: " + ex.getMessage(), LogLevel.ERR_DETAIL);
			}
		}
		if (sthreadCnt > 0 && sthreads != null)
		{
			i = sthreadCnt;
			while (i-- > 0)
			{
				sthreads[i].toStop = true;
			}
		}
		try
		{
			svr.svrSoc.close();
		}
		catch (Exception ex)
		{

		}
		if (sthreadCnt > 0 && sthreads != null && threadEvt != null)
		{
			found = true;
			while (true)
			{
				found = false;
				i = sthreadCnt;
				while (i-- > 0)
				{
					if (sthreads[i].threadRunning)
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					break;
				}
				threadEvt.waitEvent();
			}
		}
		svr.addLogMsg("End listening on v4 port " + svr.port, LogLevel.ACTION);
		svr.threadRunning &= ~1;
		return;
	}

	public TCPServer(@Nullable InetAddress bindAddr, int port, @Nullable LogTool log, @Nonnull TCPServerHandler hdlr, @Nullable String logPrefix)
	{
		this.bindAddr = bindAddr;
		this.toStop = false;
		this.error = false;
		this.port = port;
		this.log = log;
		this.svrSoc = null;
		if (logPrefix != null)
		{
			this.logPrefix = logPrefix;
		}
		else
		{
			this.logPrefix = null;
		}
		this.hdlr = hdlr;
		this.threadRunning = 0;
		this.socs = new LinkedList<Socket>();
		this.socsEvt = new ThreadEvent(true);
	
		new Thread(this).start();
		while (true)
		{
			if ((this.threadRunning & 1) != 0 || this.error)
				break;
			MyThread.sleep(10);
		}
		new Thread(new Runnable(){

			@Override
			public void run() {
				TCPServer svr = TCPServer.this;
				
				svr.threadRunning |= 2;
			
				while (!svr.toStop)
				{
					while (svr.socs.size() > 0)
					{
						Socket s = svr.socs.removeFirst();
						//str = svr->socf->GetRemoteName(str, (UInt32*)s);
						svr.addLogMsg("Client connected: " , LogLevel.ACTION);
						svr.hdlr.onTCPConnection(s);
					}
					svr.socsEvt.waitEvent(100);
				}
				svr.threadRunning &= ~2;
			}
			
		}).start();
		while (true)
		{
			if ((this.threadRunning & 1) != 0 || this.error)
				if ((this.threadRunning & 2) != 0)
					if ((this.threadRunning & 4) != 0)
						break;
			MyThread.sleep(10);
		}
	}

	public void close()
	{
		if (!this.toStop)
		{
			this.toStop = true;
			if (this.svrSoc != null)
			{
				try
				{
					this.svrSoc.close();
				}
				catch (Exception ex)
				{

				}
			}
			this.socsEvt.set();
		}
	}

	public boolean isError()
	{
		return this.error;
	}
}
