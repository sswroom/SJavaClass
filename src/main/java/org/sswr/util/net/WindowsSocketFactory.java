package org.sswr.util.net;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;

public class WindowsSocketFactory implements SocketFactory
{
	@Override
	public InetAddress[] getDefDNS()
	{
		try
		{
			Class<?> cls = Class.forName("sun.net.dns.ResolverConfiguration");
			Method method = cls.getMethod("open");
			Object o = method.invoke(null);
			Class<?> ocls = o.getClass();
			Method nameservers = ocls.getMethod("nameservers");
			Object nsVal = nameservers.invoke(o);
			@SuppressWarnings("unchecked")
			List<String> nsList = (List<String>)nsVal;
			InetAddress[] ret = new InetAddress[nsList.size()];
			int i = ret.length;
			while (i-- > 0)
			{
				ret[i] = InetAddress.getByName(nsList.get(i));
			}
			return ret;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return new InetAddress[0];
	}
	
}
