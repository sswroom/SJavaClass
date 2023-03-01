package org.sswr.util.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.URLString;

public class HTTPQueue
{
	static class DomainStatus
	{
		HTTPClient req1;
		HTTPClient req2;
	}

	private SocketFactory sockf;
	private SSLEngine ssl;
	private Map<String, DomainStatus> statusMap;
	private ThreadEvent statusEvt;

	public HTTPQueue(SocketFactory sockf, SSLEngine ssl)
	{
		this.sockf = sockf;
		this.ssl = ssl;
		this.statusMap = new HashMap<String, DomainStatus>();
		this.statusEvt = new ThreadEvent(true);
	}

	public void dispose()
	{
		this.clear();
	}


	public HTTPClient makeRequest(String url, RequestMethod method, boolean noShutdown)
	{
		String domain = URLString.getURLDomain(url, null);
		boolean found = false;
		DomainStatus status;
		HTTPClient cli = null;
		while (true)
		{
			synchronized(this.statusEvt)
			{
				status = this.statusMap.get(domain);
				if (status != null)
				{
					if (status.req1 == null)
					{
						cli = HTTPClient.createConnect(this.sockf, this.ssl, url, method, noShutdown);
						status.req1 = cli;
						found = true;
					}
					else if (status.req2 == null)
					{
						cli = HTTPClient.createConnect(this.sockf, this.ssl, url, method, noShutdown);
						status.req2 = cli;
						found = true;
					}
				}
				else
				{
					status = new DomainStatus();
					status.req1 = null;
					status.req2 = null;
					cli = HTTPClient.createConnect(this.sockf, this.ssl, url, method, noShutdown);
					status.req1 = cli;
					this.statusMap.put(domain, status);
					found = true;
				}
			}
			if (found)
				break;
			this.statusEvt.waitEvent(1000);
		}
		return cli;
	}
	
	public void endRequest(HTTPClient cli)
	{
		DomainStatus status;
		String url = cli.getURL();
		String domain = URLString.getURLDomain(url, null);
	
		synchronized(this.statusEvt)
		{
			status = this.statusMap.get(domain);
			if (status != null)
			{
				if (status.req1 == cli)
				{
					status.req1 = null;
				}
				else if (status.req2 == cli)
				{
					status.req2 = null;
				}
				cli.close();
				if (status.req1 == null && status.req2 == null)
				{
					this.statusMap.remove(domain);
				}
				this.statusEvt.set();
			}
			else
			{
				cli.close();
			}
		}
	}
	
	public void clear()
	{
		DomainStatus status;
	
		synchronized(this.statusEvt)
		{
			Iterator<DomainStatus> it = this.statusMap.values().iterator();
			while (it.hasNext())
			{
				status = it.next();
				if (status.req1 != null)
				{
					status.req1.close();
				}
				if (status.req2 != null)
				{
					status.req2.close();
				}
			}
		}
		while (this.statusMap.size() > 0)
		{
			MyThread.sleep(10);
		}
	}
}
