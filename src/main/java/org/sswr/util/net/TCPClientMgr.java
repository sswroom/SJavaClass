package org.sswr.util.net;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.basic.ArrayListInt64;
import org.sswr.util.basic.SyncCircularBuff;

public class TCPClientMgr implements Runnable
{
	private int timeOutSeconds;
	private TCPClientHandler hdlr;
	private boolean toStop;
	private boolean clientThreadRunning;
	private int workerCnt;
	private boolean pipeHasData;
	private Pipe pipe;
	private TCPClientMgrWorker[] workers;
	private ArrayListInt64 cliIdArr;
	private List<TCPClientStatus> cliArr;
	private SyncCircularBuff<TCPClientStatus> workerTasks;

	private static void removeCliStat(List<TCPClientStatus> cliArr, ArrayListInt64 cliIdArr, TCPClientStatus cliStat)
	{
		int ind;
		int i;
		long cliId = cliStat.cli.getCliId();
	//	printf("Removing CliId: %lld\r\n", cliId); //////////
	
		ind = cliIdArr.sortedIndexOf(cliId);
		if (ind < 0)
		{
			System.out.println("CliId not found");
		}
		if (cliArr.get(ind) == cliStat)
		{
			cliArr.remove(ind);
			cliIdArr.remove(ind);
		}
		else
		{
			i = cliArr.indexOf(cliStat);
			if (i != -1)
			{
				cliArr.remove(i);
				cliIdArr.remove(i);
			}
			else
			{
				System.out.println("CliId not found2\r\n");
			}
		}
	}

	private void processClient(TCPClientStatus cliStat)
	{
		this.workerTasks.put(cliStat);
		int i = this.workerCnt;
		while (i-- > 0)
		{
			if (!this.workers[i].isWorking())
			{
				this.workers[i].setEvent();
				return;
			}
		}
}

	public TCPClientMgr(int timeOutSeconds, TCPClientHandler hdlr, int workerCnt)
	{
		this.cliIdArr = new ArrayListInt64();
		this.cliArr = new ArrayList<TCPClientStatus>();
		this.workerTasks = new SyncCircularBuff<TCPClientStatus>();
		this.timeOutSeconds = timeOutSeconds;
		this.hdlr = hdlr;
		this.toStop = false;
		this.clientThreadRunning = false;
		if (workerCnt <= 0)
			workerCnt = 1;
		this.workerCnt = workerCnt;
		try
		{
			this.pipe = Pipe.open();
		}
		catch (IOException ex)
		{
			this.workers = null;
			return;
		}
		new Thread(this).run();
		this.workers = new TCPClientMgrWorker[workerCnt];
		while (workerCnt-- > 0)
		{
			this.workers[workerCnt] = new TCPClientMgrWorker(this, (workerCnt == 0));
		}
	}

	public void close()
	{
		int i = this.cliArr.size();
		TCPClientStatus cliStat;
		if (i != 0)
		{
			while (i-- > 0)
			{
				synchronized (this.cliArr)
				{
					cliStat = this.cliArr.get(i);
				}
				if (cliStat != null)
				{
					cliStat.cli.close();
				}
			}
		}
		if (this.pipe != null)
		{
			this.writePipe();
		}
		while (this.cliArr.size() > 0)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException ex)
			{
				
			}
		}
		this.toStop = true;
		if (this.pipe != null)
		{
			this.writePipe();
		}
		while (clientThreadRunning)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException ex)
			{

			}
		}
		if (this.workers != null)
		{
			i = this.workerCnt;
			while (i-- > 0)
			{
				this.workers[i].stop();
			}
			boolean exist = true;
			while (exist)
			{
				exist = false;
				i = this.workerCnt;
				while (i-- > 0)
				{
					if (this.workers[i].isRunning())
					{
						exist = true;
						break;
					}
				}
				if (!exist)
					break;
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
	
		if (this.pipe != null)
		{
			try
			{
				
				this.pipe.source().close();
				this.pipe.sink().close();
			}
			catch (IOException ex)
			{
			}
			this.pipe = null;
		}
	}

	@Override
	public void run()
	{
		long currTime;
		int pollRet;
		int i;
		int readSize = 0;
		byte tmpBuff[] = new byte[16];
		TCPClientStatus cliStat;
		int pollfdCap = 16;
		Selector []pollfds = new Selector[pollfdCap];
		TCPClientStatus []pollCli = new TCPClientStatus[pollfdCap];
		int pollCliCnt;
		int pollReqCnt;
		boolean pollPreData = false;
		Selector selector = null;
		try
		{
			selector = Selector.open();
			this.pipe.source().register(selector, SelectionKey.OP_READ, null);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	
		this.clientThreadRunning = true;
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		while (!this.toStop)
		{
			pollPreData = false;
			synchronized(this.cliArr)
			{
				pollCliCnt = this.cliArr.size();
				if (pollCliCnt >= pollfdCap)
				{
					while (pollCliCnt >= pollfdCap)
					{
						pollfdCap = pollfdCap << 1;
					}
					pollfds = new Selector[pollfdCap];
					pollCli = new TCPClientStatus[pollfdCap];
				}
				pollReqCnt = 0;
				
/*				pollfds[0].fd = clsData.piperdfd;
				pollfds[0].events = POLLIN;
				pollfds[0].revents = 0;
				if (this.pipeHasData)
				{
					pollfds[0].revents = POLLIN;
					pollPreData = true;
				}*/
				i = 0;
				while (i < pollCliCnt)
				{
					cliStat = this.cliArr.get(i);
					if (cliStat.reading)
					{
						Socket s = cliStat.cli.getSocket();
						if (s == null || cliStat.cli.isClosed())
						{
							removeCliStat(this.cliArr, this.cliIdArr, cliStat);
							synchronized (cliStat.buff)
							{
							}
							this.hdlr.onTCPClientEvent(cliStat.cli, cliStat.cliData, TCPEventType.Disconnect);

							i--;
							pollCliCnt--;
						}
						else if (cliStat.reading)
						{
/*							pollCli[pollReqCnt] = cliStat;
							pollReqCnt++;
							pollfds[pollReqCnt].fd = -1 + (int)(OSInt)s;
							pollfds[pollReqCnt].events = POLLIN;
							pollfds[pollReqCnt].revents = 0;
							if (cliStat.recvDataExist)
							{
								pollfds[pollReqCnt].revents = POLLIN;
								pollPreData = true;
							}*/
						}
					}
		
					i++;
				}
			}
			if (pollPreData)
			{
				pollRet = 1;
			}
			else
			{
	//			printf("Poll Begin\r\n");
				pollRet = 0;//poll(pollfds, pollReqCnt + 1, -1);
	//			printf("Poll End %d\r\n", i);
			}
			if (pollRet > 0)
			{
/* 				if (pollfds[0].revents != 0)
				{
					pollRet = read(clsData.piperdfd, tmpBuff, 16);
					pollfds[0].revents = 0;
					clsData.hasData = false;
				}*/
				i = pollReqCnt;
				while (i-- > 0)
				{
//					if (pollfds[i + 1].revents != 0)
					{
	//					printf("Cli %d revents %d\r\n", i, pollfds[i + 1].revents);
//						pollfds[i + 1].revents = 0;
						int cliAction = 0;
						synchronized (this.cliArr)
						{
							cliStat = pollCli[i];
							if (cliStat != null && cliStat.reading)
							{
								boolean closed = false;
								boolean toClose = false;
								synchronized(cliStat.buff)
								{
									if (cliStat.cli.isClosed())
									{
										try
										{
											Thread.sleep(1);
										}
										catch (InterruptedException ex)
										{

										}
										closed = true;
									}
									readSize = cliStat.cli.getRecvBuffSize();
									if (!closed && readSize <= 0)
									{
			//							printf("Cli Empty data found\r\n");
										cliStat.recvDataExist = false;
										toClose = true;
									}
									else
									{
			//							printf("Cli Read Begin %d\r\n", readSize);
										readSize = cliStat.cli.read(cliStat.buff, 0, TCPClientStatus.TCP_BUFF_SIZE);
			//							printf("Cli Read End %d\r\n", readSize);
									}
								}
								if (toClose)
								{
									readSize = 0;
									cliStat.cli.shutdownSend();
									cliStat.cli.close();
								}
		
								if (readSize == -1)
								{
		//							printf("Cli readSize = -1\r\n");
									currTime = System.currentTimeMillis();
									if ((currTime - cliStat.lastDataTimeTicks) > this.timeOutSeconds * 1000)
									{
		//								printf("Cli disconnect\r\n");
										cliStat.cli.shutdownSend();
										cliStat.cli.close();
										try
										{
											Thread.sleep(1);
										}
										catch (InterruptedException ex)
										{

										}
										removeCliStat(this.cliArr, this.cliIdArr, cliStat);
										cliAction = 1;
									}
								}
								else if (readSize != 0)
								{
		//							printf("Cli read data\r\n");
									cliStat.reading = false;
									cliStat.lastDataTimeTicks = System.currentTimeMillis();
									cliAction = 2;
								}
								else
								{
		//							printf("Cli end conn\r\n");
									removeCliStat(this.cliArr, this.cliIdArr, cliStat);
									cliAction = 1;
								}
							}
						}
						if (cliAction == 1 && cliStat != null)
						{
							this.hdlr.onTCPClientEvent(cliStat.cli, cliStat.cliData, TCPEventType.Disconnect);
						}
						else if (cliAction == 2 && cliStat != null)
						{
							this.hdlr.onTCPClientEvent(cliStat.cli, cliStat.cliData, TCPEventType.HasData);
							cliStat.buffSize = readSize;
							this.processClient(cliStat);
						}
					}
				}
			}
		}
		if (selector != null)
		{
			try
			{
				selector.close();
			}
			catch (IOException ex)
			{
			}
		}
		this.clientThreadRunning = false;
	}

	TCPClientHandler getHandler()
	{
		return this.hdlr;
	}

	List<TCPClientStatus> getCliArr()
	{
		return this.cliArr;
	}

	TCPClientStatus getWorkerTask()
	{
		return this.workerTasks.get();
	}

	boolean writePipe()
	{
		this.pipeHasData = true;
		byte[] buff = new byte[1];
		buff[0] = 0;
		try
		{
			return this.pipe.sink().write(ByteBuffer.wrap(buff)) == 1;
		}
		catch (IOException ex)
		{
			return false;
		}
}
}
