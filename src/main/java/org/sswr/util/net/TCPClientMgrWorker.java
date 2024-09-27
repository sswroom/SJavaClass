package org.sswr.util.net;

import java.util.List;

import org.sswr.util.basic.ThreadEvent;

import jakarta.annotation.Nonnull;

public class TCPClientMgrWorker implements Runnable
{
	private boolean running;
	private boolean toStop;
	private boolean isPrimary;
	private boolean working;
	private TCPClientMgr me;
	private ThreadEvent evt;

	public TCPClientMgrWorker(@Nonnull TCPClientMgr me, boolean isPrimary)
	{
		this.running = false;
		this.toStop = false;
		this.isPrimary = isPrimary;
		this.working = false;
		this.me = me;
		this.evt = new ThreadEvent();

		new Thread(this).run();
	}

	@Override
	public void run() {
		long currTime;
		TCPClientStatus cliStat;
		long lastCheckTime = 0;
		int i;
		{
			this.running = true;
			while (!this.toStop)
			{
				while ((cliStat = me.getWorkerTask()) != null)
				{
					this.working = true;
					currTime = System.currentTimeMillis();
					cliStat.timeStart = currTime;
					cliStat.timeAlerted = false;
					cliStat.processing = true;
					this.me.getHandler().onTCPClientData(cliStat.cli, cliStat.cliData, cliStat.buff, 0, cliStat.buffSize);
					cliStat.processing = false;
					synchronized(cliStat.buff)
					{
						cliStat.recvDataExist = cliStat.cli.getRecvBuffSize() > 0;
						cliStat.reading = true;
					}
					if (!me.writePipe())
					{
						System.out.println("TCPClientMgr: Error in writing to pipe");
					}
				}
				this.working = false;
				if (this.isPrimary)
				{
					currTime = System.currentTimeMillis();
					if ((currTime - lastCheckTime) >= 10000)
					{
						lastCheckTime = currTime;
						List<TCPClientStatus> cliArr = me.getCliArr();
						synchronized(cliArr)
						{
							i = cliArr.size();
							while (i-- > 0)
							{
								cliStat = cliArr.get(i);
								if ((currTime - cliStat.lastDataTimeTicks) > cliStat.cli.getTimeoutMS())
								{
					//				printf("Client data timeout\r\n");
									cliStat.cli.close();
								}
							}
						}
					}
				}
				this.evt.waitEvent(700);
			}
		}
		this.running = false;
	}

	public boolean isWorking()
	{
		return this.working;
	}

	public void setEvent()
	{
		this.evt.set();
	}

	public void stop()
	{
		this.toStop = true;
		this.evt.set();
	}

	public boolean isRunning()
	{
		return this.running;
	}
}
